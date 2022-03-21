package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.util.DefaultEffects
import japgolly.scalajs.react.util.Effect.Dispatch
import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js.timers.{RawTimers, SetTimeoutHandle}
import scala.scalajs.js.{UndefOr, undefined}

/** Alternatives to `window.setTimeout`/`window.setInterval` that automatically unregister installed callbacks
  * when the component unmounts.
  *
  * Provides interval methods that guarentee duration between callbacks.  Regular use of `setInterval` is fine
  * for callbacks with determined execution time.  However, if your callback could possibly take as long or longer
  * than your `timeout`, you can end up with callbacks firing back to back.
  *
  * Install in `ScalaComponent.build` via `.configure(TimerSupport.install)`.
  */
trait TimerSupportF[F[_]] extends OnUnmountF[F] { self =>
  import self.{onUnmountEffect => F}

  /** Invokes the callback `f` once after a minimum of `timeout` elapses. */
  final def setTimeout[G[_]](f: => G[Unit], timeout: FiniteDuration)(implicit G: Dispatch[G]): F[Unit] =
    setTimeoutMs(f, timeout.toMillis.toDouble)

  /** Invokes the callback `f` once after a minimum of `timeout` elapses. */
  final def setTimeoutMs[G[_]](f: => G[Unit], timeoutInMilliseconds: Double)(implicit G: Dispatch[G]): F[Unit] = {
    val first: F[F[Unit]] = F.delay {
      var handle: UndefOr[SetTimeoutHandle] = undefined
      val proc = F.chain(F.delay { handle = undefined }, F.transDispatch(f))
      handle = RawTimers.setTimeout(F.toJsFn(proc), timeoutInMilliseconds)
      F.delay(handle.foreach(RawTimers.clearTimeout))
    }
    F.flatMap(first)(onUnmount(_)(F))
  }

  /** Provides `setInterval`-like behavior insuring that the time between calls of `f` is *at least* the `timeout`. */
  final def setGuaranteedInterval[G[_]](f: G[Unit], interval: FiniteDuration)(implicit G: Dispatch[G]): F[Unit] =
    setGuaranteedIntervalMs(f, interval.toMillis.toDouble)

  /** Provides `setInterval`-like behavior insuring that the time between calls of `f` is *at least* the `timeout`. */
  final def setGuaranteedIntervalMs[G[_]](f: G[Unit], intervalInMilliseconds: Double)(implicit G: Dispatch[G]): F[Unit] = {
    val reschedule = F.suspend(setGuaranteedIntervalMs(f, intervalInMilliseconds))
    setTimeoutMs(F.finallyRun(F.transDispatch(f), reschedule), intervalInMilliseconds)(F)
  }

  /** Invokes the callback `f` repeatedly every `period`. */
  final def setInterval[G[_]](f: G[Unit], period: FiniteDuration)(implicit G: Dispatch[G]): F[Unit] =
    setIntervalMs(f, period.toMillis.toDouble)

  final def setIntervalMs[G[_]](f: G[Unit], periodInMilliseconds: Double)(implicit G: Dispatch[G]): F[Unit] = {
    val first: F[F[Unit]] = F.delay {
      val i = RawTimers.setInterval(F.toJsFn(F.transDispatch(f)), periodInMilliseconds)
      F.delay(RawTimers.clearInterval(i))
    }
    F.flatMap(first)(onUnmount(_)(F))
  }
}

object TimerSupportF {
  @inline def install[F[_]: Dispatch, P, C <: Children, S, B <: TimerSupportF[F], U <: UpdateSnapshot]: ScalaComponent.Config[P, C, S, B, U, U] =
    OnUnmountF.install[F, P, C, S, B, U]
}

// =====================================================================================================================

/** Alternatives to `window.setTimeout`/`window.setInterval` that automatically unregister installed callbacks
  * when the component unmounts.
  *
  * Provides interval methods that guarentee duration between callbacks.  Regular use of `setInterval` is fine
  * for callbacks with determined execution time.  However, if your callback could possibly take as long or longer
  * than your `timeout`, you can end up with callbacks firing back to back.
  *
  * Install in `ScalaComponent.build` via `.configure(TimerSupport.install)`.
  */
trait TimerSupport extends TimerSupportF[DefaultEffects.Sync] with OnUnmount

object TimerSupport {
  @inline def install[P, C <: Children, S, B <: TimerSupport, U <: UpdateSnapshot]: ScalaComponent.Config[P, C, S, B, U, U] =
    TimerSupportF.install(DefaultEffects.Sync)
}
