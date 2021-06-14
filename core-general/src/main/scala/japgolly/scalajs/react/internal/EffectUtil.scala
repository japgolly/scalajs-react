package japgolly.scalajs.react.internal

import japgolly.scalajs.react.util.Effect.Sync
import japgolly.scalajs.react.ReactEvent

object EffectUtil {

  def asEventDefault[F[_], A](fa: F[A], e: ReactEvent)(implicit F: Sync[F]): F[Option[A]] =
    F.suspend {
      if (e.defaultPrevented)
        F.pure(None)
      else
        F.map(fa) { a =>
          e.preventDefault()
          Some(a)
        }
    }

}
