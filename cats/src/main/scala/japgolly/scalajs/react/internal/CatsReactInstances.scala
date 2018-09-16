package japgolly.scalajs.react.internal

import cats._
import cats.arrow.{Arrow, Choice}
import cats.data.Ior
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._

trait CatsReactInstances {

  implicit final lazy val reactCallbackCatsInstance: MonadError[CallbackTo, Throwable] = new MonadError[CallbackTo, Throwable] {
    override def pure[A](x: A): CallbackTo[A] = CallbackTo.pure(x)

    override def map[A, B](fa: CallbackTo[A])(f: A => B): CallbackTo[B] =
      fa.map(f)

    override def flatMap[A, B](fa: CallbackTo[A])(f: A => CallbackTo[B]): CallbackTo[B] =
      fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => CallbackTo[Either[A, B]]): CallbackTo[B] =
      CallbackTo.tailrec(a)(f)

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
      CallbackOption.tailrec(a)(f)
  }

  implicit lazy val reactCallbackKleisliCatsInstance: Arrow[CallbackKleisli] with Choice[CallbackKleisli] =
    new Arrow[CallbackKleisli] with Choice[CallbackKleisli] {
      override def lift[A, B](f: A => B): CallbackKleisli[A, B] =
        CallbackKleisli.lift(f)

      override def choice[A, B, C](f: CallbackKleisli[A, C], g: CallbackKleisli[B, C]): CallbackKleisli[Either[A, B], C] =
        CallbackKleisli.choice(f, g)

      override def compose[A, B, C](f: CallbackKleisli[B, C], g: CallbackKleisli[A, B]): CallbackKleisli[A, C] =
        g >=> f

      override def first[A, B, C](f: CallbackKleisli[A, B]): CallbackKleisli[(A, C), (B, C)] =
        f.strongL

      override def second[A, B, C](f: CallbackKleisli[A, B]): CallbackKleisli[(C, A), (C, B)] =
        f.strongR

      override def id[A]: CallbackKleisli[A, A] =
        CallbackKleisli.ask

      override def split[A, B, C, D](f: CallbackKleisli[A, B], g: CallbackKleisli[C, D]): CallbackKleisli[(A, C), (B, D)] =
        CallbackKleisli.split(f, g)

      override def dimap[A, B, C, D](fab: CallbackKleisli[A, B])(f: C => A)(g: B => D) =
        fab.dimap(f, g)

      override def lmap[A, B, C](f: CallbackKleisli[A, B])(g: C => A): CallbackKleisli[C, B] =
        f.contramap(g)

      override def rmap[A, B, C](f: CallbackKleisli[A, B])(g: B => C): CallbackKleisli[A, C] =
        f.map(g)
    }

  implicit def reactCallbackKleisliACatsInstance[A]: MonadError[CallbackKleisli[A, ?], Throwable] =
    new MonadError[CallbackKleisli[A, ?], Throwable] {

      override def pure[B](b: B): CallbackKleisli[A, B] =
        CallbackKleisli const reactCallbackCatsInstance.pure(b)

      override def flatMap[B, C](fa: CallbackKleisli[A, B])(f: B => CallbackKleisli[A, C]): CallbackKleisli[A, C] =
        fa >>= f

      override def map[B, C](fa: CallbackKleisli[A, B])(f: B => C): CallbackKleisli[A, C] =
        fa map f

      override def tailRecM[S, B](s: S)(f: S => CallbackKleisli[A, Either[S, B]]): CallbackKleisli[A, B] =
        CallbackKleisli.tailrec(s)(f)

      override def raiseError[B](e: Throwable): CallbackKleisli[A, B] =
        CallbackKleisli const reactCallbackCatsInstance.raiseError(e)

      override def handleErrorWith[B](fb: CallbackKleisli[A, B])(f: Throwable => CallbackKleisli[A, B]): CallbackKleisli[A, B] =
        CallbackKleisli(a => reactCallbackCatsInstance.handleErrorWith(fb(a))(f.andThen(_ (a))))
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

  implicit final def reactRefFullCatsInstance[X]: Profunctor[Ref.Full[?, X, ?]] =
    new Profunctor[Ref.Full[?, X, ?]] {
      override def lmap[A, B, C](f: Ref.Full[A, X, B])(m: C => A) = f.contramap(m)
      override def rmap[A, B, C](f: Ref.Full[A, X, B])(m: B => C) = f.map(m)
    }
}
