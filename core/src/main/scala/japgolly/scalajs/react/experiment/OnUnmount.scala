package japgolly.scalajs.react.experiment

import japgolly.scalajs.react.ReactComponentB

/**
 * NOTE: This may be renamed / relocated / removed in future.
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
  def install[P, S, B <: OnUnmount] =
    (_: ReactComponentB[P, S, B]).componentWillUnmount(_.backend.runUnmount())
}
