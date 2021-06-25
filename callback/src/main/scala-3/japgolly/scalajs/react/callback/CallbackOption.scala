package japgolly.scalajs.react.callback

import japgolly.scalajs.react.callback.CallbackTo.MapGuard
import japgolly.scalajs.react.util.DomUtil._
import japgolly.scalajs.react.util.OptionLike
import japgolly.scalajs.react.util.Util.identityFn
import org.scalajs.dom.{document, html}
import scala.annotation.*
import scala.collection.BuildFrom
import scala.language.`3.0`

// TODO Document CallbackOption

object CallbackOption {
  type UnderlyingRepr[+A] = () => Option[A]

  private[CallbackOption] val someUnit: Option[Unit] = Some(())

  @deprecated("Use callback.asCBO", "2.0.0")
  def apply[A](callback: CallbackTo[Option[A]]): CallbackOption[A] =
    callback.asCBO

  def pass: CallbackOption[Unit] =
    option(someUnit)

  inline def fail[A]: CallbackOption[A] =
    option(None)

  def pure[A](a: A): CallbackOption[A] =
    option(Some(a))

  inline def delay[A](inline a: A): CallbackOption[A] =
    option(Some(a))

  inline def option[A](inline oa: Option[A]): CallbackOption[A] =
    new CallbackOption(() => oa)

  def maybe[O[_], A](oa: => O[A])(using O: OptionLike[O]): CallbackOption[A] =
    option(O toOption oa)

  inline def optionCallback[A](inline oc: Option[CallbackTo[A]]): CallbackOption[A] =
    CallbackTo.sequenceOption(oc).asCBO

  def maybeCallback[O[_], A](oa: => O[CallbackTo[A]])(implicit O: OptionLike[O]): CallbackOption[A] =
    optionCallback(O toOption oa)

  @deprecated("Use CallbackOption.delay", "2.0.0")
  def liftValue[A](a: => A): CallbackOption[A] =
    delay(a)

  @deprecated("Use CallbackOption.option", "2.0.0")
  def liftOption[A](oa: => Option[A]): CallbackOption[A] =
    option(oa)

  @deprecated("Use CallbackOption.maybe", "2.0.0")
  def liftOptionLike[O[_], A](oa: => O[A])(implicit O: OptionLike[O]): CallbackOption[A] =
    maybe(oa)

  @deprecated("Use callback.toCBO", "2.0.0")
  def liftCallback[A](callback: CallbackTo[A]): CallbackOption[A] =
    callback.toCBO

  @deprecated("Use CallbackOption.optionCallback", "2.0.0")
  def liftOptionCallback[A](oc: => Option[CallbackTo[A]]): CallbackOption[A] =
    optionCallback(oc)

  @deprecated("Use CallbackOption.maybeCallback", "2.0.0")
  def liftOptionLikeCallback[O[_], A](oa: => O[CallbackTo[A]])(implicit O: OptionLike[O]): CallbackOption[A] =
    maybeCallback(oa)

  inline def require(inline condition: Boolean): CallbackOption[Unit] =
    CallbackTo(if (condition) someUnit else None).asCBO

  inline def unless(inline condition: Boolean): CallbackOption[Unit] =
    require(!condition)

  inline def matchPF[A, B](inline a: A)(pf: PartialFunction[A, B]): CallbackOption[B] =
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

  def traverse[T[X] <: Iterable[X], A, B](ta: => T[A])(f: A => CallbackOption[B])
                                             (using cbf: BuildFrom[T[A], B, T[B]]): CallbackOption[T[B]] =
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

  def sequence[T[X] <: Iterable[X], A](tca: => T[CallbackOption[A]])
                                          (using cbf: BuildFrom[T[CallbackOption[A]], A, T[A]]): CallbackOption[T[A]] =
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

  inline implicit def toCallback(c: CallbackOption[Unit]): Callback =
    c.toCallback

  inline implicit def fromCallback(c: Callback): CallbackOption[Unit] =
    c.toCBO

  /** Returns the currently focused HTML element (if there is one). */
  lazy val activeHtmlElement: CallbackOption[html.Element] =
    option(
      Option(document.activeElement)
        .flatMap(_.domToHtml)
        .filterNot(_ eq document.body))

  extension (self: CallbackOption[Unit]) {

    @targetName("ext_toCallback")
    inline def toCallback: Callback =
      Callback(self.cbfn())

    def unary_! : CallbackOption[Unit] =
      self.asCallback.map {
        case None    => someUnit
        case Some(_) => None
      }.asCBO
  }
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
 * For a more generic (i.e. beyond Option) or comprehensive monad transformer use Cats or similar.
 */
final class CallbackOption[+A](val underlyingRepr: CallbackOption.UnderlyingRepr[A]) extends AnyVal { self =>
  import self.{underlyingRepr => cbfn}
  import CallbackOption.someUnit

  def getOrElse[AA >: A](default: => AA): CallbackTo[AA] =
    asCallback.map(_ getOrElse default)

  def asCallback: CallbackTo[Option[A]] =
    CallbackTo lift cbfn

  inline def map[B](f: A => B)(using inline ev: MapGuard[B]): CallbackOption[ev.Out] =
    unsafeMap(f)

  private[react] def unsafeMap[B](f: A => B): CallbackOption[B] =
    new CallbackOption(() => cbfn().map(f))

  /**
   * Alias for `map`.
   */
  inline def |>[B](f: A => B)(using inline ev: MapGuard[B]): CallbackOption[ev.Out] =
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
  inline def >>=[B](f: A => CallbackOption[B]): CallbackOption[B] =
    flatMap(f)

  def filter(condition: A => Boolean): CallbackOption[A] =
    new CallbackOption(() => cbfn().filter(condition))

  inline def filterNot(inline condition: A => Boolean): CallbackOption[A] =
    filter(!condition(_))

  inline def withFilter(inline condition: A => Boolean): CallbackOption[A] =
    filter(condition)

  /**
   * Sequence a callback to run after this, discarding any value produced by this.
   */
  def >>[B](next: CallbackOption[B]): CallbackOption[B] =
    flatMap(_ => next)

  /**
   * Sequence a callback to run before this, discarding any value produced by it.
   */
  inline def <<[B](prev: CallbackOption[B]): CallbackOption[A] =
    prev >> this

  /** Convenient version of `<<` that accepts an Option */
  def <<?[B](prev: Option[CallbackOption[B]]): CallbackOption[A] =
    prev.fold(this)(_ >> this)

  /**
   * Alias for `>>`.
   *
   * Where `>>` is often associated with Monads, `*>` is often associated with Applicatives.
   */
  inline def *>[B](next: CallbackOption[B]): CallbackOption[B] =
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
  inline def voidExplicit[B](using inline ev: A <:< B): CallbackOption[Unit] =
    void

  /**
   * Conditional execution of this callback.
   *
   * @param cond The condition required to be `true` for this callback to execute.
   */
  inline def when(inline cond: Boolean): CallbackOption[A] =
    new CallbackOption[A](() => if (cond) cbfn() else None)

  /**
   * Conditional execution of this callback.
   * Reverse of [[when()]].
   *
   * @param cond The condition required to be `false` for this callback to execute.
   * @return `Some` result of the callback executed, else `None`.
   */
  inline def unless(inline cond: Boolean): CallbackOption[A] =
    when(!cond)

  def orElse[AA >: A](tryNext: CallbackOption[AA]): CallbackOption[AA] =
    new CallbackOption(() => cbfn().orElse(tryNext.underlyingRepr()))

  /**
   * Alias for `orElse`.
   */
  inline def |[AA >: A](inline tryNext: CallbackOption[AA]): CallbackOption[AA] =
    orElse(tryNext)

  inline def &&[B](b: CallbackOption[B]): CallbackOption[Unit] =
    this >> b.void

  inline def ||[B](b: CallbackOption[B]): CallbackOption[Unit] =
    void | b.void

  def handleError[AA >: A](f: Throwable => CallbackOption[AA]): CallbackOption[AA] =
    asCallback.handleError(f(_).asCallback).asCBO
}
