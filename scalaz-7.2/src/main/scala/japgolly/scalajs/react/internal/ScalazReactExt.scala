package japgolly.scalajs.react.internal

import scalaz.{Equal, Monad, ~>}
import scalaz.effect.IO
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import ScalazReactExt._

object ScalazReactExt {

  @inline final class CallbackToOps[A](private val _c: () => A) extends AnyVal {
    private def c = CallbackTo lift _c

    def toIO: IO[A] =
      ScalazReact.scalazIoToCallbackIso to c

    def flattenIO[B](implicit ev: A =:= IO[B]): CallbackTo[B] =
      //_c.flatMap(a => ev(a).toCallback)
      c.map(_.unsafePerformIO())
  }

  @inline final class MA[M[_], A](private val m: M[A]) extends AnyVal {
    @inline def toCallback(implicit t: M ~> CallbackTo): CallbackTo[A] =
      t(m)
  }

  @inline final class ReusabilityOps(private val ε: Reusability.type) extends AnyVal {

    /** Compare using Scalaz equality. */
    def byEqual[A](implicit e: Equal[A]): Reusability[A] =
      new Reusability(e.equal)

    /** Compare by reference and if different, compare using Scalaz equality. */
    def byRefOrEqual[A <: AnyRef : Equal]: Reusability[A] =
      Reusability.byRef[A] || byEqual[A]
  }

  @inline final class ListenableOps(private val ε: Listenable.type) extends AnyVal {
    import ScalazReact.ReactST

//    def installS[P, C <: Children, S, B <: OnUnmount, M[_], A](f: P => Listenable[A])(g: A => ReactST[M, S, Unit])(implicit M: M ~> CallbackTo, N: Monad[M]) =
//      Listenable.install[P, C, S, B, A](f, $ => a => $.runState(g(a)))
//
//    def installSF[P, C <: Children, S, B <: OnUnmount, M[_], A](f: P => Listenable[A])(g: A => ReactST[M, S, Unit])(implicit M: M ~> CallbackTo, N: Monad[M], F: ChangeFilter[S]) =
//      Listenable.install[P, C, S, B, A](f, $ => a => $.runStateF(g(a)))
  }

}

trait ScalazReactExt {
  @inline implicit final def ScalazReactExt_Reusability(a: Reusability.type) = new ReusabilityOps(a)
  @inline implicit final def ScalazReactExt_Listenable(a: Listenable.type) = new ListenableOps(a)
  @inline implicit final def ScalazReactExt_CallbackTo[A](a: CallbackTo[A]) = new CallbackToOps(a.toScalaFn)
  @inline implicit final def ScalazReactExt_MA[M[_], A](a: M[A]) = new MA(a)
}
