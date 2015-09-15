package japgolly.scalajs.react.extra

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js.timers.RawTimers
import japgolly.scalajs.react.{CallbackTo, Callback, TopNode}

/**
 * Alternatives to `window.setTimeout`/`window.setInterval` that automatically unregisters installed callbacks 
 * when its component unmounts.
 * 
 * Provides interval methods that guarentee duration between callbacks.  Regular use of `setInterval` is fine
 * for callbacks with determined execution time.  However, if your callback could possibly take as long or longer
 * than your `timeout`, you can end up with callbacks firing back to back. 
 *
 * Install in `ReactComponentB` via `.configure(TimerSupport.install)`.
 */
trait TimerSupport extends OnUnmount {

  /** Invokes the callback `f` once after a minimum of `timeout` elapses. */
  final def setTimeout(f: Callback, timeout: FiniteDuration): Callback = 
    setTimeoutMs(f, timeout.toMillis.toDouble)
  
  /** Invokes the callback `f` once after a minimum of `timeout` elapses. */
  final def setTimeoutMs(f: Callback, timeoutInMilliseconds: Double): Callback = {
    CallbackTo {
      val i = RawTimers.setTimeout(f.toJsFn, timeoutInMilliseconds)
      Callback(RawTimers clearTimeout i)
    } flatMap onUnmount
  }

  /** Provides `setInterval`-like behavior insuring that the time between calls of `f` is *at least* the `timeout`. */
  final def setGuaranteedInterval(f: Callback, interval: FiniteDuration): Callback = 
    setGuaranteedIntervalMs(f, interval.toMillis.toDouble)
  
  /** Provides `setInterval`-like behavior insuring that the time between calls of `f` is *at least* the `timeout`. */
  final def setGuaranteedIntervalMs(f: Callback, intervalInMilliseconds: Double): Callback= {
    CallbackTo {
      val i = RawTimers.setTimeout(() => {
        f.runNow()
        setGuaranteedIntervalMs(f, intervalInMilliseconds)
        ()
      }, intervalInMilliseconds)
      Callback(RawTimers clearTimeout i)
    } flatMap onUnmount
  }
  
  /** Invokes the callback `f` repeatedly every `period`. */
  final def setInterval(f: Callback, period: FiniteDuration): Callback =
    setIntervalMs(f, period.toMillis.toDouble)

  final def setIntervalMs(f: Callback, periodInMilliseconds: Double): Callback =
    CallbackTo {
      val i = RawTimers.setInterval(f.toJsFn, periodInMilliseconds)
      Callback(RawTimers clearInterval i)
    } flatMap onUnmount
}

object TimerSupport {
  def install[P, S, B <: TimerSupport, N <: TopNode] = OnUnmount.install[P, S, B, N]
}
