package japgolly.scalajs.react.extra

import japgolly.scalajs.react.{TopNode, ReactComponentB, Callback}

/**
 * Accrues procedures to be run automatically when its component unmounts.
 *
 * Install in `ReactComponentB` via `.configure(OnUnmount.install)`.
 */
trait OnUnmount {
  private var unmountProcs: List[Callback] = Nil
  final def unmount: Callback = Callback {
    unmountProcs foreach (_.runNow())
    unmountProcs = Nil
  }
  final def onUnmount(f: Callback): Callback =
    Callback(unmountProcs ::= f)
}

object OnUnmount {
  def install[P, S, B <: OnUnmount, N <: TopNode] =
    (_: ReactComponentB[P, S, B, N]).componentWillUnmount(_.backend.unmount)

  /**
   * Convenience class for the frequent case that a component needs a backend with `OnUnmount` and nothing else.
   */
  final class Backend extends OnUnmount
}