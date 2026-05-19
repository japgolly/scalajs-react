package japgolly.scalajs.react.facade

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

object Profiler {

  type OnRender = js.Function6[

    // id: the "id" prop of the Profiler tree that has just committed
    String,

    // phase: either "mount" (if the tree just mounted) or "update" (if it re-rendered)
    String,

    // actualDuration: time spent rendering the committed update
    Double,

    // baseDuration: estimated time to render the entire subtree without memoization
    Double,

    // startTime: when React began rendering this update
    Double,

    // commitTime: when React committed this update
    Double,

    Unit
  ]

}

@js.native
@JSGlobal("performance")
object performance extends js.Object {
  def now(): Double = js.native
}
