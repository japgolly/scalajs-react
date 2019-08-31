package japgolly.scalajs.react

import japgolly.scalajs.react.internal.{catchAll, identityFn, newJsPromise, SyncPromise}
import scala.collection.compat._
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.{Thenable, |}
import scala.scalajs.js.timers.setTimeout
import scala.util.{Failure, Success, Try}

object AsyncCallback {
  private[AsyncCallback] val defaultCompleteWith: Try[Any] => Callback =
    _ => Callback.empty

  def apply[A](f: (Try[A] => Callback) => Callback): AsyncCallback[A] =
    new AsyncCallback(f)

  def init[A, B](f: (Try[B] => Callback) => CallbackTo[A]): CallbackTo[(A, AsyncCallback[B])] =
    for {
      (ac, c) <- promise[B]
      a       <- f(c)
    } yield (a, ac)

  /** Create an AsyncCallback and separately provide the completion function.
    *
    * This is like Scala's promise, not the JS promise which is more like Scala's Future.
    */
  def promise[A]: CallbackTo[(AsyncCallback[A], Try[A] => Callback)] =
    for {
      p <- SyncPromise[A]
    } yield (AsyncCallback(p.onComplete), p.complete)

  def first[A](f: (Try[A] => Callback) => Callback): AsyncCallback[A] =
    new AsyncCallback(g => CallbackTo {
      var first = true
      f(ea => Callback.when(first)(Callback{first = false} >> g(ea)))
    }.flatten)

  def point[A](a: => A): AsyncCallback[A] =
    AsyncCallback(_(catchAll(a)))

  def pure[A](a: A): AsyncCallback[A] =
    const(Success(a))

  def throwException[A](t: Throwable): AsyncCallback[A] =
    const(Failure(t))

  def const[A](t: Try[A]): AsyncCallback[A] =
    AsyncCallback(_(t))

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

  @deprecated("Use c.asAsyncCallback", "")
  def fromCallback[A](c: CallbackTo[A]): AsyncCallback[A] =
    c.asAsyncCallback

  /** Traverse stdlib T over AsyncCallback.
    * Distribute AsyncCallback over stdlib T.
    */
  def traverse[T[X] <: IterableOnce[X], A, B](ta: => T[A])(f: A => AsyncCallback[B])(implicit cbf: BuildFrom[T[A], B, T[B]]): AsyncCallback[T[B]] =
    AsyncCallback.byName {
      val as = ta.iterator.to(Vector)
      if (as.isEmpty)
        AsyncCallback.pure(cbf.newBuilder(ta).result())
      else {
        val discard = (_: Any, _: Any) => ()
        val bs = new js.Array[B](as.length)
        as
          .indices
          .iterator
          .map(i => f(as(i)).map(b => bs(i) = b))
          .reduce(_.zipWith(_)(discard))
          .map(_ => cbf.fromSpecific(ta)(bs))
      }
    }

  /** Sequence stdlib T over AsyncCallback.
    * Co-sequence AsyncCallback over stdlib T.
    */
  def sequence[T[X] <: IterableOnce[X], A](tca: => T[AsyncCallback[A]])(implicit cbf: BuildFrom[T[AsyncCallback[A]], A, T[A]]): AsyncCallback[T[A]] =
    traverse(tca)(identityFn)(cbf)

  /** Traverse Option over AsyncCallback.
    * Distribute AsyncCallback over Option.
    */
  def traverseOption[A, B](oa: => Option[A])(f: A => AsyncCallback[B]): AsyncCallback[Option[B]] =
    AsyncCallback.point(oa).flatMap {
      case Some(a) => f(a).map(Some(_))
      case None    => AsyncCallback.pure(None)
    }

  /** Sequence Option over AsyncCallback.
    * Co-sequence AsyncCallback over Option.
    */
  def sequenceOption[A](oca: => Option[AsyncCallback[A]]): AsyncCallback[Option[A]] =
    traverseOption(oca)(identityFn)

  def fromFuture[A](fa: => Future[A])(implicit ec: ExecutionContext): AsyncCallback[A] =
    AsyncCallback(f => Callback {
      val future = fa
      future.value match {
        case Some(value) => f(value).runNow()
        case None => future.onComplete(f(_).runNow())
      }
    })

  def fromCallbackToFuture[A](c: CallbackTo[Future[A]])(implicit ec: ExecutionContext): AsyncCallback[A] =
    c.asAsyncCallback.flatMap(fromFuture(_))

  def fromJsPromise[A](pa: => js.Thenable[A]): AsyncCallback[A] =
    AsyncCallback(f => Callback {
      def complete(e: Try[A]): Thenable[Unit] = f(e).asAsyncCallback.asCallbackToJsPromise.runNow()
      type R = Unit | Thenable[Unit]
      val ok: A   => R = a => complete(Success(a))
      val ko: Any => R = e => complete(Failure(e match {
        case t: Throwable => t
        case a            => js.JavaScriptException(e)
      }))
      pa.`then`[Unit](ok, ko: js.Function1[Any, R])
    })

  def fromCallbackToJsPromise[A](c: CallbackTo[js.Promise[A]]): AsyncCallback[A] =
    c.asAsyncCallback.flatMap(fromJsPromise(_))

  @inline implicit def asyncCallbackCovariance[A, B >: A](c: AsyncCallback[A]): AsyncCallback[B] =
    c.widen

  /** Not literally tail-recursive because AsyncCallback is continuation-based, but this utility in this shape may still
    * be useful.
    */
  def tailrec[A, B](a: A)(f: A => AsyncCallback[Either[A, B]]): AsyncCallback[B] =
    f(a).flatMap {
      case Left(a2) => tailrec(a2)(f)
      case Right(b) => pure(b)
    }
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
  * - [[>>()]] & [[<<()]]
  * - [[flatTap()]]
  *
  * The following methods are effectively parallel:
  * - [[*>()]] & [[<*()]]
  * - [[race()]]
  * - [[zip()]] & [[zipWith()]]
  * - `AsyncCallback.traverse` et al
  *
  * In order to actually run this, or get it into a shape in which in can be run, use one of the following:
  * - [[toCallback]] <-- most common
  * - [[asCallbackToFuture]]
  * - [[asCallbackToJsPromise]]
  * - [[unsafeToFuture()]]
  * - [[unsafeToJsPromise()]]
  *
  * A good example is the [Ajax 2 demo](https://japgolly.github.io/scalajs-react/#examples/ajax-2).
  *
  * @tparam A The type of data asynchronously produced on success.
  */
final class AsyncCallback[A] private[AsyncCallback] (val completeWith: (Try[A] => Callback) => Callback) extends AnyVal {

  def widen[B >: A]: AsyncCallback[B] =
    new AsyncCallback(completeWith)

  def map[B](f: A => B): AsyncCallback[B] =
    AsyncCallback(g => completeWith(e => g(e.flatMap(a => catchAll(f(a))))))

  /** Alias for `map`. */
  @inline def |>[B](f: A => B): AsyncCallback[B] =
    map(f)

  def flatMap[B](f: A => AsyncCallback[B]): AsyncCallback[B] =
    AsyncCallback(g => completeWith {
      case Success(a) => catchAll(f(a)) match {
        case Success(next) => next.completeWith(g)
        case Failure(e)    => g(Failure(e))
      }
      case Failure(e) => g(Failure(e))
    })

  /** Alias for `flatMap`. */
  @inline def >>=[B](g: A => AsyncCallback[B]): AsyncCallback[B] =
    flatMap(g)

  def flatten[B](implicit ev: A => AsyncCallback[B]): AsyncCallback[B] =
    flatMap(ev)

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
      var ra: Option[Try[A]] = None
      var rb: Option[Try[B]] = None
      var r: Option[Try[C]] = None

      val respond = Callback {
        if (r.isEmpty) {
          (ra, rb) match {
            case (Some(Success(a)), Some(Success(b))) => r = Some(catchAll(f(a, b)))
            case (Some(Failure(e)), _               ) => r = Some(Failure(e))
            case (_               , Some(Failure(e))) => r = Some(Failure(e))
            case (Some(Success(_)), None            )
               | (None            , Some(Success(_)))
               | (None            , None            ) => ()
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
    AsyncCallback(f => completeWith(e => f(Success(e match {
      case Success(a) => Right(a)
      case Failure(t) => Left(t)
    }))))

  def attemptTry: AsyncCallback[Try[A]] =
    AsyncCallback(f => completeWith(e => f(Success(e))))

  def handleError(f: Throwable => AsyncCallback[A]): AsyncCallback[A] =
    AsyncCallback(g => completeWith {
      case r@ Success(_) => g(r)
      case Failure(t)    => f(t).completeWith(g)
    })

  def maybeHandleError(f: PartialFunction[Throwable, AsyncCallback[A]]): AsyncCallback[A] =
    AsyncCallback(g => completeWith {
      case r@ Success(_) => g(r)
      case l@ Failure(t) => f.lift(t) match {
        case Some(n) => n.completeWith(g)
        case None    => g(l)
      }
    })

  /** Return a version of this callback that will only execute once, and reuse the result for all
    * other invocations.
    */
  def memo(): AsyncCallback[A] = {
    var result: Option[AsyncCallback[A]] = None
    def set(r: AsyncCallback[A]) = {result = Some(r); r}
    AsyncCallback.byName {
      result.getOrElse {
        val first = attemptTry.flatMap(t => AsyncCallback.byName(set(AsyncCallback.const(t))))
        val promise = first.unsafeToJsPromise()
        result getOrElse set(AsyncCallback.fromJsPromise(promise))
      }
    }
  }

  /** Conditional execution of this callback.
    *
    * @param cond The condition required to be `true` for this callback to execute.
    * @return `Some` result of the callback executed, else `None`.
    */
  def when(cond: => Boolean): AsyncCallback[Option[A]] =
    AsyncCallback(f => if (cond) completeWith(ea => f(ea.map(Some(_)))) else f(Success(None)))

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
      completeWith(t => Callback(p.complete(t))).runNow()
      p.future
    }

  def asCallbackToJsPromise: CallbackTo[js.Promise[A]] =
    for {
      (p, pc) <- newJsPromise[A]
      _       <- completeWith(pc)
    } yield p

  def unsafeToFuture(): Future[A] =
    asCallbackToFuture.runNow()

  def unsafeToJsPromise(): js.Promise[A] =
    asCallbackToJsPromise.runNow()

}
