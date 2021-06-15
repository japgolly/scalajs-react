package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.util.DefaultEffects._
import japgolly.scalajs.react.util.Effect.Dispatch

/**
 * Accrues procedures to be run automatically when its component unmounts.
 *
 * Install in `ScalaComponent.build` via `.configure(OnUnmount.install)`.
 */
trait OnUnmount {
  private var unmountProcs: List[Sync[Unit]] = Nil

  final def unmount: Sync[Unit] =
    Sync.chain(Sync.sequence_(unmountProcs), Sync.delay{ unmountProcs = Nil })

  final def onUnmount[F[_]](f: F[Unit])(implicit F: Dispatch[F]): Sync[Unit] =
    Sync.delay(unmountProcs ::= Sync.fromJsFn0(F.dispatchFn(f)))
}

object OnUnmount {
  def install[P, C <: Children, S, B <: OnUnmount, U <: UpdateSnapshot]: ScalaComponent.Config[P, C, S, B, U, U] =
    _.componentWillUnmount(_.backend.unmount)

  /**
   * Convenience class for the frequent case that a component needs a backend with `OnUnmount` and nothing else.
   */
  final class Backend extends OnUnmount
}