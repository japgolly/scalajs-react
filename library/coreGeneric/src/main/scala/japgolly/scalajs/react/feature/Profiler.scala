package japgolly.scalajs.react.feature

import japgolly.scalajs.react.facade
import japgolly.scalajs.react.util.Effect.Sync
import japgolly.scalajs.react.util.JsUtil
import japgolly.scalajs.react.vdom.PackageBase._
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
  def apply[F[_], A](id: String, onRender: OnRenderData => F[A])(children: VdomNode)(implicit F: Sync[F]): VdomElement = {
    val onRenderRaw: facade.Profiler.OnRender =
      (
        id,
        phase,
        actualDuration,
        baseDuration,
        startTime,
        commitTime,
      ) => {
        val data = OnRenderData(
          id               = id,
          phase            = phase,
          actualDurationMs = actualDuration,
          baseDurationMs   = baseDuration,
          startTime        = startTime,
          commitTime       = commitTime,
        )
        F.runSync(onRender(data))
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
  }
}
