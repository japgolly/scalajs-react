package japgolly.scalajs.react.internal

import cats._
import cats.arrow.{Arrow, Choice}
import cats.data.Ior
import japgolly.scalajs.react._

trait CatsReactInstances {

  implicit final lazy val reactCallbackCatsInstance: MonadError[CallbackTo, Throwable] = new MonadError[CallbackTo, Throwable] {
    override def pure[A](x: A): CallbackTo[A] =
      CallbackTo.pure(x)

    override def map[A, B](fa: CallbackTo[A])(f: A => B): CallbackTo[B] =
      fa.map(f)

    override def flatMap[A, B](fa: CallbackTo[A])(f: A => CallbackTo[B]): CallbackTo[B] =
      fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => CallbackTo[Either[A, B]]): CallbackTo[B] =
      CallbackTo.tailrec(a)(f)

    override def raiseError[A](e: Throwable): CallbackTo[A] =
      CallbackTo.throwException(e)

    override def handleErrorWith[A](fa: CallbackTo[A])(f: Throwable => CallbackTo[A]): CallbackTo[A] =
      fa.handleError(f)
  }

  implicit final lazy val reactAsyncCallbackCatsInstance: MonadError[AsyncCallback, Throwable] = new MonadError[AsyncCallback, Throwable] {

    override def pure[A](x: A): AsyncCallback[A] =
      AsyncCallback.pure(x)

    override def ap[A, B](ff: AsyncCallback[A => B])(fa: AsyncCallback[A]) =
      ff.zipWith(fa)(_(_))

    override def ap2[A, B, Z](ff: AsyncCallback[(A, B) => Z])(fa: AsyncCallback[A], fb: AsyncCallback[B]) =
      ff.zipWith(fa.zip(fb))(_.tupled(_))

    override def map2[A, B, Z](fa: AsyncCallback[A], fb: AsyncCallback[B])(f: (A, B) => Z) =
      fa.zipWith(fb)(f)

    override def map[A, B](fa: AsyncCallback[A])(f: A => B): AsyncCallback[B] =
      fa.map(f)

    override def flatMap[A, B](fa: AsyncCallback[A])(f: A => AsyncCallback[B]): AsyncCallback[B] =
      fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => AsyncCallback[Either[A, B]]): AsyncCallback[B] =
      AsyncCallback.tailrec(a)(f)

    override def raiseError[A](e: Throwable): AsyncCallback[A] =
      AsyncCallback.throwException(e)

    override def handleErrorWith[A](fa: AsyncCallback[A])(f: Throwable => AsyncCallback[A]): AsyncCallback[A] =
      fa.handleError(f)
    }

  implicit final lazy val reactCallbackOptionCatsInstance: Monad[CallbackOption] = new Monad[CallbackOption] {
    override def pure[A](x: A): CallbackOption[A] = CallbackOption.pure(x)

    override def map[A, B](fa: CallbackOption[A])(f: A => B): CallbackOption[B] =
      fa.map(f)

    override def flatMap[A, B](fa: CallbackOption[A])(f: A => CallbackOption[B]): CallbackOption[B] =
      fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => CallbackOption[Either[A, B]]): CallbackOption[B] =
      CallbackOption.tailrec(a)(f)
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

  implicit final lazy val reactRefFnCatsInstance: Profunctor[Ref.Fn] =
    new Profunctor[Ref.Fn] {
      override def lmap[A, B, C](f: Ref.Fn[A, B])(m: C => A) = f.contramap(m)
      override def rmap[A, B, C](f: Ref.Fn[A, B])(m: B => C) = f.map(m)
    }

  implicit final def reactRefFullCatsInstance[X]: Profunctor[Ref.Full[*, X, *]] =
    new Profunctor[Ref.Full[*, X, *]] {
      override def lmap[A, B, C](f: Ref.Full[A, X, B])(m: C => A) = f.contramap(m)
      override def rmap[A, B, C](f: Ref.Full[A, X, B])(m: B => C) = f.map(m)
    }
}
