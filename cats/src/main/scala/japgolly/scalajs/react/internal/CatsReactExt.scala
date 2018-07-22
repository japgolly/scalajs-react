package japgolly.scalajs.react.internal

import cats.data.Kleisli
import cats.{Eq, Monad, ~>}
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._

object CatsReactExt {

  final class CallbackKleisliOps[A, B](private val k: A => CallbackTo[B]) extends AnyVal {
    def toCatsKleisli: Kleisli[CallbackTo, A, B] =
      Kleisli(k)
  }

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
    import CatsReactExt._
    import CatsReactState.{ReactST, ChangeFilter}
    import CatsReact.CatsReactExt_StateAccessCB

    def listenWithStateMonad[P, C <: Children, S, B <: OnUnmount, U <: UpdateSnapshot, M[_], A]
        (listenable: P => Listenable[A],
         listener: A => ReactST[M, S, Unit])
        (implicit M: M ~> CallbackTo, N: Monad[M]) =
      Listenable.listen[P, C, S, B, U, A](listenable, $ => a => $.runState(listener(a)))

    def listenWithStateMonadF[P, C <: Children, S, B <: OnUnmount, U <: UpdateSnapshot, M[_], A]
        (listenable: P => Listenable[A],
         listener: A => ReactST[M, S, Unit])
        (implicit M: M ~> CallbackTo, N: Monad[M], F: ChangeFilter[S]) =
      Listenable.listen[P, C, S, B, U, A](listenable, $ => a => $.runStateF(listener(a)))
  }

}

trait CatsReactExt {
  import CatsReactExt._

  implicit final def CatsReactExt_Reusability(a: Reusability.type) = new ReusabilityOps(a)
  implicit final def CatsReactExt_Listenable(a: Listenable.type) = new ListenableOps(a)
  implicit final def CatsReactExt_CallbackKleisli[A, B](k: CallbackKleisli[A, B]) = new CallbackKleisliOps(k.run)
  implicit final def CatsReactExt_MA[M[_], A](a: M[A]) = new MA(a)
}