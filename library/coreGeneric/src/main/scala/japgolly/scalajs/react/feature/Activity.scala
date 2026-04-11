package japgolly.scalajs.react.feature

import japgolly.scalajs.react.facade
import japgolly.scalajs.react.vdom._
import scala.scalajs.js

  /** Lets you hide and restore the UI and internal state of its children.
    *
    * @since 4.0.0 / React 19
    */
object Activity {

  sealed abstract class Mode(val raw: String)
  case object Visible extends Mode("visible")
  case object Hidden extends Mode("hidden")

  def apply(mode: Mode)(ns: VdomNode*): VdomElement = {
    val props = js.Dynamic.literal("mode" -> mode.raw)
    VdomElement(facade.React.createElement(facade.React.Activity, props, ns.map(_.rawNode): _*))
  }
}
