package japgolly.scalajs.react.extra

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js.timers.RawTimers
import japgolly.scalajs.react.{CallbackTo, Callback, TopNode}

/**
 * Alternative to `window.setInterval` that automatically unregisters installed callbacks when its component unmounts.
 *
 * Install in `ReactComponentB` via `.configure(SetInterval.install)`.
 */
trait SetInterval extends OnUnmount {
  final def setInterval(f: Callback, timeout: FiniteDuration): Callback =
    CallbackTo {
      val i = RawTimers.setInterval(f.toJsFunction, timeout.toMillis.toDouble)
      Callback(RawTimers clearInterval i)
    } flatMap onUnmount
}

object SetInterval {
  def install[P, S, B <: SetInterval, N <: TopNode] =
    OnUnmount.install[P, S, B, N]
}
