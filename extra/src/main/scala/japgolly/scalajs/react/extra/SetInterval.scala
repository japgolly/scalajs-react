package japgolly.scalajs.react.extra

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js
import scalaz.effect.IO
import japgolly.scalajs.react.TopNode

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

  final def setIntervalIO(f: IO[Unit], timeout: FiniteDuration): IO[Unit] =
    IO(setInterval(f.unsafePerformIO(), timeout))
}

object SetInterval {
  def install[P, S, B <: SetInterval, N <: TopNode] = OnUnmount.install[P, S, B, N]
}
