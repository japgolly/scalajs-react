package japgolly.scalajs.react

import cats.arrow.Profunctor
import cats.data.Ior
import cats.kernel.Eq
import cats.{MonadThrow, Monoid}
import japgolly.scalajs.react.internal.CoreGeneral.Key
import japgolly.scalajs.react.util.Effect
import scala.annotation.nowarn

object ReactCats extends ReactCats

@nowarn("cat=unused")
trait ReactCats {
  import internal.{ReactCats => X}

  @inline final implicit def reactCatsEqKey: Eq[Key] =
    Eq.fromUniversalEquals

  @inline final implicit def ReactCatsExtReusabilityObj(e: Reusability.type): X.ReactCatsExtReusabilityObj.type =
    X.ReactCatsExtReusabilityObj

  @inline final implicit def reactCatsReusabilityIor[A: Reusability, B: Reusability]: Reusability[A Ior B] =
    X.reactCatsReusabilityIor

  @inline final implicit def reactCatsProfunctorRefFull[F[_], X]: Profunctor[Ref.FullF[F, *, X, *]] =
    X.reactCatsProfunctorRefFull

  @inline final implicit def reactCatsSyncEffectMonadThrow[F[_]: Effect]: MonadThrow[F] =
    X.reactCatsSyncEffectMonadThrow

  @inline final implicit def reactCatsSyncEffectMonoid[F[_]: Effect, A: Monoid]: Monoid[F[A]] =
    X.reactCatsSyncEffectMonoid
}
