package japgolly.scalajs.react.internal

import japgolly.scalajs.react.callback._
import japgolly.scalajs.react.{ReactEventTypes, Reusability, Reusable}
import scala.annotation.nowarn

@nowarn("cat=unused")
trait ReactCallbackExtensions {
  import ReactCallbackExtensions._

  @inline final implicit def ReactCallbackExtensionCallbackOptionObj(self: CallbackOption.type): CallbackOptionObjExt.type =
    CallbackOptionObjExt

  @inline final implicit def ReactCallbackExtensionCallbackOption[A](self: CallbackOption[A]): CallbackOptionExt[A] =
    new CallbackOptionExt(self.underlyingRepr)

  @inline final implicit def ReactCallbackExtensionCallbackTo[A](self: CallbackTo[A]): CallbackToExt[A] =
    new CallbackToExt(self.underlyingRepr)

  @inline final implicit def ReactCallbackExtensionReusable(self: Reusable.type): ReusableExt.type =
    ReusableExt

  @inline final implicit def ReactCallbackExtensionReusability(self: Reusability.type): ReusabilityExt.type =
    ReusabilityExt
}

object ReactCallbackExtensions {

  object Events extends ReactEventTypes
  import Events._

  object CallbackOptionObjExt {
    import CallbackOption._

    @inline def keyCodeSwitch[A](e       : ReactKeyboardEvent,
                                 altKey  : Boolean = false,
                                 ctrlKey : Boolean = false,
                                 metaKey : Boolean = false,
                                 shiftKey: Boolean = false)
                                (switch  : PartialFunction[Int, CallbackTo[A]]): CallbackOption[A] =
      keyEventSwitch(e, e.keyCode, altKey, ctrlKey, metaKey, shiftKey)(switch)

    def keyEventSwitch[A, B](e       : ReactKeyboardEvent,
                             a       : A,
                             altKey  : Boolean = false,
                             ctrlKey : Boolean = false,
                             metaKey : Boolean = false,
                             shiftKey: Boolean = false)
                            (switch  : PartialFunction[A, CallbackTo[B]]): CallbackOption[B] =
      option {
        EffectUtil.unsafeKeyEventSwitch(e, a, altKey = altKey, ctrlKey = ctrlKey, metaKey = metaKey, shiftKey = shiftKey)(switch)
          .map(_.runNow())
      }
  }

  final class CallbackOptionExt[A](private val _self: CallbackOption.UnderlyingRepr[A]) extends AnyVal {
    @inline private def self: CallbackOption[A] =
      new CallbackOption(_self)

    /** Wraps this so that:
      *
      * 1) It only executes if `e.defaultPrevented` is `false`.
      * 2) It sets `e.preventDefault` on successful completion.
      */
    def asEventDefault(e: ReactEvent): CallbackOption[A] =
      (self <* e.preventDefaultCB.toCBO).unless(e.defaultPrevented)
  }

  final class CallbackToExt[A](private val _self: Trampoline[A]) extends AnyVal {
    @inline private def self: CallbackTo[A] =
      new CallbackTo(_self)

    /** Wraps this so that:
      *
      * 1) It only executes if `e.defaultPrevented` is `false`.
      * 2) It sets `e.preventDefault` on successful completion.
      */
    def asEventDefault(e: ReactEvent): CallbackTo[Option[A]] =
      (self <* e.preventDefaultCB).unless(e.defaultPrevented)
  }

  object ReusableExt {
    import Reusable._

    def callbackByRef[A](c: CallbackTo[A]): Reusable[CallbackTo[A]] =
      byRefIso(c)(_.underlyingRepr)

    def callbackOptionByRef[A](c: CallbackOption[A]): Reusable[CallbackOption[A]] =
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

    implicit lazy val callbackSetIntervalResult: Reusability[Callback.SetIntervalResult] =
      byRef || by(_.handle)

    implicit lazy val callbackSetTimeoutResult: Reusability[Callback.SetTimeoutResult] =
      byRef || by(_.handle)
  }

}
