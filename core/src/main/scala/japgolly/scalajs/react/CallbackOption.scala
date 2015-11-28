package japgolly.scalajs.react

import scala.annotation.tailrec
import scala.collection.generic.CanBuildFrom

// TODO Document CallbackOption

object CallbackOption {
  private[CallbackOption] val someUnit: Option[Unit] = Some(())

  def apply[A](cb: CallbackTo[Option[A]]): CallbackOption[A] =
    new CallbackOption(cb.toScalaFn)

  @deprecated("Use CallbackOption.pass instead.", "0.10.1")
  def empty: CallbackOption[Unit] =
    pass

  def pass: CallbackOption[Unit] =
    CallbackOption(CallbackTo pure someUnit)

  def fail[A]: CallbackOption[A] =
    CallbackOption(CallbackTo pure None)

  def pure[A](a: A): CallbackOption[A] =
    CallbackOption(CallbackTo pure Some(a))

  def liftValue[A](a: => A): CallbackOption[A] =
    liftOption(Some(a))

  def liftOption[A](oa: => Option[A]): CallbackOption[A] =
    CallbackOption(CallbackTo(oa))

  def liftOptionLike[O[_], A](oa: => O[A])(implicit O: OptionLike[O]): CallbackOption[A] =
    liftOption(O toOption oa)

  def liftCallback[A](cb: CallbackTo[A]): CallbackOption[A] =
    CallbackOption(cb map Some.apply)

  def require(condition: => Boolean): CallbackOption[Unit] =
    CallbackOption(CallbackTo(if (condition) someUnit else None))

  def unless(condition: => Boolean): CallbackOption[Unit] =
    require(!condition)

  def matchPF[A, B](a: => A)(pf: PartialFunction[A, B]): CallbackOption[B] =
    liftOption(pf lift a)

  def traverse[T[X] <: TraversableOnce[X], A, B](ta: => T[A])(f: A => CallbackOption[B])
                                                (implicit cbf: CanBuildFrom[T[A], B, T[B]]): CallbackOption[T[B]] =
    liftOption {
      val it = ta.toIterator
      val r = cbf(ta)
      @tailrec
      def go: Option[T[B]] =
        if (it.hasNext)
          f(it.next()).get.runNow() match {
            case Some(b) => r += b; go
            case None    => None
          }
        else
          Some(r.result())
      go
    }

  @inline def sequence[T[X] <: TraversableOnce[X], A](tca: => T[CallbackOption[A]])
                                                     (implicit cbf: CanBuildFrom[T[CallbackOption[A]], A, T[A]]): CallbackOption[T[A]] =
    traverse(tca)(identity)

  /**
   * NOTE: Technically a proper, lawful traversal should return `CallbackOption[Option[B]]`.
   */
  def traverseO[A, B](oa: => Option[A])(f: A => CallbackOption[B]): CallbackOption[B] =
    liftOption(oa.map(f(_).get).flatMap(_.runNow()))

  /**
   * NOTE: Technically a proper, lawful sequence should return `CallbackOption[Option[A]]`.
   */
  @inline def sequenceO[A](oca: => Option[CallbackOption[A]]): CallbackOption[A] =
    traverseO(oca)(identity)

  implicit def toCallback(co: CallbackOption[Unit]): Callback =
    co.toCallback

  implicit def fromCallback(c: Callback): CallbackOption[Unit] =
    c.toCBO

  def keyCodeSwitch[A](e       : ReactKeyboardEvent,
                       altKey  : Boolean = false,
                       ctrlKey : Boolean = false,
                       metaKey : Boolean = false,
                       shiftKey: Boolean = false)
                      (switch  : PartialFunction[Int, CallbackTo[A]]): CallbackOption[A] =
    keyEventSwitch(e, e.nativeEvent.keyCode, altKey, ctrlKey, metaKey, shiftKey)(switch)

  def keyEventSwitch[A, B](e       : ReactKeyboardEvent,
                           a       : A,
                           altKey  : Boolean = false,
                           ctrlKey : Boolean = false,
                           metaKey : Boolean = false,
                           shiftKey: Boolean = false)
                          (switch  : PartialFunction[A, CallbackTo[B]]): CallbackOption[B] =
    for {
      _  <- require(ReactKeyboardEvent.checkKeyMods(e, altKey, ctrlKey, metaKey, shiftKey))
      cb <- matchPF(a)(switch)
      b  <- cb.toCBO
    } yield b

  /**
   * Wraps a `CallbackOption[A]` so that:
   *
   * 1) It only executes if `e.defaultPrevented` is `false`.
   * 2) It sets `e.preventDefault` on successful completion.
   */
  def asEventDefault[A](e: ReactEvent, co: CallbackOption[A]): CallbackOption[A] =
    for {
      _ <- unless(e.defaultPrevented)
      a <- co
      _ <- e.preventDefaultCB.toCBO
    } yield a
}

// =====================================================================================================================

/**
 * Callback that can short-circuit along the way when conditions you specify, aren't met.
 *
 * Especially useful for event handlers such as key handlers, drag-and-drop handlers, etc, where you
 * check a condition, perform an effect, check another condition, perform another effect, etc.
 *
 * This is meant to be lightweight, and be immediately useful without the typical pain of imports, implicit conversions
 * and extension methods then normally accompany monad transforms in Scala.
 *
 * For a more generic (i.e. beyond Option) or comprehensive monad transformer use Scalaz or similar.
 */
final class CallbackOption[A](private val cbfn: () => Option[A]) extends AnyVal {
  import CallbackOption.someUnit

  def get: CallbackTo[Option[A]] =
    CallbackTo lift cbfn

  def getOrElse(default: => A): CallbackTo[A] =
    get.map(_ getOrElse default)

  def toCallback(implicit ev: A =:= Unit): Callback =
    get.void

  def toCallbackB: CallbackB =
    get.map(_.isDefined)

  def unary_!(implicit ev: A =:= Unit): CallbackOption[Unit] =
    CallbackOption(get.map {
      case None    => someUnit
      case Some(_) => None
    })

  def map[B](f: A => B): CallbackOption[B] =
    CallbackOption(get.map(_ map f))

  /**
   * Alias for `map`.
   */
  @inline def |>[B](f: A => B): CallbackOption[B] =
    map(f)

  def flatMapOption[B](f: A => Option[B]): CallbackOption[B] =
    CallbackOption(get.map(_ flatMap f))

  def flatMap[B](f: A => CallbackOption[B]): CallbackOption[B] =
    CallbackOption(get flatMap {
      case Some(a) => f(a).get
      case None    => CallbackTo pure None
    })

  /**
   * Alias for `flatMap`.
   */
  @inline def >>=[B](f: A => CallbackOption[B]): CallbackOption[B] =
    flatMap(f)

  def filter(condition: A => Boolean): CallbackOption[A] =
    CallbackOption(get.map(_ filter condition))

  def withFilter(condition: A => Boolean): CallbackOption[A] =
    filter(condition)

  /**
   * Sequence a callback to run after this, discarding any value produced by this.
   */
  def >>[B](next: CallbackOption[B]): CallbackOption[B] =
    flatMap(_ => next)

  /**
   * Sequence a callback to run before this, discarding any value produced by it.
   */
  def <<[B](prev: CallbackOption[B]): CallbackOption[A] =
    prev >> this

  /**
   * Alias for `>>`.
   *
   * Where `>>` is often associated with Monads, `*>` is often associated with Applicatives.
   */
  @inline def *>[B](next: CallbackOption[B]): CallbackOption[B] =
    this >> next

  /**
   * Sequence actions, discarding the value of the second argument.
   */
  def <*[B](next: CallbackOption[B]): CallbackOption[A] =
    for {
      a <- this
      _ <- next
    } yield a

  def zip[B](cb: CallbackOption[B]): CallbackOption[(A, B)] =
    for {
      a <- this
      b <- cb
    } yield (a, b)

  /**
   * Discard the value produced by this callback.
   */
  def void: CallbackOption[Unit] =
    map(_ => ())

  /**
   * Discard the value produced by this callback.
   *
   * This method allows you to be explicit about the type you're discarding (which may change in future).
   */
  @inline def voidExplicit[B](implicit ev: A =:= B): Callback =
    void

  def orElse(tryNext: CallbackOption[A]): CallbackOption[A] =
    CallbackOption(get flatMap {
      case a@ Some(_) => CallbackTo pure a
      case None       => tryNext.get
    })

  /**
   * Alias for `orElse`.
   */
  @inline def |(tryNext: CallbackOption[A]): CallbackOption[A] =
    orElse(tryNext)

  def &&[B](b: CallbackOption[B]): CallbackOption[Unit] =
    this >> b.void

  def ||[B](b: CallbackOption[B]): CallbackOption[Unit] =
    void | b.void
}
