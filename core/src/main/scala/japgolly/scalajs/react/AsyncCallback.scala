package japgolly.scalajs.react

import japgolly.scalajs.react.internal.identityFn
import scala.collection.generic.CanBuildFrom
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.{Thenable, |}
import scala.scalajs.js.timers.setTimeout

object AsyncCallback {
  private[AsyncCallback] val defaultCompleteWith: Either[Throwable, Any] => Callback =
    _ => Callback.empty

  def apply[A](f: (Either[Throwable, A] => Callback) => Callback): AsyncCallback[A] =
    new AsyncCallback(f)

  def first[A](f: (Either[Throwable, A] => Callback) => Callback): AsyncCallback[A] =
    new AsyncCallback(g => {
      var first = true
      f(ea => Callback.when(first)(Callback{first = false} >> g(ea)))
    })

  private def tryE[A](a: => A): Either[Throwable, A] =
    try Right(a)
    catch {case t: Throwable => Left(t) }

  def point[A](a: => A): AsyncCallback[A] =
    AsyncCallback(_(tryE(a)))

  def pure[A](a: A): AsyncCallback[A] = {
    val r = Right(a)
    AsyncCallback(_(r))
  }

  /** Callback that isn't created until the first time it is used, after which it is reused. */
  def lazily[A](f: => AsyncCallback[A]): AsyncCallback[A] = {
    lazy val g = f
    byName(g)
  }

  /** Callback that is recreated each time it is used.
    *
    * https://en.wikipedia.org/wiki/Evaluation_strategy#Call_by_name
    */
  def byName[A](f: => AsyncCallback[A]): AsyncCallback[A] =
    point(f).flatten

  def traverse[T[X] <: TraversableOnce[X], A, B](ta: => T[A])(f: A => AsyncCallback[B])(implicit cbf: CanBuildFrom[T[A], B, T[B]]): AsyncCallback[T[B]] =
    AsyncCallback.byName {
      val as = ta.toVector
      if (as.isEmpty)
        AsyncCallback.pure(cbf().result())
      else {
        val discard = (_: Any, _: Any) => ()
        val bs = new js.Array[B](as.length)
        as
          .indices
          .iterator
          .map(i => f(as(i)).map(b => bs(i) = b))
          .reduce(_.zipWith(_)(discard))
          .map(_ => bs.to(cbf))
      }
    }

  def sequence[T[X] <: TraversableOnce[X], A](tca: => T[AsyncCallback[A]])(implicit cbf: CanBuildFrom[T[AsyncCallback[A]], A, T[A]]): AsyncCallback[T[A]] =
    traverse(tca)(identityFn)(cbf)

  def traverseOption[A, B](oa: => Option[A])(f: A => AsyncCallback[B]): AsyncCallback[Option[B]] =
    AsyncCallback.point(oa).flatMap {
      case Some(a) => f(a).map(Some(_))
      case None    => AsyncCallback.pure(None)
    }

  def sequenceOption[A](oca: => Option[AsyncCallback[A]]): AsyncCallback[Option[A]] =
    traverseOption(oca)(identityFn)

  def fromFuture[A](fa: => Future[A])(implicit ec: ExecutionContext): AsyncCallback[A] =
    AsyncCallback(f => Callback {
      fa.onComplete {
        case scala.util.Success(a) => f(Right(a)).runNow()
        case scala.util.Failure(e) => f(Left(e)).runNow()
      }
    })

  def fromCallbackToFuture[A](c: CallbackTo[Future[A]])(implicit ec: ExecutionContext): AsyncCallback[A] =
    c.asAsyncCallback.flatMap(fromFuture(_))

  def fromJsPromise[A](pa: => js.Thenable[A]): AsyncCallback[A] =
    AsyncCallback(f => Callback {
      def complete(e: Either[Throwable, A]): Thenable[Unit] = f(e).asAsyncCallback.asCallbackToJsPromise.runNow()
      type R = Unit | Thenable[Unit]
      val ok: A   => R = a => complete(Right(a))
      val ko: Any => R = e => complete(Left(e match {
        case t: Throwable => t
        case a            => js.JavaScriptException(e)
      }))
      pa.`then`[Unit](ok, ko: js.Function1[Any, R])
    })

  def fromCallbackToJsPromise[A](c: CallbackTo[js.Promise[A]]): AsyncCallback[A] =
    c.asAsyncCallback.flatMap(fromJsPromise(_))

  @inline implicit def asynCallbackCovariance[A, B >: A](c: AsyncCallback[A]): AsyncCallback[B] =
    c.widen
}

/** Pure asynchronous callback.
  *
  * You can think of this as being similar to using `Future` - you can use it in for-comprehensions the same way -
  * except `AsyncCallback` is pure and doesn't need an `ExecutionContext`.
  *
  * When combining instances, it's good to know which methods are sequential and which are parallel
  * (or at least concurrent).
  *
  * The following methods are sequential:
  * - [[>>=()]] / [[flatMap()]]
  * - [[>>()]]
  * - [[<<()]]
  * - [[flatTap()]]
  *
  * The following methods are effectively parallel:
  * - [[*>()]]
  * - [[<*()]]
  * - [[race()]]
  * - [[zip()]] & [[zipWith()]]
  *
  * In order to actually run this, or get it into a shape in which in can be run, use one of the following:
  * - [[toCallback]] <-- most common
  * - [[asCallbackToFuture]]
  * - [[asCallbackToJsPromise]]
  * - [[unsafeToFuture()]]
  * - [[unsafeToJsPromise()]]
  *
  * @tparam A The type of data asynchronously produced on success.
  */
final class AsyncCallback[A] private[AsyncCallback] (val completeWith: (Either[Throwable, A] => Callback) => Callback) extends AnyVal {

  def widen[B >: A]: AsyncCallback[B] =
    new AsyncCallback(completeWith)

  def map[B](f: A => B): AsyncCallback[B] =
    AsyncCallback(g => completeWith(e => g(e.map(f))))

  /** Alias for `map`. */
  @inline def |>[B](f: A => B): AsyncCallback[B] =
    map(f)

  def flatMap[B](f: A => AsyncCallback[B]): AsyncCallback[B] =
    AsyncCallback(g => completeWith {
      case Right(a) => f(a).completeWith(g)
      case Left(e)  => g(Left(e))
    })

  /** Alias for `flatMap`. */
  @inline def >>=[B](g: A => AsyncCallback[B]): AsyncCallback[B] =
    flatMap(g)

  def flatten[B](implicit ev: AsyncCallback[A] =:= AsyncCallback[AsyncCallback[B]]): AsyncCallback[B] =
    ev(this).flatMap(identityFn)

  /** Sequence the argument a callback to run after this, discarding any value produced by this. */
  def >>[B](runNext: AsyncCallback[B]): AsyncCallback[B] =
    flatMap(_ => runNext)

  /** Sequence a callback to run before this, discarding any value produced by it. */
  @inline def <<[B](runBefore: AsyncCallback[B]): AsyncCallback[A] =
    runBefore >> this

  /** Convenient version of `<<` that accepts an Option */
  def <<?[B](prev: Option[AsyncCallback[B]]): AsyncCallback[A] =
    prev.fold(this)(_ >> this)

  /** When the callback result becomes available, perform a given side-effect with it. */
  def tap(t: A => Any): AsyncCallback[A] =
    flatTap(a => AsyncCallback.point(t(a)))

  /** Alias for `tap`. */
  @inline def <|(t: A => Any): AsyncCallback[A] =
    tap(t)

  def flatTap[B](t: A => AsyncCallback[B]): AsyncCallback[A] =
    for {
      a <- this
      _ <- t(a)
    } yield a

  def zip[B](that: AsyncCallback[B]): AsyncCallback[(A, B)] =
    zipWith(that)((_, _))

  def zipWith[B, C](that: AsyncCallback[B])(f: (A, B) => C): AsyncCallback[C] =
    AsyncCallback(cc => CallbackTo {
      var ra: Option[Either[Throwable, A]] = None
      var rb: Option[Either[Throwable, B]] = None
      var r: Option[Either[Throwable, C]] = None

      val respond = Callback {
        if (r.isEmpty) {
          (ra, rb) match {
            case (Some(Right(a)), Some(Right(b))) => r = Some(Right(f(a, b)))
            case (Some(Left(e)) , _             ) => r = Some(Left(e))
            case (_             , Some(Left(e)) ) => r = Some(Left(e))
            case (Some(Right(_)), None          )
               | (None          , Some(Right(_)))
               | (None          , None          ) => ()
          }
          r.foreach(cc(_).runNow())
        }
      }

      this.completeWith(e => Callback {ra = Some(e)} >> respond) >>
      that.completeWith(e => Callback {rb = Some(e)} >> respond)
    }.flatten)

  /** Start both this and the given callback at once and when both have completed successfully,
    * discard the value produced by this.
    */
  def *>[B](next: AsyncCallback[B]): AsyncCallback[B] =
    zipWith(next)((_, b) => b)

  /** Start both this and the given callback at once and when both have completed successfully,
    * discard the value produced by the given callback.
    */
  def <*[B](next: AsyncCallback[B]): AsyncCallback[A] =
    zipWith(next)((a, _) => a)

  /** Discard the callback's return value, return a given value instead.
    *
    * `ret`, short for `return`.
    */
  def ret[B](b: B): AsyncCallback[B] =
    map(_ => b)

  /** Discard the value produced by this callback. */
  def void: AsyncCallback[Unit] =
    map(_ => ())

  /** Discard the value produced by this callback.
    *
    * This method allows you to be explicit about the type you're discarding (which may change in future).
    */
  @inline def voidExplicit[B](implicit ev: A <:< B): AsyncCallback[Unit] =
    void

  /** Wraps this callback in a try-catch and returns either the result or the exception if one occurs. */
  def attempt: AsyncCallback[Either[Throwable, A]] =
    AsyncCallback(f => completeWith(e => f(Right(e))))

  def handleError(f: Throwable => AsyncCallback[A]): AsyncCallback[A] =
    AsyncCallback(g => completeWith {
      case r@ Right(_) => g(r)
      case Left(t)     => f(t).completeWith(g)
    })

  def maybeHandleError(f: PartialFunction[Throwable, AsyncCallback[A]]): AsyncCallback[A] =
    AsyncCallback(g => completeWith {
      case r@ Right(_) => g(r)
      case l@ Left(t)  => f.lift(t) match {
        case Some(n) => n.completeWith(g)
        case None    => g(l)
      }
    })

  /** Conditional execution of this callback.
    *
    * @param cond The condition required to be `true` for this callback to execute.
    * @return `Some` result of the callback executed, else `None`.
    */
  def when(cond: => Boolean): AsyncCallback[Option[A]] =
    AsyncCallback(f => if (cond) completeWith(ea => f(ea.map(Some(_)))) else f(Right(None)))

  /** Conditional execution of this callback.
    * Reverse of [[when()]].
    *
    * @param cond The condition required to be `false` for this callback to execute.
    * @return `Some` result of the callback executed, else `None`.
    */
  def unless(cond: => Boolean): AsyncCallback[Option[A]] =
    when(!cond)

  /** Conditional execution of this callback.
    * Discards the result.
    *
    * @param cond The condition required to be `true` for this callback to execute.
    */
  def when_(cond: => Boolean): AsyncCallback[Unit] =
    when(cond).void

  /** Conditional execution of this callback.
    * Discards the result.
    * Reverse of [[when_()]].
    *
    * @param cond The condition required to be `false` for the callback to execute.
    */
  def unless_(cond: => Boolean): AsyncCallback[Unit] =
    when_(!cond)

  /** Log to the console before this callback starts, and after it completes.
    *
    * Does not change the result.
    */
  def logAround(message: js.Any, optionalParams: js.Any*): AsyncCallback[A] = {
    def log(prefix: String) = Callback.log(prefix + message.toString, optionalParams: _*).asAsyncCallback
    log("→  Starting: ") *> this <* log(" ← Finished: ")
  }

  /** Logs the result of this callback as it completes. */
  def logResult(msg: A => String): AsyncCallback[A] =
    flatTap(a => Callback.log(msg(a)).asAsyncCallback)

  /** Logs the result of this callback as it completes.
    *
    * @param name Prefix to appear the log output.
    */
  def logResult(name: String): AsyncCallback[A] =
    logResult(a => s"$name: $a")

  /** Logs the result of this callback as it completes. */
  def logResult: AsyncCallback[A] =
    logResult(_.toString)

  def delay(dur: FiniteDuration): AsyncCallback[A] =
    delayMs(dur.toMillis.toDouble)

  def delayMs(milliseconds: Double): AsyncCallback[A] =
    AsyncCallback(f => Callback {
      setTimeout(milliseconds) {
        completeWith(f).runNow()
      }
    })

  /** Function distribution. See `AsyncCallback.liftTraverse(f).id` for the dual. */
  def distFn[B, C](implicit ev: AsyncCallback[A] <:< AsyncCallback[B => C]): B => AsyncCallback[C] = {
    val bc = ev(this)
    b => bc.map(_(b))
  }

  /** Start both this and the given callback at once use the first result to become available,
    * regardless of whether it's a success or failure.
    */
  def race[B](that: AsyncCallback[B]): AsyncCallback[Either[A, B]] =
    AsyncCallback.first(f =>
      this.completeWith(e => f(e.map(Left(_)))) >>
      that.completeWith(e => f(e.map(Right(_)))))

  def toCallback: Callback =
    completeWith(AsyncCallback.defaultCompleteWith)

  def asCallbackToFuture: CallbackTo[Future[A]] =
    CallbackTo {
      val p = scala.concurrent.Promise[A]()
      completeWith(ea => Callback(ea.fold(p.failure, p.success)))
      p.future
    }

  def asCallbackToJsPromise: CallbackTo[js.Promise[A]] =
    CallbackTo {
      new js.Promise[A]((respond, reject) => {
        def fail(t: Throwable) =
          reject(t match {
            case js.JavaScriptException(e) => e
            case e                         => e
          })
        completeWith(ea => Callback(ea.fold(fail, respond(_))))
      })
    }

  def unsafeToFuture(): Future[A] =
    asCallbackToFuture.runNow()

  def unsafeToJsPromise(): js.Promise[A] =
    asCallbackToJsPromise.runNow()

}
