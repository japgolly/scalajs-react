package japgolly.scalajs.react

import cats.arrow.Profunctor
import cats.data.Ior
import cats.kernel.Eq
import japgolly.scalajs.react.internal.CoreGeneral.Key
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
}
