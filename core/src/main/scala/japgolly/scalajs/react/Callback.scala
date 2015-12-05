package japgolly.scalajs.react

import org.scalajs.dom.console
import scala.annotation.implicitNotFound
import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import scala.scalajs.js
import js.{undefined, UndefOr, Function0 => JFn0, Function1 => JFn1}
import js.timers.RawTimers

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
   *
   * https://en.wikipedia.org/wiki/Evaluation_strategy#Call_by_name
   */
  @inline def byName(f: => Callback): Callback =
    CallbackTo(f.runNow())

  /**
   * Wraps a [[Future]] so that it is repeatable, and so that its inner callback is run when the future completes.
   *
   * The result is discarded. To retain it, use [[CallbackTo.future)]] instead.
   *
   * WARNING: Futures are scheduled to run as soon as they're created. Ensure that the argument you provide creates a
   * new [[Future]]; don't reference an existing one.
   */
  def future[A](f: => Future[CallbackTo[A]])(implicit ec: ExecutionContext): Callback =
    CallbackTo.future(f).voidExplicit[Future[A]]

  /**
   * Convenience for applying a condition to a callback, and returning `Callback.empty` when the condition isn't
   * satisfied.
   */
  def ifTrue(pred: Boolean, c: => Callback): Callback =
    if (pred) c else Callback.empty

  def traverse[T[X] <: TraversableOnce[X], A](ta: => T[A])(f: A => Callback): Callback =
    Callback(
      ta.foreach(a =>
        f(a).runNow()))

  @inline def sequence[T[X] <: TraversableOnce[X]](tca: => T[Callback]): Callback =
    traverse(tca)(identity)

  def traverseO[A](oa: => Option[A])(f: A => Callback): Callback =
    Callback(
      oa.foreach(a =>
        f(a).runNow()))

  @inline def sequenceO[A](oca: => Option[Callback]): Callback =
    traverseO(oca)(identity)

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
   * Convenience for calling `dom.alert`.
   */
  def alert(message: String): Callback =
    Callback(org.scalajs.dom.alert(message))

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
   *
   * https://en.wikipedia.org/wiki/Evaluation_strategy#Call_by_name
   */
  @inline def byName[A](f: => CallbackTo[A]): CallbackTo[A] =
    CallbackTo(f.runNow())

  def traverse[T[X] <: TraversableOnce[X], A, B](ta: => T[A])(f: A => CallbackTo[B])
                                                (implicit cbf: CanBuildFrom[T[A], B, T[B]]): CallbackTo[T[B]] =
    CallbackTo {
      val r = cbf(ta)
      ta.foreach(a => r += f(a).runNow())
      r.result()
    }

  @inline def sequence[T[X] <: TraversableOnce[X], A](tca: => T[CallbackTo[A]])
                                                     (implicit cbf: CanBuildFrom[T[CallbackTo[A]], A, T[A]]): CallbackTo[T[A]] =
    traverse(tca)(identity)

  def traverseO[A, B](oa: => Option[A])(f: A => CallbackTo[B]): CallbackTo[Option[B]] =
    CallbackTo(oa.map(f(_).runNow()))

  @inline def sequenceO[A](oca: => Option[CallbackTo[A]]): CallbackTo[Option[A]] =
    traverseO(oca)(identity)

  /**
   * Wraps a [[Future]] so that it is repeatable, and so that its inner callback is run when the future completes.
   *
   * WARNING: Futures are scheduled to run as soon as they're created. Ensure that the argument you provide creates a
   * new [[Future]]; don't reference an existing one.
   */
  def future[A](f: => Future[CallbackTo[A]])(implicit ec: ExecutionContext): CallbackTo[Future[A]] =
    CallbackTo(f.map(_.runNow()))

  /**
   * Serves as a temporary placeholder for a callback until you supply a real implementation.
   *
   * Unlike `???` this doesn't crash, it just prints a warning to the console.
   *
   * Also it's not really deprecated; that's just so you get a compiler warning as a reminder.
   */
  @deprecated("", "not really deprecated")
  def TODO[A](result: => A): CallbackTo[A] =
    Callback.todoImpl(None) >> CallbackTo(result)

  /**
   * Serves as a temporary placeholder for a callback until you supply a real implementation.
   *
   * Unlike `???` this doesn't crash, it just prints a warning to the console.
   *
   * Also it's not really deprecated; that's just so you get a compiler warning as a reminder.
   */
  @deprecated("", "not really deprecated")
  def TODO[A](result: => A, reason: => String): CallbackTo[A] =
    Callback.todoImpl(Some(() => reason)) >> CallbackTo(result)

  final class ReactExt_CallbackToFuture[A](private val _c: () => Future[A]) extends AnyVal {
    @inline private def c = new CallbackTo(_c)

    /**
     * Turns a `CallbackTo[Future[A]]` into a `Future[A]`.
     *
     * WARNING: This will trigger the execution of the [[Callback]].
     */
    def toFlatFuture(implicit ec: ExecutionContext): Future[A] =
      c.toFuture.flatMap(identity)
  }
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

  def flatten[B](implicit ev: A => CallbackTo[B]): CallbackTo[B] =
    flatMap(ev)

  def flatMap2[X, Y, Z](f: (X, Y) => CallbackTo[Z])(implicit ev: A =:= (X, Y)): CallbackTo[Z] =
    flatMap(f tupled _)

  /**
   * Sequence a callback to run after this, discarding any value produced by this.
   */
  def >>[B](runNext: CallbackTo[B]): CallbackTo[B] =
    //if (isEmpty_?) runNext else
    //flatMap(_ => runNext)
    new CallbackTo(() => {f(); runNext.f()})

  /**
   * Alias for `>>`.
   *
   * Where `>>` is often associated with Monads, `*>` is often associated with Applicatives.
   */
  @inline def *>[B](runNext: CallbackTo[B]): CallbackTo[B] =
    >>(runNext)

  /**
   * Sequence a callback to run before this, discarding any value produced by it.
   */
  @inline def <<[B](runBefore: CallbackTo[B]): CallbackTo[A] =
    runBefore >> this

  def zip[B](cb: CallbackTo[B]): CallbackTo[(A, B)] =
    for {
      a <- this
      b <- cb
    } yield (a, b)

  /**
   * Discard the callback's return value, return a given value instead.
   *
   * `ret`, short for `return`.
   */
  def ret[B](b: B): CallbackTo[B] =
    map(_ => b)

  /**
   * Discard the value produced by this callback.
   */
  def void: Callback =
    ret(())

  /**
   * Discard the value produced by this callback.
   *
   * This method allows you to be explicit about the type you're discarding (which may change in future).
   */
  @inline def voidExplicit[B](implicit ev: A =:= B): Callback =
    void

  def conditionally(cond: => Boolean): CallbackTo[Option[A]] =
    CallbackTo(if (cond) Some(f()) else None)

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
   * Wraps this callback in a `try-finally` block and runs the given callback in the `finally` clause, after the
   * current callback completes, be it in error or success.
   */
  def finallyRun[B](runFinally: CallbackTo[B]): CallbackTo[A] =
    CallbackTo(try f() finally runFinally.runNow())

  /**
   * When the callback result becomes available, perform a given side-effect with it.
   */
  def tap(t: A => Any): CallbackTo[A] =
    flatTap(a => CallbackTo(t(a)))

  /**
   * Alias for `tap`.
   */
  @inline def <|(t: A => Any): CallbackTo[A] =
    tap(t)

  def flatTap[B](t: A => CallbackTo[B]): CallbackTo[A] =
    for {
      a <- this
      _ <- t(a)
    } yield a

  /**
   * Sequence actions, discarding the value of the second argument.
   */
  def <*[B](next: CallbackTo[B]): CallbackTo[A] =
    flatTap(_ => next)

  @inline def toScalaFn: () => A =
    f

  def toJsFn: JFn0[A] =
    f

  def toJsFn1: JFn1[Any, A] =
    (_: Any) => f()

  def toJsCallback: UndefOr[JFn0[A]] =
    if (isEmpty_?) undefined else toJsFn

  def isEmpty_? : Boolean =
    f eq Callback.empty.f

  /**
   * Log to the console before this callback starts, and after it completes.
   *
   * Does not change the result.
   */
  def logAround(message: js.Any, optionalParams: js.Any*): CallbackTo[A] = {
    def log(prefix: String) = Callback.log(prefix + message.toString, optionalParams: _*)
    log("→  Starting: ") *> this <* log(" ← Finished: ")
  }

  /**
   * Logs the result of this callback as it completes.
   */
  def logResult(msg: A => String): CallbackTo[A] =
    flatTap(a => Callback.log(msg(a)))

  /**
   * Logs the result of this callback as it completes.
   *
   * @param name Prefix to appear the log output.
   */
  def logResult(name: String): CallbackTo[A] =
    logResult(a => s"$name: $a")

  /**
   * Logs the result of this callback as it completes.
   */
  def logResult: CallbackTo[A] =
    logResult(_.toString)

  /**
   * Run asynchronously.
   */
  def async: CallbackTo[Future[A]] =
    delayMs(4) // 4ms is minimum allowed by setTimeout spec

  /**
   * Run asynchronously after a delay of a given duration.
   */
  def delay(startIn: FiniteDuration): CallbackTo[Future[A]] =
    delayMs(startIn.toMillis.toDouble)

  /**
   * Run asynchronously after a `startInMilliseconds` ms delay.
   */
  def delayMs(startInMilliseconds: Double): CallbackTo[Future[A]] =
    CallbackTo {
      val p = Promise[A]()
      val cb = this.attempt.map[Unit] {
        case Right(a) => p.success(a)
        case Left(e)  => p.failure(e)
      }
      RawTimers.setTimeout(cb.toJsFn, startInMilliseconds)
      p.future
    }

  /**
   * Schedules an instance of this callback to run asynchronously.
   */
  def toFuture(implicit ec: ExecutionContext): Future[A] =
    Future(runNow())

  /**
   * Record the duration of this callback's execution.
   */
  def withDuration[B](f: (A, FiniteDuration) => CallbackTo[B]): CallbackTo[B] = {
    @inline def nowMS: Long = System.currentTimeMillis()
    CallbackTo {
      val s = nowMS
      val a = runNow()
      val e = nowMS
      val d = FiniteDuration(e - s, MILLISECONDS)
      f(a, d).runNow()
    }
  }

  /**
   * Log the duration of this callback's execution.
   */
  def logDuration(fmt: FiniteDuration => String): CallbackTo[A] =
    withDuration((a, d) =>
      Callback.log(fmt(d)) ret a)

  /**
   * Log the duration of this callback's execution.
   *
   * @param name Prefix to appear the log output.
   */
  def logDuration(name: String): CallbackTo[A] =
    logDuration(d => s"$name completed in $d.")

  /**
   * Log the duration of this callback's execution.
   */
  def logDuration: CallbackTo[A] =
    logDuration("Callback")

  def asCBO[B](implicit ev: This =:= CallbackTo[Option[B]]): CallbackOption[B] =
    CallbackOption(ev(this))

  def toCBO: CallbackOption[A] =
    CallbackOption liftCallback this

  // -------------------------------------------------------------------------------------------------------------------
  // Boolean ops

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
   * Negates the callback result (so long as it's boolean).
   */
  def !(implicit ev: ThisIsBool): CallbackB =
    ev(this).map(!_)
}
