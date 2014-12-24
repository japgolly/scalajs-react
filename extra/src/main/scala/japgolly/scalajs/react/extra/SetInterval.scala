package japgolly.scalajs.react.extra

import scala.scalajs.js
import org.scalajs.dom.window

/**
 * Alternative to `window.setInterval` that automatically unregisters installed callbacks when its component unmounts.
 *
 * Install in `ReactComponentB` via `.configure(SetInterval.install)`.
 */
trait SetInterval extends OnUnmount {
  final def setInterval(f: => Unit, timeout: js.Number): Unit = {
    val ff: js.Function = () => f
    val i = window.setInterval(ff, timeout)
    onUnmount(window.clearInterval(i))
  }
}

object SetInterval {
  def install[P, S, B <: SetInterval] = OnUnmount.install[P, S, B]
}
