package japgolly.scalajs.react.effects

import cats.MonadError
import cats.effect.{ExitCase, Sync}
import japgolly.scalajs.react.{CallbackTo, CatsReact}

object CallbackToEffects {
  private val callbackToMonadError: MonadError[CallbackTo, Throwable] =
    CatsReact.reactCallbackCatsInstance

  implicit object CallbackToSync extends Sync[CallbackTo] {

    // Bracket[CallbackTo, Throwable]
    override def bracketCase[A, B](acquire: CallbackTo[A])(use: A => CallbackTo[B])(release: (A, ExitCase[Throwable]) => CallbackTo[Unit]): CallbackTo[B] =
      acquire.flatMap { a =>
        use(a).attempt.flatMap {
          case Right(b) => release(a, ExitCase.Completed).ret(b)
          case Left(e)  => release(a, ExitCase.Error(e)) >> CallbackTo.throwException(e)
        }
      }
    override def pure[A](x: A): CallbackTo[A] = callbackToMonadError.pure(x)
    override def flatMap[A, B](fa: CallbackTo[A])(f: A => CallbackTo[B]): CallbackTo[B] = callbackToMonadError.flatMap(fa)(f)
    override def tailRecM[A, B](a: A)(f: A => CallbackTo[Either[A, B]]): CallbackTo[B] = callbackToMonadError.tailRecM(a)(f)
    override def raiseError[A](e: Throwable): CallbackTo[A] = callbackToMonadError.raiseError(e)
    override def handleErrorWith[A](fa: CallbackTo[A])(f: Throwable => CallbackTo[A]): CallbackTo[A] = callbackToMonadError.handleErrorWith(fa)(f)

    // Sync[CallbackTo]
    override def suspend[A](thunk: => CallbackTo[A]): CallbackTo[A] =
      CallbackTo.byName(thunk)
  }
}
