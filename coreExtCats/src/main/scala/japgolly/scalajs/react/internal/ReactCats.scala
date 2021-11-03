package japgolly.scalajs.react.internal

import cats._
import cats.arrow.{Profunctor => CatsProfunctor}
import cats.data.Ior
import japgolly.scalajs.react.{ReactExtensions, Ref, Reusability}
import japgolly.scalajs.react.util.Effect
import scala.annotation.tailrec

object ReactCats {
  import ReactExtensions._

  object ReactCatsExtReusabilityObj {

    /** Compare using cat's Eq */
    def byEq[A](implicit eq: Eq[A]): Reusability[A] =
      new Reusability[A](eq.eqv)

    def byRefOrEq[A <: AnyRef : Eq]: Reusability[A] =
      Reusability.byRef[A] || byEq[A]
  }

  implicit def reactCatsReusabilityIor[A: Reusability, B: Reusability]: Reusability[A Ior B] =
    Reusability {
      case (Ior.Both(a, b), Ior.Both(c, d)) => (a ~=~ c) && (b ~=~ d)
      case (Ior.Left(a), Ior.Left(b))       => a ~=~ b
      case (Ior.Right(a), Ior.Right(b))     => a ~=~ b
      case _                                => false
    }

  implicit def reactCatsProfunctorRefFull[F[_], X]: CatsProfunctor[Ref.FullF[F, *, X, *]] =
    new CatsProfunctor[Ref.FullF[F, *, X, *]] {
      override def lmap[A, B, C](f: Ref.FullF[F, A, X, B])(m: C => A) = f.contramap(m)
      override def rmap[A, B, C](f: Ref.FullF[F, A, X, B])(m: B => C) = f.map(m)
      override def dimap[A, B, C, D](r: Ref.FullF[F, A, X, B])(f: C => A)(g: B => D) = r.contramap(f).map(g)
    }

  implicit def reactCatsSyncEffectMonadThrow[F[_]](implicit F: Effect.Sync[F]): MonadThrow[F] =
    new MonadThrow[F] {
      override def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.flatMap(fa)(f)

      override def tailRecM[A, B](a: A)(f: A => F[Either[A,B]]): F[B] = F.pure {
        @tailrec
        def go(a: A): B =
          F.runSync(f(a)) match {
            case Left(n)  => go(n)
            case Right(b) => b
          }
        go(a)
      }

      override def pure[A](a: A): F[A] = F.pure(a)

      override def raiseError[A](e: Throwable): F[A] = F.throwException(e)

      override def handleErrorWith[A](fa: F[A])(f: Throwable => F[A]): F[A] =
        F.handleError(fa)(f)
    }

  implicit def reactCatsSyncEffectMonoid[F[_]: Effect.Sync, A: Monoid]: Monoid[F[A]] =
    Applicative.monoid[F, A]
}
