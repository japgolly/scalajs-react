package japgolly.scalajs.react.extra

import japgolly.scalajs.react._

/**
 * Accrues procedures to be run automatically when its component unmounts.
 *
 * Install in `ScalaComponent.build` via `.configure(OnUnmount.install)`.
 */
trait OnUnmount {
  private var unmountProcs: List[Callback] = Nil

  final def unmount: Callback =
    Callback.sequence(unmountProcs) >> Callback({unmountProcs = Nil})

  final def onUnmount(f: Callback): Callback =
    Callback(unmountProcs ::= f)
}

object OnUnmount {
  def install[P, C <: Children, S, B <: OnUnmount, U <: UpdateSnapshot]: ScalaComponent.Config[P, C, S, B, U, U] =
    _.componentWillUnmount(_.backend.unmount)

  /**
   * Convenience class for the frequent case that a component needs a backend with `OnUnmount` and nothing else.
   */
  final class Backend extends OnUnmount
}