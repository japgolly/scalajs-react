package japgolly.scalajs.react.internal

import japgolly.scalajs.react.userdefined
import japgolly.scalajs.react.util.OptionLike
import scala.scalajs.js

object SafeEffect
    extends SafeEffectCallback
       with SafeEffectCatsEffect
       with userdefined.SafeEffects {

  trait Sync[F[_]] {

    val syncEmpty: F[Unit]

    def syncRun[A](f: => F[A]): A

    def syncOption_[O[_], A](f: => O[F[A]])(implicit O: OptionLike[O]): F[Unit]

    final def syncJsFn0[A](f: => F[A]): js.Function0[A] =
      () => syncRun(f)

    final def syncJsFn1[A, Z](f: A => F[Z]): js.Function1[A, Z] =
      a => syncRun(f(a))
  }

  implicit object JsFunction extends Sync[js.Function0] {
    override val syncEmpty: js.Function0[Unit] =
      () => ()

    override def syncRun[A](f: => js.Function0[A]): A =
      f()

    override def syncOption_[O[_], A](f: => O[js.Function0[A]])(implicit O: OptionLike[O]): js.Function0[Unit] =
      () => O.foreach(f)(_())
  }
}

trait SafeEffectCallback
trait SafeEffectCatsEffect
