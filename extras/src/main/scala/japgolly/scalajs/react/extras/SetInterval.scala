package japgolly.scalajs.react.extras

import scala.scalajs.js
import org.scalajs.dom.window

/**
 * NOTE: This may be renamed / relocated / removed in future.
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
