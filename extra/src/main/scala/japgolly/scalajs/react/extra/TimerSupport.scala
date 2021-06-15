package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.util.DefaultEffects
import japgolly.scalajs.react.util.Effect.Sync
import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js.timers.{RawTimers, SetTimeoutHandle}
import scala.scalajs.js.{UndefOr, undefined}

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
  final def setTimeout[F[_]](f: F[Unit], timeout: FiniteDuration)(implicit F: Sync[F]): F[Unit] =
    setTimeoutMs(f, timeout.toMillis.toDouble)

  /** Invokes the callback `f` once after a minimum of `timeout` elapses. */
  final def setTimeoutMs[F[_]](f: F[Unit], timeoutInMilliseconds: Double)(implicit F: Sync[F]): F[Unit] = {
    val first = F.delay {
      var handle: UndefOr[SetTimeoutHandle] = undefined
      val proc = F.chain(F.delay { handle = undefined }, f)
      handle = RawTimers.setTimeout(F.toJsFn(proc), timeoutInMilliseconds)
      F.delay(handle.foreach(RawTimers.clearTimeout))
    }
    F.flatMap(first)(x => F.transSync(onUnmount(x))(DefaultEffects.Sync))
  }

  /** Provides `setInterval`-like behavior insuring that the time between calls of `f` is *at least* the `timeout`. */
  final def setGuaranteedInterval[F[_]](f: F[Unit], interval: FiniteDuration)(implicit F: Sync[F]): F[Unit] =
    setGuaranteedIntervalMs(f, interval.toMillis.toDouble)

  /** Provides `setInterval`-like behavior insuring that the time between calls of `f` is *at least* the `timeout`. */
  final def setGuaranteedIntervalMs[F[_]](f: F[Unit], intervalInMilliseconds: Double)(implicit F: Sync[F]): F[Unit] = {
    val reschedule = F.suspend(setGuaranteedIntervalMs(f, intervalInMilliseconds))
    setTimeoutMs(F.finallyRun(f, reschedule), intervalInMilliseconds)
  }

  /** Invokes the callback `f` repeatedly every `period`. */
  final def setInterval[F[_]](f: F[Unit], period: FiniteDuration)(implicit F: Sync[F]): F[Unit] =
    setIntervalMs(f, period.toMillis.toDouble)

  final def setIntervalMs[F[_]](f: F[Unit], periodInMilliseconds: Double)(implicit F: Sync[F]): F[Unit] = {
    val first = F.delay {
      val i = RawTimers.setInterval(F.toJsFn(f), periodInMilliseconds)
      F.delay(RawTimers.clearInterval(i))
    }
    F.flatMap(first)(x => F.transSync(onUnmount(x))(DefaultEffects.Sync))
  }
}

object TimerSupport {
  @inline def install[P, C <: Children, S, B <: TimerSupport, U <: UpdateSnapshot]: ScalaComponent.Config[P, C, S, B, U, U] =
    OnUnmount.install
}
