package japgolly.scalajs.react.callback

import japgolly.scalajs.react.util.Util.identityFn
import org.scalajs.dom.{console, window}
import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.timers.{RawTimers, SetIntervalHandle, SetTimeoutHandle}
import scala.util.{Failure, Success}

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
    CallbackTo.lift(f)

  /** A callback that does nothing. */
  val empty: Callback =
    new CallbackTo(Trampoline.unit)

  @deprecated("use throwException", "1.6.1")
  @inline def error(t: Throwable): Callback =
    CallbackTo.throwException(t)

  @inline def throwException(t: Throwable): Callback =
    CallbackTo.throwException(t)

  @inline def fromJsFn(f: js.Function0[Unit]): Callback =
    CallbackTo.fromJsFn(f)

  /**
   * Callback that isn't created until the first time it is used, after which it is reused.
   */
  @inline def lazily(f: => Callback): Callback =
    CallbackTo.lazily(f)

  /** Callback that is recreated each time it is used. */
  @inline def suspend(f: => Callback): Callback =
    CallbackTo.suspend(f)

  /** Callback that is recreated each time it is used.
    *
    * https://en.wikipedia.org/wiki/Evaluation_strategy#Call_by_name
    */
  @deprecated("Use Callback.suspend", "2.0.0")
  @inline def byName(f: => Callback): Callback =
    suspend(f)

  /**
   * Wraps a [[Future]] so that it is repeatable, and so that its inner callback is run when the future completes.
   *
   * The result is discarded. To retain it, use [[CallbackTo.future]] instead.
   *
   * Because the `Future` is discarded, when an exception causes it to fail, the exception is re-thrown.
   * If you want the exception to be ignored or handled differently, use [[CallbackTo.future]] instead and then
   * `.void` to discard the future and turn the result into a `Callback`.
   *
   * WARNING: Futures are scheduled to run as soon as they're created. Ensure that the argument you provide creates a
   * new [[Future]]; don't reference an existing one.
   */
  def future[A](f: => Future[CallbackTo[A]])(implicit ec: ExecutionContext): Callback =
    CallbackTo(f.onComplete {
      case Success(cb) => cb.runNow()
      case Failure(t)  => throw t
    })

  /**
   * Convenience for applying a condition to a callback, and returning `Callback.empty` when the condition isn't
   * satisfied.
   *
   * Notice the condition is strict. If non-strictness is desired use `callback.when(cond)`.
   *
   * @param cond The condition required to be `true` for the callback to execute.
   */
  def when[A](cond: Boolean)(c: => CallbackTo[A]): Callback =
    if (cond) c.void else Callback.empty

  /**
   * Convenience for applying a condition to a callback, and returning `Callback.empty` when the condition is already
   * satisfied.
   *
   * Notice the condition is strict. If non-strictness is desired use `callback.unless(cond)`.
   *
   * @param cond The condition required to be `false` for the callback to execute.
   */
  def unless[A](cond: Boolean)(c: => CallbackTo[A]): Callback =
    when(!cond)(c)

  def traverse[T[X] <: Iterable[X], A, B](ta: => T[A])(f: A => CallbackTo[B]): Callback =
    Callback(
      ta.iterator.foreach(a =>
        f(a).runNow()))

  def sequence[T[X] <: Iterable[X], A](tca: => T[CallbackTo[A]]): Callback =
    traverse(tca)(identityFn)

  def traverseOption[A, B](oa: => Option[A])(f: A => CallbackTo[B]): Callback =
    Callback(
      oa.foreach(a =>
        f(a).runNow()))

  def sequenceOption[A](oca: => Option[CallbackTo[A]]): Callback =
    traverseOption(oca)(identityFn)

  /** Run all given callbacks.
    *
    * All results are discarded.
    * Any exceptions get a `printStackTrace` and are then discarded, and the next callback run.
    *
    * @since 2.0.0
    */
  def runAll(callbacks: CallbackTo[Any]*): Callback =
    callbacks.foldLeft(empty)((x, y) => x >> y.reset)

  /**
   * Convenience for calling `dom.console.log`.
   */
  def log(message: Any, optionalParams: Any*): Callback =
    Callback(console.log(message, optionalParams: _*))

  /**
   * Convenience for calling `dom.console.info`.
   */
  def info(message: Any, optionalParams: Any*): Callback =
    Callback(console.info(message, optionalParams: _*))

  /**
   * Convenience for calling `dom.console.warn`.
   */
  def warn(message: Any, optionalParams: Any*): Callback =
    Callback(console.warn(message, optionalParams: _*))

  /**
   * Convenience for calling `dom.console.assert`.
   */
  def assert(test: Boolean, message: String, optionalParams: Any*): Callback =
    Callback(console.assert(test, message, optionalParams: _*))

  /**
   * Convenience for calling `dom.alert`.
   */
  def alert(message: String): Callback =
    Callback(window.alert(message))

  /**
   * Serves as a temporary placeholder for a callback until you supply a real implementation.
   *
   * Unlike `???` this doesn't crash, it just prints a warning to the console.
   *
   * Also it's not really deprecated; that's just so you get a compiler warning as a reminder.
   */
  @deprecated("", "not really deprecated")
  def TODO: Callback =
    todoImpl(None)

  /**
   * Serves as a temporary placeholder for a callback until you supply a real implementation.
   *
   * Unlike `???` this doesn't crash, it just prints a warning to the console.
   *
   * Also it's not really deprecated; that's just so you get a compiler warning as a reminder.
   */
  @deprecated("", "not really deprecated")
  def TODO(reason: => String): Callback =
    todoImpl(Some(() => reason))

  private[react] def todoImpl(reason: Option[() => String]): Callback =
    suspend(warn("TODO" + reason.fold("")(": " + _())))

  final class SetIntervalResult(val handle: SetIntervalHandle) {
    val cancel: Callback = Callback { RawTimers.clearInterval(handle) }
  }

  final class SetTimeoutResult(val handle: SetTimeoutHandle) {
    val cancel: Callback = Callback { RawTimers.clearTimeout(handle) }
  }
}
