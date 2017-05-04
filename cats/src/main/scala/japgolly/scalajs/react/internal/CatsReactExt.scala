package japgolly.scalajs.react.internal

import cats.{Eq, Monad, ~>}
import cats.effect.IO

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._

/**
  * Created by alonsodomin on 13/03/2017.
  */
object CatsReactExt {

  final class CallbackToOps[A](private val body: () => A) extends AnyVal {
    private def callback = CallbackTo.lift(body)

    def toIO: IO[A] = IO(body())

    def flattenIO[B](implicit ev: A =:= IO[B]): CallbackTo[B] =
      callback.map(_.unsafeRunSync())
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

    def listenWithStateMonad[P, C <: Children, S, B <: OnUnmount, M[_], A](listenable: P => Listenable[A],
                                                                           listener: A => ReactST[M, S, Unit])(implicit M: M ~> CallbackTo, N: Monad[M]) =
      Listenable.listen[P, C, S, B, A](listenable, $ => a => $.runState(listener(a)))

    def listenWithStateMonadF[P, C <: Children, S, B <: OnUnmount, M[_], A](listenable: P => Listenable[A],
                                                                            listener: A => ReactST[M, S, Unit])(implicit M: M ~> CallbackTo, N: Monad[M], F: ChangeFilter[S]) =
      Listenable.listen[P, C, S, B, A](listenable, $ => a => $.runStateF(listener(a)))
  }

}

trait CatsReactExt {
  import CatsReactExt._

  implicit final def CatsReactExt_Reusability(a: Reusability.type) = new ReusabilityOps(a)
  implicit final def CatsReactExt_Listenable(a: Listenable.type) = new ListenableOps(a)
  implicit final def CatsReactExt_CallbackTo[A](a: CallbackTo[A]) = new CallbackToOps(a.toScalaFn)
  implicit final def CatsReactExt_MA[M[_], A](a: M[A]) = new MA(a)
}
