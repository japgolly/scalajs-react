package japgolly.scalajs.react.internal

import japgolly.scalajs.react.internal.CoreGeneral._
import japgolly.scalajs.react.util.Effect.Sync

object EffectUtil {

  def asEventDefault_[F[_], A](e: ReactEvent)(fa: => F[A])(implicit F: Sync[F]): F[Unit] =
    F.suspend {
      if (e.defaultPrevented)
        F.empty
      else
        F.map(fa)(_ => e.preventDefault())
    }

  // def asEventDefault[F[_], A](e: ReactEvent)(fa: => F[A])(implicit F: Sync[F]): F[Option[A]] =
  //   F.suspend {
  //     if (e.defaultPrevented)
  //       F.pure(None)
  //     else
  //       F.map(fa) { a =>
  //         e.preventDefault()
  //         Some(a)
  //       }
  //   }

  def keyCodeSwitch[F[_], A](e       : ReactKeyboardEvent,
                             altKey  : Boolean = false,
                             ctrlKey : Boolean = false,
                             metaKey : Boolean = false,
                             shiftKey: Boolean = false)
                            (switch  : PartialFunction[Int, F[A]])
                            (implicit F: Sync[F]): F[Option[A]] =
    keyEventSwitch(e, e.keyCode, altKey, ctrlKey, metaKey, shiftKey)(switch)

  def keyEventSwitch[F[_], A, B](e       : ReactKeyboardEvent,
                                 a       : A,
                                 altKey  : Boolean = false,
                                 ctrlKey : Boolean = false,
                                 metaKey : Boolean = false,
                                 shiftKey: Boolean = false)
                                (switch  : PartialFunction[A, F[B]])
                                (implicit F: Sync[F]): F[Option[B]] =
    F.delay {
      if (!e.pressedModifierKeys(altKey, ctrlKey, metaKey, shiftKey))
        None
      else
        switch.lift(a).map(F.runSync(_))
    }
}
