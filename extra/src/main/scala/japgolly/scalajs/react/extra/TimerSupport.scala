package japgolly.scalajs.react.extra

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js.{UndefOr, undefined}
import scala.scalajs.js.timers.{RawTimers, SetTimeoutHandle}
import japgolly.scalajs.react._

/**
 * Alternatives to `window.setTimeout`/`window.setInterval` that automatically unregister installed callbacks
 * when the component unmounts.
 *
 * Provides interval methods that guarentee duration between callbacks.  Regular use of `setInterval` is fine
 * for callbacks with determined execution time.  However, if your callback could possibly take as long or longer
 * than your `timeout`, you can end up with callbacks firing back to back.
 *
 * Install in `ScalaComponent.build` via `.configure(TimerSupport.install)`.
 */
trait TimerSupport extends OnUnmount {

  /** Invokes the callback `f` once after a minimum of `timeout` elapses. */
  final def setTimeout(f: Callback, timeout: FiniteDuration): Callback =
    setTimeoutMs(f, timeout.toMillis.toDouble)

  /** Invokes the callback `f` once after a minimum of `timeout` elapses. */
  final def setTimeoutMs(f: Callback, timeoutInMilliseconds: Double): Callback = {
    CallbackTo {
      var handle: UndefOr[SetTimeoutHandle] = undefined
      val proc = f << Callback { handle = undefined }
      handle = RawTimers.setTimeout(proc.toJsFn, timeoutInMilliseconds)
      Callback(handle foreach RawTimers.clearTimeout)
    } flatMap onUnmount
  }

  /** Provides `setInterval`-like behavior insuring that the time between calls of `f` is *at least* the `timeout`. */
  final def setGuaranteedInterval(f: Callback, interval: FiniteDuration): Callback =
    setGuaranteedIntervalMs(f, interval.toMillis.toDouble)

  /** Provides `setInterval`-like behavior insuring that the time between calls of `f` is *at least* the `timeout`. */
  final def setGuaranteedIntervalMs(f: Callback, intervalInMilliseconds: Double): Callback= {
    val reschedule = Callback byName setGuaranteedIntervalMs(f, intervalInMilliseconds)
    setTimeoutMs(f finallyRun reschedule, intervalInMilliseconds)
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
  @inline def install[P, C <: Children, S, B <: TimerSupport, U <: UpdateSnapshot]: ScalaComponent.Config[P, C, S, B, U, U] =
    OnUnmount.install
}
