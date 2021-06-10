package japgolly.scalajs.react.util

import japgolly.scalajs.react.callback._

trait SafeEffectCallback {

  implicit object callback extends SafeEffect.Sync[CallbackTo] {

    @inline override def syncRun[A](f: => CallbackTo[A]): A =
      f.runNow()

    override def syncOption_[O[_], A](f: => O[CallbackTo[A]])(implicit O: OptionLike[O]): Callback =
      Callback(O.foreach(f)(_.runNow()))
  }

}
