package japgolly.scalajs.react.facade

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
trait Interaction extends js.Object {
  val __count  : Int    = js.native
  val id       : Int    = js.native
  val name     : String = js.native
  val timestamp: Double  = js.native
}

object Profiler {

  type OnRender = js.Function7[
    String,              // id:             the "id" prop of the Profiler tree that has just committed
    String,              // phase:          either "mount" (if the tree just mounted) or "update" (if it re-rendered)
    Double,              // actualDuration: time spent rendering the committed update
    Double,              // baseDuration:   estimated time to render the entire subtree without memoization
    Double,              // startTime:      when React began rendering this update
    Double,              // commitTime:     when React committed this update
    js.Set[Interaction], // interactions:   Set of interactions belonging to this update
    Unit
  ]

}

@js.native
@JSGlobal("performance")
object performance extends js.Object {
  def now(): Double = js.native
}