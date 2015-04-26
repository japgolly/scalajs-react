package japgolly.scalajs.react.extra

import japgolly.scalajs.react.{TopNode, ReactComponentB}

/**
 * Accrues procedures to be run automatically when its component unmounts.
 *
 * Install in `ReactComponentB` via `.configure(OnUnmount.install)`.
 */
trait OnUnmount {
  private var unmountProcs: List[() => Unit] = Nil
  final def runUnmount(): Unit = {
    unmountProcs foreach (_())
    unmountProcs = Nil
  }
  final def onUnmount(f: => Unit): Unit = unmountProcs ::= (() => f)
  final def onUnmountF(f: () => Unit): Unit = unmountProcs ::= f
}

object OnUnmount {
  def install[P, S, B <: OnUnmount, N <: TopNode] =
    (_: ReactComponentB[P, S, B, N]).componentWillUnmount(_.backend.runUnmount())

  /**
   * Convenience class for the frequent case that a component needs a backend with `OnUnmount` and nothing else.
   */
  final class Backend extends OnUnmount
}