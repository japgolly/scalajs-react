package japgolly.scalajs.react.internal

import japgolly.scalajs.react.internal.CoreGeneral._
import japgolly.scalajs.react.util.Effect.Sync

object EffectUtil {

  def unsafeAsEventDefault_[A](e: ReactEvent)(a: => A): Unit =
    if (!e.defaultPrevented) {
      a
      e.preventDefault()
    }

  def asEventDefault_[F[_], A](e: ReactEvent)(fa: => F[A])(implicit F: Sync[F]): F[Unit] =
    F.delay(unsafeAsEventDefault_(e)(F.runSync(fa)))

  def unsafeKeyCodeSwitch[A](e       : ReactKeyboardEvent,
                             altKey  : Boolean = false,
                             ctrlKey : Boolean = false,
                             metaKey : Boolean = false,
                             shiftKey: Boolean = false)
                            (switch  : PartialFunction[Int, A]): Option[A] =
    unsafeKeyEventSwitch(e, e.keyCode, altKey, ctrlKey, metaKey, shiftKey)(switch)

  def unsafeKeyEventSwitch[A, B](e       : ReactKeyboardEvent,
                                 a       : A,
                                 altKey  : Boolean = false,
                                 ctrlKey : Boolean = false,
                                 metaKey : Boolean = false,
                                 shiftKey: Boolean = false)
                                (switch  : PartialFunction[A, B]): Option[B] =
    if (!e.pressedModifierKeys(altKey = altKey, ctrlKey = ctrlKey, metaKey = metaKey, shiftKey = shiftKey))
      None
    else
      switch.lift(a)
}
