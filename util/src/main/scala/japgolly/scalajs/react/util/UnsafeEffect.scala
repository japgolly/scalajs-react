package japgolly.scalajs.react.util

import japgolly.scalajs.react.userdefined
import japgolly.scalajs.react.util.OptionLike
import scala.scalajs.js

object UnsafeEffect extends userdefined.UnsafeEffects {
  type Id[A] = A

  trait Monadic[F[_]] {
    def delay  [A]   (a: => A)               : F[A]
    def pure   [A]   (a: A)                  : F[A]
    def map    [A, B](fa: F[A])(f: A => B)   : F[B]
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  }

  trait Sync[F[_]] extends Monadic[F] {
    def runSync[A]      (fa: => F[A])                               : A
    def option_[O[_], A](ofa: => O[F[A]])(implicit O: OptionLike[O]): F[Unit]

    final def toJsFn0[A](f: => F[A]): js.Function0[A] =
      () => runSync(f)

    final def toJsFn1[A, Z](f: A => F[Z]): js.Function1[A, Z] =
      a => runSync(f(a))
  }

  object Sync extends EffectTrans[Sync] {
    override protected def trans[F[_], G[_], A](from: Sync[F], to: Sync[G], f: => F[A]): G[A] = {
      val fn = from.toJsFn0(f)
      to.delay(fn())
    }

    implicit lazy val id: Sync[Id] = new Sync[Id] {
      override def delay  [A]      (a: => A)                                = a
      override def pure   [A]      (a: A)                                   = a
      override def map    [A, B]   (a: A)(f: A => B)                        = f(a)
      override def flatMap[A, B]   (a: A)(f: A => B)                        = f(a)
      override def runSync[A]      (a: => A)                                = a
      override def option_[O[_], A](oa: => O[A])(implicit O: OptionLike[O]) = O.foreach(oa)(_ => ())
    }

    implicit lazy val idEndo: Trans.Id[Id] =
      Trans.id[Id]
  }
}
