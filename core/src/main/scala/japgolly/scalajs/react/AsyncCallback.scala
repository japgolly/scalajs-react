package japgolly.scalajs.react

import scala.concurrent.{ExecutionContext, Future}

final case class AsyncCallback[+A](completeWith: (Either[Throwable, A] => Callback) => Callback) {
  def map[B](f: A => B): AsyncCallback[B] =
    AsyncCallback(g => completeWith(e => g(e.map(f))))

  def flatMap[B](f: A => AsyncCallback[B]): AsyncCallback[B] =
    AsyncCallback(g => completeWith {
      case Right(a) => f(a).completeWith(g)
      case Left(e)  => g(Left(e))
    })

  def toCallback: Callback =
    completeWith(AsyncCallback.defaultCompleteWith)

  def attempt: AsyncCallback[Either[Throwable, A]] =
    AsyncCallback(f => completeWith(e => f(Right(e))))

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
    AsyncCallback(f =>
      CallbackTo {
        var called = false

        def respond(r: Either[Throwable, Either[A, B]]): Callback =
          Callback.unless(called)(Callback {called = true} >> f(r))

        this.completeWith(e => respond(e.map(Left(_)))) >>
        that.completeWith(e => respond(e.map(Right(_))))
      }.flatten
    )
}

object AsyncCallback {
  private[AsyncCallback] val defaultCompleteWith: Either[Throwable, Any] => Callback =
    _ => Callback.empty

  private def tryE[A](a: => A): Either[Throwable, A] =
    try Right(a)
    catch {case t: Throwable => Left(t) }

  def point[A](a: => A): AsyncCallback[A] =
    AsyncCallback(_(tryE(a)))

  def pure[A](a: A): AsyncCallback[A] = {
    val r = Right(a)
    AsyncCallback(_(r))
  }

  def fromCallback[A](c: CallbackTo[A]): AsyncCallback[A] =
    AsyncCallback(c.attempt.flatMap)

  def fromFuture[A](fa: => Future[A])(implicit ec: ExecutionContext): AsyncCallback[A] =
    AsyncCallback(f => Callback {
      fa.onComplete {
        case scala.util.Success(a) => f(Right(a)).runNow()
        case scala.util.Failure(e) => f(Left(e)).runNow()
      }
    })

  def fromCallbackToFuture[A](c: CallbackTo[Future[A]])(implicit ec: ExecutionContext): AsyncCallback[A] =
    fromCallback(c).flatMap(fromFuture(_))
}