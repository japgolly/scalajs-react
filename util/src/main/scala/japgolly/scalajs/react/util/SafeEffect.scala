package japgolly.scalajs.react.util

import japgolly.scalajs.react.userdefined
import japgolly.scalajs.react.util.OptionLike
import scala.scalajs.js
import scala.util.Try

object SafeEffect
    extends SafeEffectCallback
       with SafeEffectCatsEffect
       with userdefined.SafeEffects {

  trait Sync[F[_]] extends UnsafeEffect.Sync[F]

  object Sync {
    type Untyped[A] = js.Function0[A]

    val empty: Untyped[Unit] =
      () => ()

    implicit val untyped: Sync[Untyped] = new Sync[Untyped] {
      type F[A] = Untyped[A]
      override def delay  [A]      (a: => A)                                    = () => a
      override def pure   [A]      (a: A)                                       = () => a
      override def map    [A, B]   (fa: F[A])(f: A => B)                        = () => f(fa())
      override def flatMap[A, B]   (fa: F[A])(f: A => F[B])                     = () => f(fa())()
      override def runSync[A]      (fa: => F[A])                                = fa()
      override def option_[O[_], A](ofa: => O[F[A]])(implicit O: OptionLike[O]) = () => O.foreach(ofa)(_())
    }
  }

  trait Async[F[_]] {
    def async[A](f: Async.Untyped[A]): F[A]

    // TODO: FX: Confirm this works. If it does then why does AsyncCallback.viaCallback use a promise?
    final def async_(onCompletion: Sync.Untyped[Unit] => Sync.Untyped[Unit]): F[Unit] =
      async[Unit](f => onCompletion(f(Try(()))))

    def runAsync[A](fa: => F[A]): Async.Untyped[A]
  }

  object Async extends EffectTrans[Async] {
    type Untyped[A] = (Try[A] => Sync.Untyped[Unit]) => Sync.Untyped[Unit]

    override protected def trans[F[_], G[_], A](from: Async[F], to: Async[G], fa: => F[A]): G[A] =
      to.async[A](from.runAsync(fa)(_))

    implicit lazy val untyped: Async[Untyped] =
      new Async[Untyped] {
        override def async   [A](f: Untyped[A]) = f
        override def runAsync[A](f: => Untyped[A]) = f
      }
  }
}

trait SafeEffectCallback
trait SafeEffectCatsEffect
