package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.util.DefaultEffects
import japgolly.scalajs.react.util.Effect.{Dispatch, Sync}

/** Accrues procedures to be run automatically when its component unmounts.
  *
  * Install in `ScalaComponent.build` via `.configure(OnUnmount.install)`.
  */
trait OnUnmountF[F[_]] { self =>
  protected def onUnmountEffect: Sync[F]
  import self.{onUnmountEffect => F}

  private var unmountProcs: List[F[Unit]] = Nil

  final def unmount: F[Unit] =
    F.chain(F.sequence_(unmountProcs), F.delay{ unmountProcs = Nil })

  final def onUnmount[G[_]](f: => G[Unit])(implicit G: Dispatch[G]): F[Unit] =
    F.delay(unmountProcs ::= F.transDispatch(f))
}

object OnUnmountF {
  def install[F[_]: Dispatch, P, C <: Children, S, B <: OnUnmountF[F], U <: UpdateSnapshot]: ScalaComponent.Config[P, C, S, B, U, U] =
    _.componentWillUnmount(_.backend.unmount)

  def apply[F[_]]()(implicit F: Sync[F]): OnUnmountF[F] =
    new OnUnmountF[F] {
      override protected def onUnmountEffect = F
    }
}

// =====================================================================================================================

/** Accrues procedures to be run automatically when its component unmounts.
  *
  * Install in `ScalaComponent.build` via `.configure(OnUnmount.install)`.
  */
trait OnUnmount extends OnUnmountF[DefaultEffects.Sync] {
  override protected def onUnmountEffect: Sync[DefaultEffects.Sync] =
    DefaultEffects.Sync
}

object OnUnmount {
  def install[P, C <: Children, S, B <: OnUnmount, U <: UpdateSnapshot]: ScalaComponent.Config[P, C, S, B, U, U] =
    _.componentWillUnmount(_.backend.unmount)(DefaultEffects.Sync)

  /** Convenience class for the frequent case that a component needs a backend with `OnUnmount` and nothing else. */
  @deprecated("Change `new OnUnmount.Backend` to `OnUnmount()`", "2.0.0")
  final class Backend extends OnUnmount

  def apply(): OnUnmount =
    new OnUnmount {}
}
