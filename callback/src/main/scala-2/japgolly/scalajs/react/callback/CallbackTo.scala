package japgolly.scalajs.react.callback

import japgolly.scalajs.react.callback.CallbackTo.MapGuard
import japgolly.scalajs.react.util.Effect.Sync
import japgolly.scalajs.react.util.JsUtil
import japgolly.scalajs.react.util.Util.{catchAll, identityFn}
import java.time.{Duration, Instant}
import org.scalajs.dom.raw.Window
import org.scalajs.dom.window
import scala.annotation.tailrec
import scala.collection.BuildFrom
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.timers.RawTimers
import scala.scalajs.js.{Function0 => JFn0, Function1 => JFn1, UndefOr, undefined}
import scala.util.Try

object CallbackTo {
  @inline def apply[A](f: => A): CallbackTo[A] =
    lift(() => f)

  def lift[A](f: () => A): CallbackTo[A] =
    new CallbackTo(Trampoline.delay(f))

  def pure[A](a: A): CallbackTo[A] =
    new CallbackTo(Trampoline.pure(a))

  def throwException[A](t: Throwable): CallbackTo[A] =
    CallbackTo(throw t)

  def fromJsFn[A](f: js.Function0[A]): CallbackTo[A] =
    apply(f())

  /** Callback that isn't created until the first time it is used, after which it is reused. */
  def lazily[A](f: => CallbackTo[A]): CallbackTo[A] = {
    lazy val g = f
    suspend(g)
  }

  /** Callback that is recreated each time it is used. */
  def suspend[A](f: => CallbackTo[A]): CallbackTo[A] =
    new CallbackTo(Trampoline.suspend(() => f.trampoline))

  /** Callback that is recreated each time it is used.
    *
    * https://en.wikipedia.org/wiki/Evaluation_strategy#Call_by_name
    */
  @deprecated("Use CallbackTo.suspend", "2.0.0")
  def byName[A](f: => CallbackTo[A]): CallbackTo[A] =
    suspend(f)

  /** Tail-recursive callback. Uses constant stack space.
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
    def std[T[X] <: Iterable[X]](implicit cbf: BuildFrom[T[A], B, T[B]]): CallbackTo[T[A] => T[B]] =
      CallbackTo { ta =>
        val r = cbf.newBuilder(ta)
        ta.iterator.foreach(a => r += f(a).runNow())
        r.result()
      }

    def option: CallbackTo[Option[A] => Option[B]] =
      CallbackTo(_.map(f(_).runNow()))
  }

  /** Traverse stdlib T over CallbackTo.
    * Distribute CallbackTo over stdlib T.
    */
  def traverse[T[X] <: Iterable[X], A, B](ta: => T[A])(f: A => CallbackTo[B])(implicit cbf: BuildFrom[T[A], B, T[B]]): CallbackTo[T[B]] =
    liftTraverse(f).std[T](cbf).map(_(ta))

  /** Sequence stdlib T over CallbackTo.
    * Co-sequence CallbackTo over stdlib T.
    */
  def sequence[T[X] <: Iterable[X], A](tca: => T[CallbackTo[A]])(implicit cbf: BuildFrom[T[CallbackTo[A]], A, T[A]]): CallbackTo[T[A]] =
    traverse(tca)(identityFn)(cbf)

  /** Traverse Option over CallbackTo.
    * Distribute CallbackTo over Option.
    */
  def traverseOption[A, B](oa: => Option[A])(f: A => CallbackTo[B]): CallbackTo[Option[B]] =
    liftTraverse(f).option.map(_(oa))

  /** Sequence Option over CallbackTo.
    * Co-sequence CallbackTo over Option.
    */
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

  def newJsPromise[A]: CallbackTo[(js.Promise[A], Try[A] => Callback)] =
    CallbackTo {
      val (p, f) = JsUtil.newPromise[A]()
      (p, t => Callback.fromJsFn(f(t)))
    }

  lazy val now: CallbackTo[Instant] =
    apply(Instant.now())

  lazy val currentTimeMillis: CallbackTo[Long] =
    apply(System.currentTimeMillis())

  lazy val nanoTime: CallbackTo[Long] =
    apply(System.nanoTime())

  /** When executed, opens a new window (tab) to a given URL.
    *
    * @param noopener See https://developers.google.com/web/tools/lighthouse/audits/noopener
    * @param focus    Whether or not to focus the new window.
    */
  def windowOpen(url     : String,
                 noopener: Boolean = true,
                 focus   : Boolean = true): CallbackTo[Window] =
    CallbackTo {
      val w = window.open(url, target = "_blank")
      if (noopener) w.opener = null
      if (focus) w.focus()
      w
    }

  /** Displays a dialog with an optional message prompting the user to input some text.
    *
    * @return Some string comprising the text entered by the user, or None.
    */
  def prompt: CallbackTo[Option[String]] =
    apply(Option(window.prompt()))

  /** Displays a dialog with an optional message prompting the user to input some text.
    *
    * @param message a string of text to display to the user. This parameter is optional and can be omitted if there is nothing to show in the prompt window.
    * @return Some string comprising the text entered by the user, or None.
    */
  def prompt(message: String): CallbackTo[Option[String]] =
    apply(Option(window.prompt(message)))

  /** Displays a dialog with an optional message prompting the user to input some text.
    *
    * @param message a string of text to display to the user. This parameter is optional and can be omitted if there is nothing to show in the prompt window.
    * @param default a string containing the default value displayed in the text input field. It is an optional parameter. Note that in Internet Explorer 7 and 8, if you do not provide this parameter, the string "undefined" is the default value.
    * @return Some string comprising the text entered by the user, or None.
    */
  def prompt(message: String, default: String): CallbackTo[Option[String]] =
    apply(Option(window.prompt(message, default)))

  /** Displays a modal dialog with a message and two buttons, OK and Cancel.
    *
    * @param message a string to be displayed in the dialog.
    * @return A boolean value indicating whether OK or Cancel was selected (true means OK).
    */
  def confirm(message: String): CallbackTo[Boolean] =
    apply(window.confirm(message))

  def retryUntilRight[L, R](attempt: CallbackTo[Either[L, R]])(onLeft: L => Callback): CallbackTo[R] =
    tailrec[Unit, R](())(_ => attempt.flatMap {
      case Left(e)  => onLeft(e).map(_ => Left(()))
      case Right(a) => CallbackTo pure Right(a)
    })

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

  @inline implicit def InvariantOps[A](c: CallbackTo[A]): InvariantOps[A] =
    new InvariantOps[A](c.trampoline)

  final class InvariantOps[A](private val trampoline: Trampoline[A]) extends AnyVal {
    def to[F[_]](implicit F: Sync[F]): F[A] =
      F.delay(trampoline.run)
  }
}

// =====================================================================================================================

/**
 * A function to be executed later, usually by scalajs-react in response to some kind of event.
 *
 * The purpose of this class is to lift effects into the type system, and use the compiler to ensure safety around
 * callbacks (without depending on an external library like Cats).
 *
 * `() => Unit` is replaced by `Callback`.
 * Similarly, `ReactEvent => Unit` is replaced  by `ReactEvent => Callback`.
 *
 * @tparam A The type of result produced when the callback is invoked.
 *
 * @since 0.10.0
 */
final class CallbackTo[+A] private[react] (private[CallbackTo] val trampoline: Trampoline[A]) extends AnyVal { self =>

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
    trampoline.run

  def map[B](f: A => B)(implicit ev: MapGuard[B]): CallbackTo[ev.Out] =
    new CallbackTo(trampoline.map(f))

  /** Alias for `map`. */
  @inline def |>[B](f: A => B)(implicit ev: MapGuard[B]): CallbackTo[ev.Out] =
    map(f)

  def flatMap[B](f: A => CallbackTo[B]): CallbackTo[B] =
    new CallbackTo(trampoline.flatMap(f(_).trampoline))

  /** Alias for `flatMap`. */
  @inline def >>=[B](f: A => CallbackTo[B]): CallbackTo[B] =
    flatMap(f)

  def flatten[B](implicit ev: A => CallbackTo[B]): CallbackTo[B] =
    flatMap(ev)

  def flatMap2[X, Y, Z](f: (X, Y) => CallbackTo[Z])(implicit ev: A <:< (X, Y)): CallbackTo[Z] =
    flatMap(f tupled _)

  /** Sequence a callback to run after this, discarding any value produced by this. */
  def >>[B](runNext: CallbackTo[B]): CallbackTo[B] =
    if (isEmpty_?)
      runNext
    else
      flatMap(_ => runNext)

  /** Sequence a callback to run before this, discarding any value produced by it. */
  @inline def <<[B](runBefore: CallbackTo[B]): CallbackTo[A] =
    runBefore >> this

  /** Convenient version of `<<` that accepts an Option */
  def <<?[B](prev: Option[CallbackTo[B]]): CallbackTo[A] =
    prev.fold(this)(_ >> this)

  /** When the callback result becomes available, perform a given side-effect with it. */
  def tap(t: A => Any): CallbackTo[A] =
    flatTap(a => CallbackTo(t(a)))

  /** Alias for `tap`. */
  @inline def <|(t: A => Any): CallbackTo[A] =
    tap(t)

  def flatTap[B](t: A => CallbackTo[B]): CallbackTo[A] =
    for {
      a <- this
      _ <- t(a)
    } yield a

  def zip[B](cb: CallbackTo[B]): CallbackTo[(A, B)] =
    zipWith(cb)((_, _))

  def zipWith[B, C](cb: CallbackTo[B])(f: (A, B) => C): CallbackTo[C] =
    for {
      a <- this
      b <- cb
    } yield f(a, b)

  /** Alias for `>>`.
    *
    * Where `>>` is often associated with Monads, `*>` is often associated with Applicatives.
    */
  @inline def *>[B](runNext: CallbackTo[B]): CallbackTo[B] =
    >>(runNext)

  /** Sequence actions, discarding the value of the second argument. */
  def <*[B](next: CallbackTo[B]): CallbackTo[A] =
    flatTap(_ => next)

  /** Discard the callback's return value, return a given value instead.
    *
    * `ret`, short for `return`.
    */
  def ret[B](b: B): CallbackTo[B] =
    this >> CallbackTo.pure(b)

  /** Discard the value produced by this callback. */
  def void: Callback =
    this >> Callback.empty

  /** Discard the value produced by this callback.
    *
    * This method allows you to be explicit about the type you're discarding (which may change in future).
    */
  @inline def voidExplicit[B](implicit ev: A <:< B): Callback =
    void

  /** Wraps this callback in a try-catch and returns either the result or the exception if one occurs. */
  def attempt: CallbackTo[Either[Throwable, A]] =
    CallbackTo(
      try Right(runNow())
      catch { case t: Throwable => Left(t) }
    )

  def attemptTry: CallbackTo[Try[A]] =
    CallbackTo(catchAll(runNow()))

  def handleError[AA >: A](f: Throwable => CallbackTo[AA]): CallbackTo[AA] =
    CallbackTo[AA](
      try runNow()
      catch { case t: Throwable => f(t).runNow() })

  def maybeHandleError[AA >: A](f: PartialFunction[Throwable, CallbackTo[AA]]): CallbackTo[AA] =
    CallbackTo[AA](
      try runNow()
      catch {
        case t: Throwable => f.lift(t) match {
          case Some(c) => c.runNow()
          case None    => throw t
        }
      })

  /** If this completes successfully, discard the result.
    * If any exception occurs, call `printStackTrace` and continue.
    *
    * @since 2.0.0
    */
  def reset: Callback =
    Callback(
      try {
        runNow()
        ()
      } catch {
        case t: Throwable =>
          t.printStackTrace()
      }
    )

  /** Return a version of this callback that will only execute once, and reuse the result for all
    * other invocations.
    */
  def memo(): CallbackTo[A] = {
    var result: Option[Try[A]] = None
    val real = attemptTry
    CallbackTo {
      val t: Try[A] =
        result.getOrElse {
          val t = real.runNow()
          result = Some(t)
          t
        }
      t.get
    }
  }

  /** Conditional execution of this callback.
    *
    * @param cond The condition required to be `true` for this callback to execute.
    * @return `Some` result of the callback executed, else `None`.
    */
  def when(cond: => Boolean): CallbackTo[Option[A]] =
    CallbackTo(if (cond) Some(runNow()) else None)

  /** Conditional execution of this callback.
    * Reverse of [[when()]].
    *
    * @param cond The condition required to be `false` for this callback to execute.
    * @return `Some` result of the callback executed, else `None`.
    */
  def unless(cond: => Boolean): CallbackTo[Option[A]] =
    when(!cond)

  /** Conditional execution of this callback.
    * Discards the result.
    *
    * @param cond The condition required to be `true` for this callback to execute.
    */
  def when_(cond: => Boolean): Callback =
    when(cond).void

  /** Conditional execution of this callback.
    * Discards the result.
    * Reverse of [[when_()]].
    *
    * @param cond The condition required to be `false` for the callback to execute.
    */
  def unless_(cond: => Boolean): Callback =
    when_(!cond)

  /** Limits the number of invocations in a given amount of time.
    *
    * @return Some if invocation was allowed, None if rejected/rate-limited
    */
  def rateLimit(window: Duration): CallbackTo[Option[A]] =
    rateLimitMs(window.toMillis)

  /** Limits the number of invocations in a given amount of time.
    *
    * @return Some if invocation was allowed, None if rejected/rate-limited
    */
  def rateLimit(window: FiniteDuration): CallbackTo[Option[A]] =
    rateLimitMs(window.toMillis)

  /** Limits the number of invocations in a given amount of time.
    *
    * @return Some if invocation was allowed, None if rejected/rate-limited
    */
  def rateLimit(window: Duration, maxPerWindow: Int): CallbackTo[Option[A]] =
    rateLimitMs(window.toMillis, maxPerWindow)

  /** Limits the number of invocations in a given amount of time.
    *
    * @return Some if invocation was allowed, None if rejected/rate-limited
    */
  def rateLimit(window: FiniteDuration, maxPerWindow: Int): CallbackTo[Option[A]] =
    rateLimitMs(window.toMillis, maxPerWindow)

  /** Limits the number of invocations in a given amount of time.
    *
    * @return Some if invocation was allowed, None if rejected/rate-limited
    */
  def rateLimitMs(windowMs: Long, maxPerWindow: Int = 1): CallbackTo[Option[A]] =
    if (windowMs <= 0 || maxPerWindow <= 0)
      CallbackTo.pure(None)
    else
      CallbackTo.lift(RateLimit.fn0(
        run          = toScalaFn,
        maxPerWindow = maxPerWindow,
        windowMs = windowMs,
      ))

  /** Creates an debounce boundary over the underlying computation.
    *
    * Save the result of this as a `val` somewhere because it relies on internal state that must be reused.
    */
  def debounce(delay: Duration): Callback =
    _debounceMs(delay.toMillis)

  /** Creates an debounce boundary over the underlying computation.
    *
    * Save the result of this as a `val` somewhere because it relies on internal state that must be reused.
    */
  def debounce(delay: FiniteDuration): Callback =
    _debounceMs(delay.toMillis)

  /** Creates an debounce boundary over the underlying computation.
    *
    * Save the result of this as a `val` somewhere because it relies on internal state that must be reused.
    */
  def debounceMs(delayMs: Long): Callback =
    _debounceMs(delayMs)

  private[react] def _debounceMs(delayMs: Long)(implicit timer: Timer): Callback =
    if (delayMs <= 0)
      void
    else {
      var prev = Option.empty[timer.Handle]

      CallbackTo {
        prev.foreach(timer.cancel)
        val newHandle = timer.delay(delayMs) {
          prev = None
          self.runNow()
        }
        prev = Some(newHandle)
      }
    }

  /** Log to the console before this callback starts, and after it completes.
    *
    * Does not change the result.
    */
  def logAround(message: Any, optionalParams: Any*): CallbackTo[A] = {
    def log(prefix: String) = Callback.log(prefix + message, optionalParams: _*)
    log("→  Starting: ") *> this <* log(" ← Finished: ")
  }

  /** Logs the result of this callback as it completes. */
  def logResult(msg: A => String): CallbackTo[A] =
    flatTap(a => Callback.log(msg(a)))

  /** Logs the result of this callback as it completes.
    *
    * @param name Prefix to appear the log output.
    */
  def logResult(name: String): CallbackTo[A] =
    logResult(a => s"$name: $a")

  /** Logs the result of this callback as it completes. */
  def logResult: CallbackTo[A] =
    logResult(_.toString)


  /** Convenience-method to run additional code after this callback. */
  def thenRun[B](runNext: => B)(implicit ev: MapGuard[B]): CallbackTo[ev.Out] =
    this >> CallbackTo(runNext)

  /** Convenience-method to run additional code before this callback. */
  def precedeWith(runFirst: => Unit): CallbackTo[A] =
    this << Callback(runFirst)

  /** Wraps this callback in a `try-finally` block and runs the given callback in the `finally` clause, after the
    * current callback completes, be it in error or success.
    */
  def finallyRun[B](runFinally: CallbackTo[B]): CallbackTo[A] =
    CallbackTo(try runNow() finally runFinally.runNow())

  @inline def toScalaFn: () => A =
    () => runNow()

  /** The underlying representation of this value-class */
  def underlyingRepr: Trampoline[A] =
    trampoline

  def toJsFn: JFn0[A] =
    toScalaFn

  def toJsFn1: JFn1[Any, A] =
    (_: Any) => runNow()

  def toJsCallback: UndefOr[JFn0[A]] =
    if (isEmpty_?) undefined else toJsFn

  def isEmpty_? : Boolean =
    trampoline eq Callback.empty.trampoline

  /** Turns this into an [[AsyncCallback]] that runs whenever/wherever it's called;
    * `setTimeout` isn't used.
    *
    * In order words, `this.toAsyncCallback.toCallback` == `this`.
    */
  def asAsyncCallback: AsyncCallback[A] =
    AsyncCallback(attemptTry.flatMap)

  /** Schedules this to run asynchronously (i.e. uses a `setTimeout`).
    *
    * Exceptions will be handled by the [[AsyncCallback]] such that
    * `this.async.toCallback` will never throw an exception.
    */
  def async: AsyncCallback[A] =
    delayMs(1)

  /** Run asynchronously after a delay of a given duration. */
  def delay(startIn: FiniteDuration): AsyncCallback[A] =
    delayMs(startIn.toMillis.toDouble)

  /** Run asynchronously after a delay of a given duration. */
  def delay(startIn: Duration): AsyncCallback[A] =
    delayMs(startIn.toMillis.toDouble)

  /** Run asynchronously after a `startInMilliseconds` ms delay. */
  def delayMs(startInMilliseconds: Double): AsyncCallback[A] =
    asAsyncCallback.delayMs(startInMilliseconds)

  /** Record the duration of this callback's execution. */
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

  /** Log the duration of this callback's execution. */
  def logDuration(fmt: FiniteDuration => String): CallbackTo[A] =
    withDuration((a, d) =>
      Callback.log(fmt(d)) ret a)

  /** Log the duration of this callback's execution.
    *
    * @param name Prefix to appear the log output.
    */
  def logDuration(name: String): CallbackTo[A] =
    logDuration(d => s"$name completed in $d.")

  /** Log the duration of this callback's execution. */
  def logDuration: CallbackTo[A] =
    logDuration("Callback")

  def asCBO[B](implicit ev: CallbackTo[A] <:< CallbackTo[Option[B]]): CallbackOption[B] =
    new CallbackOption(ev(this).toScalaFn)

  def toCBO: CallbackOption[A] =
    map[Option[A]](Some(_)).asCBO

  /** Returns a [[CallbackOption]] that requires the boolean value therein to be true. */
  def requireCBO(implicit ev: CallbackTo[A] <:< CallbackTo[Boolean]): CallbackOption[Unit] =
    ev(this).toCBO.flatMap(CallbackOption.require(_))

  /** Function distribution. See `CallbackTo.liftTraverse(f).id` for the dual. */
  def distFn[B, C](implicit ev: CallbackTo[A] <:< CallbackTo[B => C]): B => CallbackTo[C] = {
    val bc = ev(this)
    b => bc.map(_(b))
  }

  /** Schedule for repeated execution every `interval`. */
  def setInterval(interval: Duration): CallbackTo[Callback.SetIntervalResult] =
    setIntervalMs(interval.toMillis.toDouble)

  /** Schedule for repeated execution every `interval`. */
  def setInterval(interval: FiniteDuration): CallbackTo[Callback.SetIntervalResult] =
    setIntervalMs(interval.toMillis.toDouble)

  /** Schedule this callback for repeated execution every `interval` milliseconds.
    *
    * @param interval duration in milliseconds between executions
    * @return A means to cancel the interval.
    */
  def setIntervalMs(interval: Double): CallbackTo[Callback.SetIntervalResult] = {
    val underlying = self.toJsFn
    CallbackTo {
      val handle = RawTimers.setInterval(underlying, interval)
      new Callback.SetIntervalResult(handle)
    }
  }

  /** Schedule this callback for execution in `interval`.
    *
    * Note: in most cases [[delay()]] is a better alternative.
    *
    * @return A means to cancel the timeout.
    */
  def setTimeout(interval: Duration): CallbackTo[Callback.SetTimeoutResult] =
    setTimeoutMs(interval.toMillis.toDouble)

  /** Schedule this callback for execution in `interval`.
    *
    * Note: in most cases [[delay()]] is a better alternative.
    *
    * @return A means to cancel the timeout.
    */
  def setTimeout(interval: FiniteDuration): CallbackTo[Callback.SetTimeoutResult] =
    setTimeoutMs(interval.toMillis.toDouble)

  /** Schedule this callback for execution in `interval` milliseconds.
    *
    * Note: in most cases [[delayMs()]] is a better alternative.
    *
    * @param interval duration in milliseconds to wait
    * @return A means to cancel the timeout.
    */
  def setTimeoutMs(interval: Double): CallbackTo[Callback.SetTimeoutResult] = {
    val underlying = self.toJsFn
    CallbackTo {
      val handle = RawTimers.setTimeout(underlying, interval)
      new Callback.SetTimeoutResult(handle)
    }
  }

  /** Runs this computation in the background. */
  def dispatch: Callback =
    asAsyncCallback.fork_

  def withFilter(f: A => Boolean): CallbackTo[A] =
    map[A](a => if (f(a)) a else
      // This is what scala.Future does
      throw new NoSuchElementException("CallbackTo.withFilter predicate is not satisfied"))

  // -------------------------------------------------------------------------------------------------------------------
  // Boolean ops

  private def bool2(b: CallbackTo[Boolean])
                   (op: (() => Boolean, () => Boolean) => Boolean)
                   (implicit ev: CallbackTo[A] <:< CallbackTo[Boolean]): CallbackTo[Boolean] = {
    val x = ev(this).toScalaFn
    val y = b.toScalaFn
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

  /** Negates the callback result (so long as it's boolean). */
  @deprecated("Use !cb instead of cb.!", "2.0.0")
  def !(implicit ev: CallbackTo[A] <:< CallbackTo[Boolean]): CallbackTo[Boolean] =
    ev(this).map(!_)

  /** Negates the callback result (so long as it's boolean). */
  def unary_!(implicit ev: CallbackTo[A] <:< CallbackTo[Boolean]): CallbackTo[Boolean] =
    ev(this).map(!_)
}
