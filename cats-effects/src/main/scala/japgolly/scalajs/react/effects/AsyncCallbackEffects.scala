package japgolly.scalajs.react.effects

import cats.effect.{Async, Bracket, Effect, ExitCase, IO, LiftIO, Sync, SyncIO}
import cats.{Defer, MonadError}
import japgolly.scalajs.react.{AsyncCallback, Callback, CatsReact}

import scala.util.{Either, Failure, Success}

trait AsyncCallbackEffects {
  private val asyncCallbackMonadError: MonadError[AsyncCallback, Throwable] =
    CatsReact.reactAsyncCallbackCatsInstance

  trait AsyncCallbackDefer extends Defer[AsyncCallback] {
    override def defer[A](fa: => AsyncCallback[A]): AsyncCallback[A] =
      AsyncCallback.byName(fa)
  }

  implicit val asyncCallbackDefer: Defer[AsyncCallback] = new AsyncCallbackDefer {}

  trait AsyncCallbackBracket extends Bracket[AsyncCallback, Throwable] {
    override def bracketCase[A, B](acquire: AsyncCallback[A])(use: A => AsyncCallback[B])(release: (A, ExitCase[Throwable]) => AsyncCallback[Unit]): AsyncCallback[B] =
      acquire.flatMap { a =>
        handleErrorWith(use(a))(t => release(a, ExitCase.Error(t)).flatMap(_ => raiseError(t)))
          .flatMap(b => release(a, ExitCase.Completed).map(_ => b))
      }
    override def pure[A](x: A): AsyncCallback[A] = asyncCallbackMonadError.pure(x)
    override def flatMap[A, B](fa: AsyncCallback[A])(f: A => AsyncCallback[B]): AsyncCallback[B] = asyncCallbackMonadError.flatMap(fa)(f)
    override def tailRecM[A, B](a: A)(f: A => AsyncCallback[Either[A, B]]): AsyncCallback[B] = asyncCallbackMonadError.tailRecM(a)(f)
    override def raiseError[A](e: Throwable): AsyncCallback[A] = asyncCallbackMonadError.raiseError(e)
    override def handleErrorWith[A](fa: AsyncCallback[A])(f: Throwable => AsyncCallback[A]): AsyncCallback[A] = asyncCallbackMonadError.handleErrorWith(fa)(f)
  }

  implicit val asyncCallbackBracket: Bracket[AsyncCallback, Throwable] = new AsyncCallbackBracket {}

  trait AsyncCallbackSync extends AsyncCallbackBracket with AsyncCallbackDefer with Sync[AsyncCallback] {
    override def suspend[A](thunk: => AsyncCallback[A]): AsyncCallback[A] =
      AsyncCallback.byName(thunk)
  }

  implicit val asyncCallbackSync: Sync[AsyncCallback] = new AsyncCallbackSync {}

  trait AsyncCallbackLiftIO extends LiftIO[AsyncCallback] {
    def liftIO[A](ioa: IO[A]): AsyncCallback[A] = {
      AsyncCallback(cb =>
        ioa.attempt.unsafeRunSync().fold(t => cb(Failure(t)), a => cb(Success(a)))
      )
    }
  }

  implicit val asyncCallbackLiftIO: LiftIO[AsyncCallback] = new AsyncCallbackLiftIO {}

  trait AsyncCallbackAsync extends AsyncCallbackSync with AsyncCallbackLiftIO with Async[AsyncCallback] {
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
  }

  implicit val asyncCallbackAsync: Async[AsyncCallback] = new AsyncCallbackAsync {}

  trait AsyncCallbackEffect extends AsyncCallbackAsync with Effect[AsyncCallback] {
    override def runAsync[A](fa: AsyncCallback[A])(cb: Either[Throwable, A] => IO[Unit]): SyncIO[Unit] = {
      SyncIO(
        fa.attempt.map(cb.andThen(_.unsafeRunAsyncAndForget())).toCallback.runNow()
      )
    }
  }

  implicit val asyncCallbackEffect: Effect[AsyncCallback] = new AsyncCallbackEffect {}
}

object AsyncCallbackEffects extends AsyncCallbackEffects

