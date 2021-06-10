package japgolly.scalajs.react.util

import japgolly.scalajs.react.userdefined
import japgolly.scalajs.react.util.OptionLike
import scala.scalajs.js

object SafeEffect
    extends SafeEffectCallback
       with SafeEffectCatsEffect
       with userdefined.SafeEffects {

  trait Sync[F[_]] extends UnsafeEffect.Sync[F]

  object Sync {
    type RawCallback = js.Function0[Any]

    val empty: js.Function0[Unit] =
      () => ()

    implicit val jsFunction: Sync[js.Function0] = new Sync[js.Function0] {
      type F[A] = js.Function0[A]
      override def delay  [A]      (a: => A)                                    = () => a
      override def pure   [A]      (a: A)                                       = () => a
      override def map    [A, B]   (fa: F[A])(f: A => B)                        = () => f(fa())
      override def flatMap[A, B]   (fa: F[A])(f: A => F[B])                     = () => f(fa())()
      override def runSync[A]      (fa: => F[A])                                = fa()
      override def option_[O[_], A](ofa: => O[F[A]])(implicit O: OptionLike[O]) = () => O.foreach(ofa)(_())
    }
  }

  trait Async[F[_]] {
    def async_(onCompletion: Sync.RawCallback => Sync.RawCallback): F[Unit]
  }
}

trait SafeEffectCallback
trait SafeEffectCatsEffect
