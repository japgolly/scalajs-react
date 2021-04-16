package japgolly.scalajs.react

import japgolly.scalajs.react.CallbackTo.MapGuard
import japgolly.scalajs.react.internal.{OptionLike, identityFn}
import org.scalajs.dom.{document, html}
import scala.annotation.tailrec
import scala.collection.compat._

// TODO Document CallbackOption

object CallbackOption {
  private[CallbackOption] val someUnit: Option[Unit] = Some(())

  @deprecated("Use callback.asCBO", "1.8.0")
  def apply[A](callback: CallbackTo[Option[A]]): CallbackOption[A] =
    callback.asCBO

  def pass: CallbackOption[Unit] =
    CallbackTo.pure(someUnit).asCBO

  def fail[A]: CallbackOption[A] =
    CallbackTo.pure[Option[A]](None).asCBO

  def pure[A](a: A): CallbackOption[A] =
    CallbackTo.pure[Option[A]](Some(a)).asCBO

  def delay[A](a: => A): CallbackOption[A] =
    option(Some(a))

  def option[A](oa: => Option[A]): CallbackOption[A] =
    CallbackTo(oa).asCBO

  def maybe[O[_], A](oa: => O[A])(implicit O: OptionLike[O]): CallbackOption[A] =
    option(O toOption oa)

  def optionCallback[A](oc: => Option[CallbackTo[A]]): CallbackOption[A] =
    CallbackTo.sequenceOption(oc).asCBO

  def maybeCallback[O[_], A](oa: => O[CallbackTo[A]])(implicit O: OptionLike[O]): CallbackOption[A] =
    optionCallback(O toOption oa)

  @deprecated("Use CallbackOption.delay", "1.8.0")
  def liftValue[A](a: => A): CallbackOption[A] =
    delay(a)

  @deprecated("Use CallbackOption.option", "1.8.0")
  def liftOption[A](oa: => Option[A]): CallbackOption[A] =
    option(oa)

  @deprecated("Use CallbackOption.maybe", "1.8.0")
  def liftOptionLike[O[_], A](oa: => O[A])(implicit O: OptionLike[O]): CallbackOption[A] =
    maybe(oa)

  @deprecated("Use callback.toCBO", "1.8.0")
  def liftCallback[A](callback: CallbackTo[A]): CallbackOption[A] =
    callback.toCBO

  @deprecated("Use CallbackOption.optionCallback", "1.8.0")
  def liftOptionCallback[A](oc: => Option[CallbackTo[A]]): CallbackOption[A] =
    optionCallback(oc)

  @deprecated("Use CallbackOption.maybeCallback", "1.8.0")
  def liftOptionLikeCallback[O[_], A](oa: => O[CallbackTo[A]])(implicit O: OptionLike[O]): CallbackOption[A] =
    maybeCallback(oa)

  def require(condition: => Boolean): CallbackOption[Unit] =
    CallbackTo(if (condition) someUnit else None).asCBO

  def unless(condition: => Boolean): CallbackOption[Unit] =
    require(!condition)

  def matchPF[A, B](a: => A)(pf: PartialFunction[A, B]): CallbackOption[B] =
    option(pf lift a)

  /**
   * Tail-recursive callback. Uses constant stack space.
   *
   * Based on Phil Freeman's work on stack safety in PureScript, described in
   * [[http://functorial.com/stack-safety-for-free/index.pdf Stack Safety for
   * Free]].
   */
  def tailrec[A, B](a: A)(f: A => CallbackOption[Either[A, B]]): CallbackOption[B] =
    option {
      @tailrec
      def go(a: A): Option[B] =
        f(a).asCallback.runNow() match {
          case Some(Left(n))  => go(n)
          case Some(Right(b)) => Some(b)
          case None           => None
        }
      go(a)
    }

  def traverse[T[X] <: IterableOnce[X], A, B](ta: => T[A])(f: A => CallbackOption[B])
                                             (implicit cbf: BuildFrom[T[A], B, T[B]]): CallbackOption[T[B]] =
    option {
      val _ta = ta
      val it = _ta.iterator
      val r = cbf.newBuilder(_ta)
      @tailrec
      def go: Option[T[B]] =
        if (it.hasNext)
          f(it.next()).asCallback.runNow() match {
            case Some(b) => r += b; go
            case None    => None
          }
        else
          Some(r.result())
      go
    }

  def sequence[T[X] <: IterableOnce[X], A](tca: => T[CallbackOption[A]])
                                          (implicit cbf: BuildFrom[T[CallbackOption[A]], A, T[A]]): CallbackOption[T[A]] =
    traverse(tca)(identityFn)

  /**
   * NOTE: Technically a proper, lawful traversal should return `CallbackOption[Option[B]]`.
   */
  def traverseOption[A, B](oa: => Option[A])(f: A => CallbackOption[B]): CallbackOption[B] =
    option(oa.map(f(_).asCallback).flatMap(_.runNow()))

  /**
   * NOTE: Technically a proper, lawful sequence should return `CallbackOption[Option[A]]`.
   */
  def sequenceOption[A](oca: => Option[CallbackOption[A]]): CallbackOption[A] =
    traverseOption(oca)(identityFn)

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
    keyEventSwitch(e, e.keyCode, altKey, ctrlKey, metaKey, shiftKey)(switch)

  def keyEventSwitch[A, B](e       : ReactKeyboardEvent,
                           a       : A,
                           altKey  : Boolean = false,
                           ctrlKey : Boolean = false,
                           metaKey : Boolean = false,
                           shiftKey: Boolean = false)
                          (switch  : PartialFunction[A, CallbackTo[B]]): CallbackOption[B] =
    for {
      _  <- require(e.pressedModifierKeys(altKey, ctrlKey, metaKey, shiftKey))
      cb <- matchPF(a)(switch)
      b  <- cb.toCBO
    } yield b

  /** Returns the currently focused HTML element (if there is one). */
  lazy val activeHtmlElement: CallbackOption[html.Element] =
    option(
      Option(document.activeElement)
        .flatMap(_.domToHtml)
        .filterNot(_ eq document.body))

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
final class CallbackOption[+A](private[react] val cbfn: () => Option[A]) extends AnyVal {
  import CallbackOption.someUnit

  /** The underlying representation of this value-class */
  @inline def underlyingRepr: () => Option[A] =
    cbfn

  def getOrElse[AA >: A](default: => AA): CallbackTo[AA] =
    asCallback.map(_ getOrElse default)

  def asCallback: CallbackTo[Option[A]] =
    CallbackTo lift cbfn

  def toCallback(implicit ev: A <:< Unit): Callback =
    Callback(cbfn())

  def unary_!(implicit ev: A <:< Unit): CallbackOption[Unit] =
    asCallback.map {
      case None    => someUnit
      case Some(_) => None
    }.asCBO

  def map[B](f: A => B)(implicit ev: MapGuard[B]): CallbackOption[ev.Out] =
    new CallbackOption(() => cbfn().map(f))

  /**
   * Alias for `map`.
   */
  @inline def |>[B](f: A => B)(implicit ev: MapGuard[B]): CallbackOption[ev.Out] =
    map(f)

  def flatMapOption[B](f: A => Option[B]): CallbackOption[B] =
    asCallback.map(_ flatMap f).asCBO

  def flatMapCB[B](f: A => CallbackTo[B]): CallbackOption[B] =
    flatMap(f(_).toCBO)

  def flatMap[B](f: A => CallbackOption[B]): CallbackOption[B] =
    asCallback.flatMap {
      case Some(a) => f(a).asCallback
      case None    => CallbackTo.pure[Option[B]](None)
    }.asCBO

  /**
   * Alias for `flatMap`.
   */
  @inline def >>=[B](f: A => CallbackOption[B]): CallbackOption[B] =
    flatMap(f)

  def filter(condition: A => Boolean): CallbackOption[A] =
    new CallbackOption(() => cbfn().filter(condition))

  def filterNot(condition: A => Boolean): CallbackOption[A] =
    new CallbackOption(() => cbfn().filterNot(condition))

  @inline def withFilter(condition: A => Boolean): CallbackOption[A] =
    filter(condition)

  /**
   * Sequence a callback to run after this, discarding any value produced by this.
   */
  def >>[B](next: CallbackOption[B]): CallbackOption[B] =
    flatMap(_ => next)

  /**
   * Sequence a callback to run before this, discarding any value produced by it.
   */
  @inline def <<[B](prev: CallbackOption[B]): CallbackOption[A] =
    prev >> this

  /** Convenient version of `<<` that accepts an Option */
  def <<?[B](prev: Option[CallbackOption[B]]): CallbackOption[A] =
    prev.fold(this)(_ >> this)

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
  @inline def voidExplicit[B](implicit ev: A <:< B): CallbackOption[Unit] =
    void

  /**
   * Conditional execution of this callback.
   *
   * @param cond The condition required to be `true` for this callback to execute.
   */
  def when(cond: => Boolean): CallbackOption[A] =
    new CallbackOption[A](() => if (cond) cbfn() else None)

  /**
   * Conditional execution of this callback.
   * Reverse of [[when()]].
   *
   * @param cond The condition required to be `false` for this callback to execute.
   * @return `Some` result of the callback executed, else `None`.
   */
  @inline def unless(cond: => Boolean): CallbackOption[A] =
    when(!cond)

  def orElse[AA >: A](tryNext: CallbackOption[AA]): CallbackOption[AA] =
    new CallbackOption(() => cbfn().orElse(tryNext.cbfn()))

  /**
   * Alias for `orElse`.
   */
  @inline def |[AA >: A](tryNext: CallbackOption[AA]): CallbackOption[AA] =
    orElse(tryNext)

  @inline def &&[B](b: CallbackOption[B]): CallbackOption[Unit] =
    this >> b.void

  @inline def ||[B](b: CallbackOption[B]): CallbackOption[Unit] =
    void | b.void

  /** Wraps this so that:
    *
    * 1) It only executes if `e.defaultPrevented` is `false`.
    * 2) It sets `e.preventDefault` on successful completion.
    */
  def asEventDefault(e: ReactEvent): CallbackOption[A] =
    (this <* e.preventDefaultCB.toCBO).unless(e.defaultPrevented)
}
