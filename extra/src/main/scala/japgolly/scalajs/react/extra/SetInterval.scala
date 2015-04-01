package japgolly.scalajs.react.extra

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js

/**
 * Alternative to `window.setInterval` that automatically unregisters installed callbacks when its component unmounts.
 *
 * Install in `ReactComponentB` via `.configure(SetInterval.install)`.
 */
trait SetInterval extends OnUnmount {
  final def setInterval(f: => Unit, timeout: FiniteDuration): Unit = {
    val i = js.timers.setInterval(timeout.toMillis)(f)
    onUnmount(js.timers.clearInterval(i))
  }
}

object SetInterval {
  def install[P, S, B <: SetInterval] = OnUnmount.install[P, S, B]
}
