package japgolly.scalajs.react.internal

import cats._
import cats.data.Ior

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._

import scala.annotation.tailrec

/**
  * Created by alonsodomin on 13/03/2017.
  */
trait CatsReactInstances {

  implicit final lazy val reactCallbackCatsInstance: MonadError[CallbackTo, Throwable] = new MonadError[CallbackTo, Throwable] {
    override def pure[A](x: A): CallbackTo[A] = CallbackTo.pure(x)

    override def map[A, B](fa: CallbackTo[A])(f: A => B): CallbackTo[B] =
      fa.map(f)

    override def flatMap[A, B](fa: CallbackTo[A])(f: A => CallbackTo[B]): CallbackTo[B] =
      fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => CallbackTo[Either[A, B]]): CallbackTo[B] = CallbackTo {
      @tailrec
      def go(a: A): B = f(a).runNow() match {
        case Left(a0) => go(a0)
        case Right(b) => b
      }
      go(a)
    }

    override def raiseError[A](e: Throwable): CallbackTo[A] =
      CallbackTo(throw e)

    override def handleErrorWith[A](fa: CallbackTo[A])(f: Throwable => CallbackTo[A]): CallbackTo[A] =
      fa.attempt.flatMap {
        case Right(a) => CallbackTo pure a
        case Left(t)  => f(t)
      }
  }

  implicit final lazy val reactCallbackOptionCatsInstance: Monad[CallbackOption] = new Monad[CallbackOption] {
    override def pure[A](x: A): CallbackOption[A] = CallbackOption.pure(x)

    override def map[A, B](fa: CallbackOption[A])(f: A => B): CallbackOption[B] =
      fa.map(f)

    override def flatMap[A, B](fa: CallbackOption[A])(f: A => CallbackOption[B]): CallbackOption[B] =
      fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => CallbackOption[Either[A, B]]): CallbackOption[B] =
      CallbackOption.liftOption {
        @tailrec
        def go(a: A): Option[B] = f(a).asCallback.runNow() match {
          case Some(Left(a0)) => go(a0)
          case Some(Right(b)) => Some(b)
          case None           => None
        }
        go(a)
      }
  }

  implicit final lazy val catsIdToReactCallback: (Id ~> CallbackTo) = new (Id ~> CallbackTo) {
    override def apply[A](fa: Id[A]): CallbackTo[A] = CallbackTo(fa)
  }

  implicit final lazy val catsReactCallbackToItself: (CallbackTo ~> CallbackTo) = new (CallbackTo ~> CallbackTo) {
    override def apply[A](fa: CallbackTo[A]): CallbackTo[A] = fa
  }

  implicit final def reusabilityIor[A: Reusability, B: Reusability]: Reusability[A Ior B] = Reusability {
    case (Ior.Both(a, b), Ior.Both(c, d)) => (a ~=~ c) && (b ~=~ d)
    case (Ior.Left(a), Ior.Left(b))       => a ~=~ b
    case (Ior.Right(a), Ior.Right(b))     => a ~=~ b
    case _                                => false
  }

}
