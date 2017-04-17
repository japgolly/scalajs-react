package japgolly.scalajs.react.extra

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js
import scalaz.effect.IO
import japgolly.scalajs.react.TopNode

/**
 * Alternative to `window.setTimeout` that automatically unregisters installed callbacks when its component unmounts.
 * 
 * Provides interval methods that guarentee duration between callbacks.  Regular use of `SetInterval` is fine
 * for callbacks with determined execution time.  However, if your callback could possibly take as long or longer
 * than your `timeout`, you can end up with callbacks firing back to back. 
 *
 * Install in `ReactComponentB` via `.configure(SetTimeout.install)`.
 */
trait SetTimeout extends OnUnmount {
  final def setTimeout(f: => Unit, timeout: FiniteDuration): Unit = {
    val i = js.timers.setTimeout(timeout.toMillis)(f)
    onUnmount(js.timers.clearTimeout(i))
  }
  
  /** Provides `setInterval`-like behavior insuring that the time between calls of `f` is *at least* the `timeout`. */
  final def setGuaranteedInterval(f: => Unit, interval: FiniteDuration): Unit = {
    val i = js.timers.setTimeout(interval.toMillis.toDouble)({
      f
      setGuaranteedInterval(f, interval)
    })
    onUnmount(js.timers.clearTimeout(i))
  }

  final def setTimeoutIO(f: IO[Unit], timeout: FiniteDuration): IO[Unit] =
    IO(setTimeout(f.unsafePerformIO(), timeout))

  /** Provides `setInterval`-like behavior insuring that the time between calls of `f` is *at least* the `timeout`. */
  final def setGuaranteedIntervalIO(f: IO[Unit], interval: FiniteDuration): IO[Unit] =
    IO(setGuaranteedInterval(f.unsafePerformIO(), interval))
}

object SetTimeout {
  def install[P, S, B <: SetTimeout, N <: TopNode] = OnUnmount.install[P, S, B, N]
}
