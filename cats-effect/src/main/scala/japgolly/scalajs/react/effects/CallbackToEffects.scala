package japgolly.scalajs.react.effects

import cats.effect._
import cats.effect.kernel.CancelScope
import cats.~>
import japgolly.scalajs.react.{Callback, CallbackTo}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object CallbackToEffects {

  implicit lazy val callbackToSyncIO: (CallbackTo ~> SyncIO) =
    new (CallbackTo ~> SyncIO) {
      override def apply[A](f: CallbackTo[A]): SyncIO[A] =
        SyncIO(f.runNow())
    }

  implicit lazy val syncIOToCallback: (SyncIO ~> CallbackTo) =
    new (SyncIO ~> CallbackTo) {
      override def apply[A](f: SyncIO[A]): CallbackTo[A] =
        CallbackTo(f.unsafeRunSync())
    }

  // ===================================================================================================================

  implicit object CallbackToSync extends Sync[CallbackTo] {

    override def unit: CallbackTo[Unit] =
      Callback.empty

    override def pure[A](x: A): CallbackTo[A] =
      CallbackTo.pure(x)

    override def raiseError[A](e: Throwable): CallbackTo[A] =
      CallbackTo.throwException(e)

    override def handleErrorWith[A](fa: CallbackTo[A])(f: Throwable => CallbackTo[A]): CallbackTo[A] =
      fa.handleError(f)

    override def flatMap[A, B](fa: CallbackTo[A])(f: A => CallbackTo[B]): CallbackTo[B] =
      fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => CallbackTo[Either[A,B]]): CallbackTo[B] =
      CallbackTo.tailrec(a)(f)

    override def rootCancelScope: CancelScope =
      CancelScope.Uncancelable

    override def forceR[A, B](fa: CallbackTo[A])(fb: CallbackTo[B]): CallbackTo[B] =
      fa.attempt >> fb

    override def uncancelable[A](body: Poll[CallbackTo] => CallbackTo[A]): CallbackTo[A] =
      body(pollId)

    private lazy val pollId: Poll[CallbackTo] =
      new Poll[CallbackTo] {
        override def apply[A](fa: CallbackTo[A]) = fa
      }

    override def canceled: CallbackTo[Unit] =
      Callback.empty

    override def onCancel[A](fa: CallbackTo[A], fin: CallbackTo[Unit]): CallbackTo[A] =
      fin.async.toCallback >> fa

    override def monotonic: CallbackTo[FiniteDuration] =
      realTime

    override def realTime: CallbackTo[FiniteDuration] =
      CallbackTo(FiniteDuration(System.nanoTime(), TimeUnit.NANOSECONDS))

    override def suspend[A](hint: kernel.Sync.Type)(thunk: => A): CallbackTo[A] =
      CallbackTo(thunk)
  }
}
