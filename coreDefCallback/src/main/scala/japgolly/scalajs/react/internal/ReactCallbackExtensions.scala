package japgolly.scalajs.react.internal

import japgolly.scalajs.react.callback._
import japgolly.scalajs.react.{Reusability, Reusable}
import scala.annotation.nowarn

@nowarn("cat=unused")
trait ReactCallbackExtensions {
  import ReactCallbackExtensions._

  @inline final implicit def ReactCallbackExtensionReusable(self: Reusable.type): ReusableExt.type =
    ReusableExt

  @inline final implicit def ReactCallbackExtensionReusability(self: Reusability.type): ReusabilityExt.type =
    ReusabilityExt
}

object ReactCallbackExtensions {

  object ReusableExt {
    import Reusable._

    def callbackByRef[A](c: CallbackTo[A]): Reusable[CallbackTo[A]] =
      byRefIso(c)(_.underlyingRepr)

    def callbackOptionByRef[A](c: CallbackOption[A]): Reusable[CallbackOption[A]] =
      byRefIso(c)(_.underlyingRepr)

    def asyncCallbackByRef[A](c: AsyncCallback[A]): Reusable[AsyncCallback[A]] =
      byRefIso(c)(_.underlyingRepr)

    lazy val emptyCallback: Reusable[Callback] =
      callbackByRef(Callback.empty)
  }

  object ReusabilityExt {
    import Reusability._

    def callbackByRef[A]: Reusability[CallbackTo[A]] =
      by((_: CallbackTo[A]).underlyingRepr)(byRef)

    def callbackOptionByRef[A]: Reusability[CallbackOption[A]] =
      by((_: CallbackOption[A]).underlyingRepr)(byRef)

    def asyncCallbackByRef[A]: Reusability[AsyncCallback[A]] =
      by((_: AsyncCallback[A]).underlyingRepr)(byRef)

    implicit lazy val callbackSetIntervalResult: Reusability[Callback.SetIntervalResult] =
      byRef || by(_.handle)

    implicit lazy val callbackSetTimeoutResult: Reusability[Callback.SetTimeoutResult] =
      byRef || by(_.handle)
  }

}
