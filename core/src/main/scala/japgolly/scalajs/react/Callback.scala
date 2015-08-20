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
      @inline implicit def allowAnythingElse[A]: Proof[A] = null.asInstanceOf[Proof[A]]
    }
    @inline implicit def apply[A: Proof]: ResultGuard[A] = null.asInstanceOf[ResultGuard[A]]
  }

  @inline def apply[U: ResultGuard](f: => U): Callback =
    CallbackTo(f: Unit)

  @inline def lift(f: () => Unit): Callback =
    CallbackTo lift f

  @inline def lazily(f: => Callback): Callback =
    CallbackTo(f.runNow())

  /** A callback that does nothing. */
  val empty: Callback =
    CallbackTo.pure(())

  def log(message: js.Any, optionalParams: js.Any*): Callback =
    Callback(console.log(message, optionalParams: _*))

  def info(message: js.Any, optionalParams: js.Any*): Callback =
    Callback(console.info(message, optionalParams: _*))

  def warn(message: js.Any, optionalParams: js.Any*): Callback =
    Callback(console.warn(message, optionalParams: _*))

  def assert(test: Boolean, message: String, optionalParams: js.Any*): Callback =
    Callback(console.assert(test, message, optionalParams: _*))
}

object CallbackTo {
  @inline def apply[A](f: => A): CallbackTo[A] =
    new CallbackTo(() => f)

  @inline def lift[A](f: () => A): CallbackTo[A] =
    new CallbackTo(f)

  @inline def lazily[A](f: => CallbackTo[A]): CallbackTo[A] =
    CallbackTo(f.runNow())

  @inline def pure[A](a: A): CallbackTo[A] =
    new CallbackTo(() => a)
}

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

  @inline def toScalaFunction: () => A =
    f

  def toJsFunction: JFn0[A] =
    f

  def toJsFunction1: JFn1[Any, A] =
    (_: Any) => f()

  def toJsCallback: UndefOr[JFn0[A]] =
    unlessEmpty(toJsFunction)

  def isEmpty_? : Boolean =
    f eq Callback.empty.f

  def unlessEmpty[B](b: => B): UndefOr[B] =
    if (isEmpty_?)
      undefined
    else
      b

  type TypeEv[T] = CallbackTo[A] =:= CallbackTo[T]

  private def bool2(b: CallbackTo[Boolean])(op: (() => Boolean, () => Boolean) => Boolean)(implicit ev: TypeEv[Boolean]): CallbackTo[Boolean] = {
    val x = ev(this).f
    val y = b.f
    CallbackTo(op(x, y))
  }

  /**
   * Creates a new callback that returns `true` when both this and the given callback return `true`.
   */
  def &&(b: CallbackTo[Boolean])(implicit ev: TypeEv[Boolean]): CallbackTo[Boolean] =
    bool2(b)(_() && _())

  /**
   * Creates a new callback that returns `true` when either this or the given callback return `true`.
   */
  def ||(b: CallbackTo[Boolean])(implicit ev: TypeEv[Boolean]): CallbackTo[Boolean] =
    bool2(b)(_() || _())

  /**
   * Negates the callback result (so long as its boolean).
   */
  def !(implicit ev: A =:= Boolean): CallbackTo[Boolean] =
    map(!_)
}
