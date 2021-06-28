package japgolly.scalajs.react.internal

import cats._
import cats.arrow.{Profunctor => CatsProfunctor}
import cats.data.Ior
import japgolly.scalajs.react.{ReactExtensions, Ref, Reusability}

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
}
