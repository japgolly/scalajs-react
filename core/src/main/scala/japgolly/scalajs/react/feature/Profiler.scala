package japgolly.scalajs.react.feature

import japgolly.scalajs.react.util.JsUtil
import japgolly.scalajs.react.vdom.PackageBase._
import japgolly.scalajs.react.{Callback, facade}
import java.time.Duration
import scala.scalajs.js

/** The Profiler measures how often a React application renders and what the "cost" of rendering is. Its purpose is to
  * help identify parts of an application that are slow and may benefit from optimizations such as memoization.
  *
  * See https://reactjs.org/docs/profiler.html
  *
  * @since React 16.9.0 / scalajs-react 1.7.0
  */
object Profiler {

  /** The Profiler measures how often a React application renders and what the "cost" of rendering is. Its purpose is to
    * help identify parts of an application that are slow and may benefit from optimizations such as memoization.
    *
    * See https://reactjs.org/docs/profiler.html
    *
    * @param children Use `React.Fragment` to group multiple children.
    * @since React 16.9.0 / scalajs-react 1.7.0
    */
  def apply(id: String, onRender: OnRenderData => Callback)(children: VdomNode): VdomElement = {
    val onRenderRaw: facade.Profiler.OnRender =
      (
        id,
        phase,
        actualDuration,
        baseDuration,
        startTime,
        commitTime,
        interactions,
      ) => {
        val data = OnRenderData(
          id               = id,
          phase            = phase,
          actualDurationMs = actualDuration,
          baseDurationMs   = baseDuration,
          startTime        = startTime,
          commitTime       = commitTime,
          rawInteractions  = interactions,
        )
        onRender(data).runNow()
      }

    val props = js.Dynamic.literal(
      "id" -> id,
      "onRender" -> onRenderRaw)

    VdomElement(
      facade.React.createElement(
        facade.React.Profiler,
        props,
        children.rawNode))
  }

  /** Data returned by the Profiler.
    *
    * @param id The id prop of the Profiler tree that has just committed. This can be used to identify which part of the tree was committed if you are using multiple profilers.
    * @param phase Identifies whether the tree has just been mounted for the first time or re-rendered due to a change in props, state, or hooks.
    * @param actualDurationMs Time spent rendering the Profiler and its descendants for the current update. This indicates how well the subtree makes use of memoization (e.g. React.memo, useMemo, shouldComponentUpdate). Ideally this value should decrease significantly after the initial mount as many of the descendants will only need to re-render if their specific props change.
    * @param baseDurationMs Duration of the most recent render time for each individual component within the Profiler tree. This value estimates a worst-case cost of rendering (e.g. the initial mount or a tree with no memoization).
    * @param startTime Timestamp when React began rendering the current update.
    * @param commitTime Timestamp when React committed the current update. This value is shared between all profilers in a commit, enabling them to be grouped if desirable.
    */
  final case class OnRenderData(id              : String,
                                phase           : String,
                                actualDurationMs: Double,
                                baseDurationMs  : Double,
                                startTime       : Double,
                                commitTime      : Double,
                                rawInteractions : js.Iterable[facade.Interaction],
                               ) {

    def phaseIsMount: Boolean =
      phase == "mount"

    def phaseIsUpdate: Boolean =
      phase == "update"

    /** Time spent rendering the Profiler and its descendants for the current update. This indicates how well the subtree makes use of memoization (e.g. React.memo, useMemo, shouldComponentUpdate). Ideally this value should decrease significantly after the initial mount as many of the descendants will only need to re-render if their specific props change. */
    lazy val actualDuration: Duration =
      JsUtil.durationFromDOMHighResTimeStamp(actualDurationMs)

    /** Duration of the most recent render time for each individual component within the Profiler tree. This value estimates a worst-case cost of rendering (e.g. the initial mount or a tree with no memoization). */
    lazy val baseDuration: Duration =
      JsUtil.durationFromDOMHighResTimeStamp(baseDurationMs)

    /** Set of "interactions" that were being traced when the update was scheduled
      * (e.g. when render or setState were called).
      */
    lazy val interactions: Vector[Interaction] =
      rawInteractions.iterator.map(Interaction.fromRaw).toVector
  }

  final case class Interaction(id       : Int,
                               name     : String,
                               timestamp: Double)

  object Interaction {
    def fromRaw(r: facade.Interaction): Interaction =
      apply(
        id        = r.id,
        name      = r.name,
        timestamp = r.timestamp,
      )
  }

  @inline private def now(): Double =
    facade.performance.now()

  /** CAUTION: Unstable API. React may modify or remove this method without notice or deprecation and so might scalajs-react.
    *
    * Traces a new interaction (by appending to the existing set of interactions). The callback function will be executed
    * and its return value will be returned to the caller. Any code run within that callback will be attributed to that
    * interaction. Calls to unstable_wrap() will schedule async work within the same zone.
    */
  def unstable_trace[A](name: String)(body: => A): A =
    facade.React.SecretInternals.SchedulerTracing.unstable_trace(
      name,
      now(),
      () => body)
}
