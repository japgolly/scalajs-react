package japgolly.scalajs.react.effects

import cats.MonadError
import cats.effect.{Effect, ExitCase, IO, SyncIO}
import japgolly.scalajs.react.{AsyncCallback, Callback, CatsReact}
import scala.util.Either

object AsyncCallbackEffects {
  private val asyncCallbackMonadError: MonadError[AsyncCallback, Throwable] =
    CatsReact.reactAsyncCallbackCatsInstance

  implicit object AsyncCallbackEffect extends Effect[AsyncCallback] {

    // Bracket[AsyncCallback, Throwable]
    override def bracketCase[A, B](acquire: AsyncCallback[A])(use: A => AsyncCallback[B])(release: (A, ExitCase[Throwable]) => AsyncCallback[Unit]): AsyncCallback[B] =
      acquire.flatMap { a =>
        use(a).attempt.flatMap {
          case Right(b) => release(a, ExitCase.Completed).ret(b)
          case Left(e)  => release(a, ExitCase.Error(e)) >> AsyncCallback.throwException(e)
        }
      }
    override def pure[A](x: A): AsyncCallback[A] = asyncCallbackMonadError.pure(x)
    override def flatMap[A, B](fa: AsyncCallback[A])(f: A => AsyncCallback[B]): AsyncCallback[B] = asyncCallbackMonadError.flatMap(fa)(f)
    override def tailRecM[A, B](a: A)(f: A => AsyncCallback[Either[A, B]]): AsyncCallback[B] = asyncCallbackMonadError.tailRecM(a)(f)
    override def raiseError[A](e: Throwable): AsyncCallback[A] = asyncCallbackMonadError.raiseError(e)
    override def handleErrorWith[A](fa: AsyncCallback[A])(f: Throwable => AsyncCallback[A]): AsyncCallback[A] = asyncCallbackMonadError.handleErrorWith(fa)(f)

    // Sync[AsyncCallback]
    override def suspend[A](thunk: => AsyncCallback[A]): AsyncCallback[A] =
      AsyncCallback.suspend(thunk)

    // LiftIO[AsyncCallback]
    override def liftIO[A](ioa: IO[A]): AsyncCallback[A] = {
      AsyncCallback(cb =>
        Callback(ioa.unsafeRunAsync(e => cb(e.toTry)))
      )
    }

    // Async[AsyncCallback]
    def async[A](k: (Either[Throwable, A] => Unit) => Unit): AsyncCallback[A] = {
      AsyncCallback { accb =>
        val convertCallback: Either[Throwable, A] => Unit =
          either => accb(either.toTry).runNow()

        Callback {
          k(convertCallback)
        }
      }
    }

    override def asyncF[A](k: (Either[Throwable, A] => Unit) => AsyncCallback[Unit]): AsyncCallback[A] = {
      AsyncCallback { accb =>
        val convertCallback: Either[Throwable, A] => Unit =
          either => accb(either.toTry).runNow()

        k(convertCallback).toCallback
      }
    }

    // Effect[AsyncCallback]
    override def runAsync[A](fa: AsyncCallback[A])(cb: Either[Throwable, A] => IO[Unit]): SyncIO[Unit] = {
      SyncIO(
        fa.attempt.map(cb.andThen(_.unsafeRunAsyncAndForget())).toCallback.runNow()
      )
    }
  }
}