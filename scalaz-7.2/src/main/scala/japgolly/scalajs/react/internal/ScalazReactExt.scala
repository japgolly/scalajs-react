package japgolly.scalajs.react.internal

import japgolly.scalajs.react.ScalazReact.{ChangeFilter, ScalazReactExt_StateAccessCB}
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.internal.ScalazReactExt._
import scalaz.effect.IO
import scalaz.{Equal, Kleisli, Monad, ~>}

object ScalazReactExt {

  final class CallbackToOps[A](private val t: Trampoline[A]) extends AnyVal {
    private def self = new CallbackTo(t)

    def toIO: IO[A] =
      IO(self.runNow())

    def flattenIO[B](implicit ev: A =:= IO[B]): CallbackTo[B] =
      self.map(_.unsafePerformIO())
  }

  final class CallbackKleisliOps[A, B](private val k: A => CallbackTo[B]) extends AnyVal {
    def toScalazKleisli: Kleisli[CallbackTo, A, B] =
      Kleisli(k)
  }

  final class MA[M[_], A](private val m: M[A]) extends AnyVal {
    def toCallback(implicit t: M ~> CallbackTo): CallbackTo[A] =
      t(m)
  }

  final class ReusabilityOps(private val ε: Reusability.type) extends AnyVal {

    /** Compare using Scalaz equality. */
    def byEqual[A](implicit e: Equal[A]): Reusability[A] =
      new Reusability(e.equal)

    /** Compare by reference and if different, compare using Scalaz equality. */
    def byRefOrEqual[A <: AnyRef : Equal]: Reusability[A] =
      Reusability.byRef[A] || byEqual[A]
  }

  final class ListenableOps(private val ε: Listenable.type) extends AnyVal {
    import ScalazReact.ReactST

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

trait ScalazReactExt {

  implicit final def ScalazReactExt_Reusability(a: Reusability.type): ReusabilityOps =
    new ReusabilityOps(a)

  implicit final def ScalazReactExt_Listenable(a: Listenable.type): ListenableOps =
    new ListenableOps(a)

  implicit final def ScalazReactExt_CallbackTo[A](a: CallbackTo[A]): CallbackToOps[A] =
    new CallbackToOps(a.underlyingRepr)

  implicit final def ScalazReactExt_CallbackKleisli[A, B](k: CallbackKleisli[A, B]): CallbackKleisliOps[A, B] =
    new CallbackKleisliOps(k.run)

  implicit final def ScalazReactExt_MA[M[_], A](a: M[A]): MA[M, A] =
    new MA(a)
}
