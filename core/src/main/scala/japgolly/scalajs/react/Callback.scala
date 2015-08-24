package japgolly.scalajs.react

import org.scalajs.dom.console
import scala.annotation.implicitNotFound
import scala.scalajs.js
import js.{undefined, UndefOr, Function0 => JFn0, Function1 => JFn1}

/**
 * A callback with no return value. Equivalent to `() => Unit`.
 *
 * @see CallbackTo
 */
object Callback {
  @implicitNotFound("You're wrapping a ${A} in a Callback which will discard without running it. Instead use CallbackTo(…).flatten or Callback{,To}.lazily(…).")
  final class ResultGuard[A] private[Callback]()
  object ResultGuard {
    final class Proof[A] private[Callback]()
    object Proof {
      implicit def preventCallback1[A]: Proof[CallbackTo[A]] = ???
      implicit def preventCallback2[A]: Proof[CallbackTo[A]] = ???
      @inline implicit def allowAnythingElse[A]: Proof[A] = null
    }
    @inline implicit def apply[A: Proof]: ResultGuard[A] = null
  }

  @inline def apply[U: ResultGuard](f: => U): Callback =
    CallbackTo(f: Unit)

  @inline def lift(f: () => Unit): Callback =
    CallbackTo lift f

  /** A callback that does nothing. */
  val empty: Callback =
    CallbackTo.pure(())

  /**
   * Callback that isn't created until the first time it is used, after which it is reused.
   */
  def lazily(f: => Callback): Callback = {
    lazy val g = f
    byName(g)
  }

  /**
   * Callback that is recreated each time it is used.
   */
  @inline def byName(f: => Callback): Callback =
    CallbackTo(f.runNow())

  /**
   * Convenience for returning `Callback.empty` if a condition isn't satisfied.
   */
  def ifTrue(pred: Boolean, c: => Callback): Callback =
    if (pred) c else Callback.empty

  /**
   * Convenience for calling `dom.console.log`.
   */
  def log(message: js.Any, optionalParams: js.Any*): Callback =
    Callback(console.log(message, optionalParams: _*))

  /**
   * Convenience for calling `dom.console.info`.
   */
  def info(message: js.Any, optionalParams: js.Any*): Callback =
    Callback(console.info(message, optionalParams: _*))

  /**
   * Convenience for calling `dom.console.warn`.
   */
  def warn(message: js.Any, optionalParams: js.Any*): Callback =
    Callback(console.warn(message, optionalParams: _*))

  /**
   * Convenience for calling `dom.console.assert`.
   */
  def assert(test: Boolean, message: String, optionalParams: js.Any*): Callback =
    Callback(console.assert(test, message, optionalParams: _*))

  /**
   * Serves as a temporary placeholder for a callback until you supply a real implementation.
   *
   * Unlike `???` this doesn't crash, it just prints a warning to the console.
   *
   * Also it's not really deprecated, that's just so you get a compiler warning to remind you.
   */
  @deprecated("", "")
  def TODO: Callback =
    todoImpl(None)

  /**
   * Serves as a temporary placeholder for a callback until you supply a real implementation.
   *
   * Unlike `???` this doesn't crash, it just prints a warning to the console.
   *
   * Also it's not really deprecated, that's just so you get a compiler warning to remind you.
   */
  @deprecated("", "")
  def TODO(reason: => String): Callback =
    todoImpl(Some(() => reason))

  private[react] def todoImpl(reason: Option[() => String]): Callback =
    byName(warn("TODO" + reason.fold("")(": " + _())))
}

// =====================================================================================================================

object CallbackTo {
  @inline def apply[A](f: => A): CallbackTo[A] =
    new CallbackTo(() => f)

  @inline def lift[A](f: () => A): CallbackTo[A] =
    new CallbackTo(f)

  @inline def pure[A](a: A): CallbackTo[A] =
    new CallbackTo(() => a)

  /**
   * Callback that isn't created until the first time it is used, after which it is reused.
   */
  def lazily[A](f: => CallbackTo[A]): CallbackTo[A] = {
    lazy val g = f
    byName(g)
  }

  /**
   * Callback that is recreated each time it is used.
   */
  @inline def byName[A](f: => CallbackTo[A]): CallbackTo[A] =
    CallbackTo(f.runNow())

  /**
   * Serves as a temporary placeholder for a callback until you supply a real implementation.
   *
   * Unlike `???` this doesn't crash, it just prints a warning to the console.
   *
   * Also it's not really deprecated, that's just so you get a compiler warning to remind you.
   */
  @deprecated("", "")
  def TODO[A](result: => A): CallbackTo[A] =
    Callback.todoImpl(None) >> CallbackTo(result)

  /**
   * Serves as a temporary placeholder for a callback until you supply a real implementation.
   *
   * Unlike `???` this doesn't crash, it just prints a warning to the console.
   *
   * Also it's not really deprecated, that's just so you get a compiler warning to remind you.
   */
  @deprecated("", "")
  def TODO[A](result: => A, reason: => String): CallbackTo[A] =
    Callback.todoImpl(Some(() => reason)) >> CallbackTo(result)
}

// =====================================================================================================================

/**
 * A function to be executed later, usually by scalajs-react in response to some kind of event.
 *
 * The purpose of this class is to lift effects into the type system, and use the compiler to ensure safety around
 * callbacks (without depending on an external library like Scalaz).
 *
 * `() => Unit` is replaced by `Callback`.
 * Similarly, `ReactEvent => Unit` is replaced  by `ReactEvent => Callback`.
 *
 * @tparam A The type of result produced when the callback is invoked.
 *
 * @since 0.10.0
 */
final class CallbackTo[A] private[react] (private[CallbackTo] val f: () => A) extends AnyVal {
  type This = CallbackTo[A]

  /**
   * Executes this callback, on the current thread, right now, blocking until complete.
   *
   * In most cases, this type is passed to scalajs-react such that you don't need to call this method yourself.
   *
   * Exceptions will not be caught. Use [[attempt]] to catch thrown exceptions.
   */
  @inline def runNow(): A =
    f()

  def map[B](g: A => B): CallbackTo[B] =
    new CallbackTo(() => g(f()))

  /**
   * Alias for `map`.
   */
  @inline def |>[B](g: A => B): CallbackTo[B] =
    map(g)

  def flatMap[B](g: A => CallbackTo[B]): CallbackTo[B] =
    new CallbackTo(() => g(f()).f())

  def flatten[B](implicit ev: A =:= CallbackTo[B]): CallbackTo[B] =
    flatMap(ev)

  /**
   * Alias for `flatMap`.
   */
  @inline def >>=[B](g: A => CallbackTo[B]): CallbackTo[B] =
    flatMap(g)

  /**
   * Same as `flatMap` and `>>=`, but allows arguments to appear in reverse order.
   *
   * i.e. `f >>= g` is the same as `g =<<: f`
   */
  @inline def =<<:[B](g: A => CallbackTo[B]): CallbackTo[B] =
    flatMap(g)

  /**
   * Sequence a callback to run after this, discarding any value produced by this.
   */
  def >>[B](runNext: CallbackTo[B]): CallbackTo[B] =
    //if (isEmpty_?) runNext else
    //flatMap(_ => runNext)
    new CallbackTo(() => {f(); runNext.f()})

  /**
   * Sequence a callback to run before this, discarding any value produced by it.
   */
  @inline def <<[B](runBefore: CallbackTo[B]): CallbackTo[A] =
    runBefore >> this

  /**
   * Discard the value produced by this callback.
   */
  def void: Callback =
    map(_ => ())

  def conditionally(cond: => Boolean): CallbackTo[UndefOr[A]] =
    CallbackTo(if (cond) f() else undefined)

  /**
   * Wraps this callback in a try-catch and returns either the result or the exception if one occurs.
   */
  def attempt: CallbackTo[Either[Throwable, A]] =
    CallbackTo(
      try Right(f())
      catch { case t: Throwable => Left(t) }
    )

  /**
   * Convenience-method to run additional code after this callback.
   */
  def thenRun[B](runNext: => B): CallbackTo[B] =
    this >> CallbackTo(runNext)

  /**
   * Convenience-method to run additional code before this callback.
   */
  def precedeWith(runFirst: => Unit): CallbackTo[A] =
    this << Callback(runFirst)

  /**
   * Wraps this callback in a try-finally block such that given code runs after the callback completes, be it in error
   * or success.
   */
  def finallyRun(runFinally: => Unit): CallbackTo[A] =
    CallbackTo(try f() finally runFinally)

  /**
   * When the callback result becomes available, perform a given side-effect with it.
   */
  def tap(t: A => Any): CallbackTo[A] =
    CallbackTo {
      val a = f()
      t(a)
      a
    }

  /**
   * Alias for `tap`.
   */
  @inline def <|(t: A => Any): CallbackTo[A] =
    tap(t)

  def flatTap[B](t: A => CallbackTo[B]): CallbackTo[A] =
    tap(t(_).runNow())

  @inline def toScalaFunction: () => A =
    f

  def toJsFunction: JFn0[A] =
    f

  def toJsFunction1: JFn1[Any, A] =
    (_: Any) => f()

  def toJsCallback: UndefOr[JFn0[A]] =
    if (isEmpty_?) undefined else toJsFunction

  def isEmpty_? : Boolean =
    f eq Callback.empty.f

  def flatMapUnlessEmpty(g: Callback => Callback)(implicit ev: This =:= Callback): Callback =
    if (isEmpty_?) this else g(this)

  // -------------------------------------------------------------------------------------------------------------------
  // Boolean fns

  type ThisIsBool = This =:= CallbackB

  private def bool2(b: CallbackB)(op: (() => Boolean, () => Boolean) => Boolean)(implicit ev: ThisIsBool): CallbackB = {
    val x = ev(this).f
    val y = b.f
    CallbackTo(op(x, y))
  }

  /**
   * Creates a new callback that returns `true` when both this and the given callback return `true`.
   */
  def &&(b: CallbackB)(implicit ev: ThisIsBool): CallbackB =
    bool2(b)(_() && _())

  /**
   * Creates a new callback that returns `true` when either this or the given callback return `true`.
   */
  def ||(b: CallbackB)(implicit ev: ThisIsBool): CallbackB =
    bool2(b)(_() || _())

  /**
   * Negates the callback result (so long as its boolean).
   */
  def !(implicit ev: ThisIsBool): CallbackB =
    ev(this).map(!_)

  /**
   * Sequence the given callback to be run when the result of this is `true`.
   * The result is discarded.
   */
  def whenTrueRun[B](c: CallbackTo[B])(implicit ev: ThisIsBool): Callback =
    ev(this).map(a => if (a) c.runNow())

  /**
   * Alias for `whenTrueRun`.
   */
  @inline def ?>>[B](c: CallbackTo[B])(implicit ev: ThisIsBool): Callback =
    whenTrueRun(c)

  /**
   * Sequence the given callback to be run when the result of this is `true`.
   * Collects the result and wraps it in `Option`.
   */
  def whenTrue[B](c: CallbackTo[B])(implicit ev: ThisIsBool): CallbackTo[Option[B]] =
    ev(this).map(a => if (a) Some(c.runNow()) else None)

  /**
   * Alias for `whenTrue`.
   */
  @inline def ?>>?[B](c: CallbackTo[B])(implicit ev: ThisIsBool): CallbackTo[Option[B]] =
    whenTrue(c)
}
