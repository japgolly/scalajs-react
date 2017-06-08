package japgolly.scalajs.react

import org.scalajs.dom.{console, window}
import scala.annotation.{tailrec, implicitNotFound}
import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import scala.scalajs.js
import scala.scalajs.js.{undefined, UndefOr, Function0 => JFn0, Function1 => JFn1}
import scala.scalajs.js.timers.RawTimers
import scala.util.{Try, Failure, Success}
import japgolly.scalajs.react.internal.identityFn
import CallbackTo.MapGuard

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

  def apply[U: ResultGuard](f: => U): Callback =
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
  def byName(f: => Callback): Callback =
    CallbackTo(f.runNow())

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
   */
  @deprecated("Use when() or unless().", "0.11.0")
  def ifTrue(pred: Boolean, c: => Callback): Callback =
    when(pred)(c)

  /**
   * Convenience for applying a condition to a callback, and returning `Callback.empty` when the condition isn't
   * satisfied.
   *
   * Notice the condition is strict. If non-strictness is desired use `callback.when(cond)`.
   *
   * @param cond The condition required to be `true` for the callback to execute.
   */
  def when(cond: => Boolean)(c: => Callback): Callback =
    if (cond) c else Callback.empty

  /**
   * Convenience for applying a condition to a callback, and returning `Callback.empty` when the condition is already
   * satisfied.
   *
   * Notice the condition is strict. If non-strictness is desired use `callback.unless(cond)`.
   *
   * @param cond The condition required to be `false` for the callback to execute.
   */
  def unless(cond: Boolean)(c: => Callback): Callback =
    when(!cond)(c)

  def traverse[T[X] <: TraversableOnce[X], A](ta: => T[A])(f: A => Callback): Callback =
    Callback(
      ta.foreach(a =>
        f(a).runNow()))

  def sequence[T[X] <: TraversableOnce[X]](tca: => T[Callback]): Callback =
    traverse(tca)(identityFn)

  @deprecated("Use .traverseOption", "1.0.0")
  def traverseO[A](oa: => Option[A])(f: A => Callback): Callback = traverseOption(oa)(f)

  @deprecated("Use .sequenceOption", "1.0.0")
  def sequenceO[A](oca: => Option[Callback]): Callback = sequenceOption(oca)

  def traverseOption[A](oa: => Option[A])(f: A => Callback): Callback =
    Callback(
      oa.foreach(a =>
        f(a).runNow()))

  def sequenceOption[A](oca: => Option[Callback]): Callback =
    traverseOption(oca)(identityFn)

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
    byName(warn("TODO" + reason.fold("")(": " + _())))
}

// =====================================================================================================================

object CallbackTo {
  def apply[A](f: => A): CallbackTo[A] =
    new CallbackTo(() => f)

  def lift[A](f: () => A): CallbackTo[A] =
    new CallbackTo(f)

  def pure[A](a: A): CallbackTo[A] =
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
  def byName[A](f: => CallbackTo[A]): CallbackTo[A] =
    CallbackTo(f.runNow())

  /**
   * Tail-recursive callback. Uses constant stack space.
   *
   * Based on Phil Freeman's work on stack safety in PureScript, described in
   * [[http://functorial.com/stack-safety-for-free/index.pdf Stack Safety for
   * Free]].
   */
  def tailrec[A, B](a: A)(f: A => CallbackTo[Either[A, B]]): CallbackTo[B] =
    CallbackTo {
      @tailrec
      def go(a: A): B =
        f(a).runNow() match {
          case Left(n)  => go(n)
          case Right(b) => b
        }
      go(a)
    }

  def liftTraverse[A, B](f: A => CallbackTo[B]): LiftTraverseDsl[A, B] =
    new LiftTraverseDsl(f)

  final class LiftTraverseDsl[A, B](private val f: A => CallbackTo[B]) extends AnyVal {

    /** See [[CallbackTo.distFn]] for the dual. */
    def id: CallbackTo[A => B] =
      CallbackTo(f(_).runNow())

    /** Anything traversable by the Scala stdlib definition */
    def std[T[X] <: TraversableOnce[X]](implicit cbf: CanBuildFrom[T[A], B, T[B]]): CallbackTo[T[A] => T[B]] =
      CallbackTo { ta =>
        val r = cbf(ta)
        ta.foreach(a => r += f(a).runNow())
        r.result()
      }

    def option: CallbackTo[Option[A] => Option[B]] =
      CallbackTo(_.map(f(_).runNow()))
  }

  def traverse[T[X] <: TraversableOnce[X], A, B](ta: => T[A])(f: A => CallbackTo[B])(implicit cbf: CanBuildFrom[T[A], B, T[B]]): CallbackTo[T[B]] =
    liftTraverse(f).std[T](cbf).map(_(ta))

  def sequence[T[X] <: TraversableOnce[X], A](tca: => T[CallbackTo[A]])(implicit cbf: CanBuildFrom[T[CallbackTo[A]], A, T[A]]): CallbackTo[T[A]] =
    traverse(tca)(identityFn)(cbf)

  @deprecated("Use .traverseOption", "1.0.0")
  def traverseO[A, B](oa: => Option[A])(f: A => CallbackTo[B]): CallbackTo[Option[B]] = traverseOption(oa)(f)

  @deprecated("Use .sequenceOption", "1.0.0")
  def sequenceO[A](oca: => Option[CallbackTo[A]]): CallbackTo[Option[A]] = sequenceOption(oca)

  def traverseOption[A, B](oa: => Option[A])(f: A => CallbackTo[B]): CallbackTo[Option[B]] =
    liftTraverse(f).option.map(_(oa))

  def sequenceOption[A](oca: => Option[CallbackTo[A]]): CallbackTo[Option[A]] =
    traverseOption(oca)(identityFn)

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
    private def c = new CallbackTo(_c)

    /**
     * Turns a `CallbackTo[Future[A]]` into a `Future[A]`.
     *
     * WARNING: This will trigger the execution of the [[Callback]].
     */
    def toFlatFuture(implicit ec: ExecutionContext): Future[A] =
      c.toFuture.flatMap(identityFn)
  }

  @inline implicit def callbackCovariance[A, B >: A](c: CallbackTo[A]): CallbackTo[B] =
    c.widen

  /**
   * Prevents `scalac` discarding the result of a map function when the final result is `Callback`.
   *
   * See https://github.com/japgolly/scalajs-react/issues/256
   *
   * @since 0.11.0
   */
  sealed trait MapGuard[A] { type Out = A }

  @inline implicit def MapGuard[A]: MapGuard[A] =
    null.asInstanceOf[MapGuard[A]]
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

  /**
    * Executes this callback, on the current thread, right now, blocking until complete.
    * Exceptions will not be thrown, not caught. Use [[CallbackTo#attempt]] to catch exceptions.
    *
    * Typically, callbacks are passed to scalajs-react and you're expected not to call this method yourself.
    * Generally speaking, the only time you should call this method is in some other non-React code's async callback.
    * Inside an AJAX callback is a common example. Even for those cases though, you can avoid calling this by getting
    * direct access instead of callback-based access to your component;
    * see the online WebSockets example: https://japgolly.github.io/scalajs-react/#examples/websockets
    *
    * While it's technically safe to call [[CallbackTo#runNow]] inside the body of another [[Callback]], it's better
    * to just use combinators like [[CallbackTo#flatMap]] or even a Scala for-comprehension.
    */
  @inline def runNow(): A =
    f()

  def widen[B >: A]: CallbackTo[B] =
    new CallbackTo(f)

  def map[B](g: A => B)(implicit ev: MapGuard[B]): CallbackTo[ev.Out] =
    new CallbackTo(() => g(f()))

  /**
   * Alias for `map`.
   */
  @inline def |>[B](g: A => B)(implicit ev: MapGuard[B]): CallbackTo[ev.Out] =
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

  def flatMap2[X, Y, Z](f: (X, Y) => CallbackTo[Z])(implicit ev: A <:< (X, Y)): CallbackTo[Z] =
    flatMap(f tupled _)

  /**
   * Sequence a callback to run after this, discarding any value produced by this.
   */
  def >>[B](runNext: CallbackTo[B]): CallbackTo[B] =
    if (isEmpty_?)
      runNext
    else
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
    this >> CallbackTo.pure(b)

  /**
   * Discard the value produced by this callback.
   */
  def void: Callback =
    this >> Callback.empty

  /**
   * Discard the value produced by this callback.
   *
   * This method allows you to be explicit about the type you're discarding (which may change in future).
   */
  @inline def voidExplicit[B](implicit ev: A <:< B): Callback =
    void

  @deprecated("Use when() or unless().", "0.11.0")
  def conditionally(cond: => Boolean): CallbackTo[Option[A]] =
    when(cond)

  /**
   * Conditional execution of this callback.
   *
   * @param cond The condition required to be `true` for this callback to execute.
   * @return `Some` result of the callback executed, else `None`.
   */
  def when(cond: => Boolean): CallbackTo[Option[A]] =
    CallbackTo(if (cond) Some(f()) else None)

  /**
   * Conditional execution of this callback.
   * Reverse of [[when()]].
   *
   * @param cond The condition required to be `false` for this callback to execute.
   * @return `Some` result of the callback executed, else `None`.
   */
  def unless(cond: => Boolean): CallbackTo[Option[A]] =
    when(!cond)

  /**
   * Conditional execution of this callback.
   * Discards the result.
   *
   * @param cond The condition required to be `true` for this callback to execute.
   */
  def when_(cond: => Boolean): Callback =
    when(cond).void

  /**
   * Conditional execution of this callback.
   * Discards the result.
   * Reverse of [[when_()]].
   *
   * @param cond The condition required to be `false` for the callback to execute.
   */
  def unless_(cond: => Boolean): Callback =
    when_(!cond)

  /**
   * Wraps this callback in a try-catch and returns either the result or the exception if one occurs.
   */
  def attempt: CallbackTo[Either[Throwable, A]] =
    CallbackTo(
      try Right(f())
      catch { case t: Throwable => Left(t) }
    )

  /**
   * Wraps this callback in a scala `Try` with catches what it considers non-fatal errors.
   *
   * Use [[attempt]] to catch everything.
   */
  def attemptTry: CallbackTo[Try[A]] =
    CallbackTo(Try(f()))

  /**
   * Convenience-method to run additional code after this callback.
   */
  def thenRun[B](runNext: => B)(implicit ev: MapGuard[B]): CallbackTo[ev.Out] =
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
    delayMs(0)

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
    def nowMS: Long = System.currentTimeMillis()
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

  def asCBO[B](implicit ev:  CallbackTo[A] <:< CallbackTo[Option[B]]): CallbackOption[B] =
    CallbackOption(ev(this))

  def toCBO: CallbackOption[A] =
    CallbackOption liftCallback this

  /** Function distribution. See `CallbackTo.liftTraverse(f).id` for the dual. */
  def distFn[B, C](implicit ev: CallbackTo[A] <:< CallbackTo[B => C]): B => CallbackTo[C] = {
    val bc = ev(this)
    b => bc.map(_(b))
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Boolean ops

  private def bool2(b: CallbackTo[Boolean])
                   (op: (() => Boolean, () => Boolean) => Boolean)
                   (implicit ev: CallbackTo[A] <:< CallbackTo[Boolean]): CallbackTo[Boolean] = {
    val x = ev(this).f
    val y = b.f
    CallbackTo(op(x, y))
  }

  /**
   * Creates a new callback that returns `true` when both this and the given callback return `true`.
   */
  def &&(b: CallbackTo[Boolean])(implicit ev: CallbackTo[A] <:< CallbackTo[Boolean]): CallbackTo[Boolean] =
    bool2(b)(_() && _())

  /**
   * Creates a new callback that returns `true` when either this or the given callback return `true`.
   */
  def ||(b: CallbackTo[Boolean])(implicit ev: CallbackTo[A] <:< CallbackTo[Boolean]): CallbackTo[Boolean] =
    bool2(b)(_() || _())

  /**
   * Negates the callback result (so long as it's boolean).
   */
  def !(implicit ev: CallbackTo[A] <:< CallbackTo[Boolean]): CallbackTo[Boolean] =
    ev(this).map(!_)
}
