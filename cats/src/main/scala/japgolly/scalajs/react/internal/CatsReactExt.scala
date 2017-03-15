package japgolly.scalajs.react.internal

import cats.{Eq, Monad, ~>}

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._

/**
  * Created by alonsodomin on 13/03/2017.
  */
object CatsReactExt {

  final class MA[M[_], A](private val ma: M[A]) extends AnyVal {
    def toCallback(implicit t: M ~> CallbackTo): CallbackTo[A] = t(ma)
  }

  final class ReusabilityOps(private val ε: Reusability.type) extends AnyVal {
    /** Compare using cat's Eq */
    def byEq[A](implicit eq: Eq[A]): Reusability[A] =
      new Reusability[A](eq.eqv)

    def byRefOrEq[A <: AnyRef : Eq]: Reusability[A] =
      Reusability.byRef[A] || byEq[A]
  }

  final class ListenableOps(private val ε: Listenable.type) extends AnyVal {

  }

}

trait CatsReactExt {
  import CatsReactExt._

  implicit final def CatsReactExt_Reusability(a: Reusability.type) = new ReusabilityOps(a)
  implicit final def CatsReactExt_MA[M[_], A](a: M[A]) = new MA(a)
}