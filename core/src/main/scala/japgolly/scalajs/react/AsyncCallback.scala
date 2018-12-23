package japgolly.scalajs.react

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
}

final class AsyncCallback[+A] private[AsyncCallback] (
      private[AsyncCallback] val completeWith: (Either[Throwable, A] => Callback) => Callback
    ) extends AnyVal {

  def toCallback: Callback =
    completeWith(AsyncCallback.defaultCompleteWith)

  def map[B](f: A => B): AsyncCallback[B] =
    AsyncCallback(g => completeWith(e => g(e.map(f))))

  def flatMap[B](f: A => AsyncCallback[B]): AsyncCallback[B] =
    AsyncCallback(g => completeWith {
      case Right(a) => f(a).completeWith(g)
      case Left(e)  => g(Left(e))
    })

  def flatten[B](implicit ev: AsyncCallback[A] <:< AsyncCallback[AsyncCallback[B]]): AsyncCallback[B] =
    ev(this).flatMap(identity)

  def attempt: AsyncCallback[Either[Throwable, A]] =
    AsyncCallback(f => completeWith(e => f(Right(e))))

  /** Sequence the argument a callback to run after this, discarding any value produced by this. */
  def >>[B](runNext: AsyncCallback[B]): AsyncCallback[B] =
    flatMap(_ => runNext)

  /**
    * Alias for `>>`.
    *
    * Where `>>` is often associated with Monads, `*>` is often associated with Applicatives.
    */
  @inline def *>[B](runNext: AsyncCallback[B]): AsyncCallback[B] =
    >>(runNext)

  /**
   * Sequence a callback to run before this, discarding any value produced by it.
   */
  @inline def <<[B](runBefore: AsyncCallback[B]): AsyncCallback[A] =
    runBefore >> this

  /** Convenient version of `<<` that accepts an Option */
  def <<?[B](prev: Option[AsyncCallback[B]]): AsyncCallback[A] =
    prev.fold(this)(_ >> this)

  def zip[B](that: AsyncCallback[B]): AsyncCallback[(A, B)] =
    AsyncCallback(f => CallbackTo {
      var ra: Option[Either[Throwable, A]] = None
      var rb: Option[Either[Throwable, B]] = None
      var r: Option[Either[Throwable, (A, B)]] = None

      val respond = Callback {
        if (r.isEmpty) {
          (ra, rb) match {
            case (Some(Right(a)), Some(Right(b))) => r = Some(Right(a, b))
            case (Some(Left(e)) , _             ) => r = Some(Left(e))
            case (_             , Some(Left(e)) ) => r = Some(Left(e))
            case (Some(Right(_)), None          )
               | (None          , Some(Right(_)))
               | (None          , None          ) => ()
          }
          r.foreach(f(_).runNow())
        }
      }

      this.completeWith(e => Callback {ra = Some(e)} >> respond) >>
      that.completeWith(e => Callback {rb = Some(e)} >> respond)
    }.flatten)

  def race[B](that: AsyncCallback[B]): AsyncCallback[Either[A, B]] =
    AsyncCallback.first(f =>
      this.completeWith(e => f(e.map(Left(_)))) >>
      that.completeWith(e => f(e.map(Right(_)))))

  def delay(dur: FiniteDuration): AsyncCallback[A] =
    delayMs(dur.toMillis.toDouble)

  def delayMs(milliseconds: Double): AsyncCallback[A] =
    AsyncCallback(f => Callback {
      setTimeout(milliseconds) {
        completeWith(f).runNow()
      }
    })

  def asCallbackToFuture[B >: A]: CallbackTo[Future[B]] =
    CallbackTo {
      val p = scala.concurrent.Promise[B]()
      completeWith(ea => Callback(ea.fold(p.failure, p.success)))
      p.future
    }

  def asCallbackToJsPromise[B >: A]: CallbackTo[js.Promise[B]] =
    CallbackTo {
      new js.Promise[B]((respond, reject) => {
        def fail(t: Throwable) =
          reject(t match {
            case js.JavaScriptException(e) => e
            case e                         => e
          })
        completeWith(ea => Callback(ea.fold(fail, respond(_))))
      })
    }

  def unsafeToFuture[B >: A](): Future[B] =
    asCallbackToFuture[B].runNow()

  def unsafeToJsPromise[B >: A](): js.Promise[B] =
    asCallbackToJsPromise[B].runNow()

  def handleError[B >: A](f: Throwable => AsyncCallback[B]): AsyncCallback[B] =
    AsyncCallback(g => completeWith {
      case r@ Right(_) => g(r)
      case Left(t)     => f(t).completeWith(g)
    })

  def handleErrorPartially[B >: A](f: PartialFunction[Throwable, AsyncCallback[B]]): AsyncCallback[B] =
    AsyncCallback(g => completeWith {
      case r@ Right(_) => g(r)
      case l@ Left(t)  => f.lift(t) match {
        case Some(n) => n.completeWith(g)
        case None    => g(l)
      }
    })
}
