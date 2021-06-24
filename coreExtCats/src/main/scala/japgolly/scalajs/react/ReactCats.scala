package japgolly.scalajs.react

import cats.arrow.Profunctor
import cats.data.Ior
import cats.kernel.Eq
import japgolly.scalajs.react.internal.CoreGeneral.Key

object ReactCats extends ReactCats

trait ReactCats {
  import internal.ReactCats._

  @inline final implicit def reactCatsEqKey: Eq[Key] =
    Eq.fromUniversalEquals

  @inline final implicit def ReactCatsExtReusabilityObj(e: Reusability.type): ReactCatsExtReusabilityObj =
    internal.ReactCats.ReactCatsExtReusabilityObj(e)

  @inline final implicit def reactCatsReusabilityIor[A: Reusability, B: Reusability]: Reusability[A Ior B] =
    internal.ReactCats.reactCatsReusabilityIor

  @inline final implicit def reactCatsProfunctorRefFull[F[_], X]: Profunctor[Ref.FullF[F, *, X, *]] =
    internal.ReactCats.reactCatsProfunctorRefFull
}
