package downstream

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scala.scalajs.js.annotation._

object Carrot {

  @JSExportTopLevel("CAR_ROT")
  val Component = ScalaComponent.builder[String]("CarRot!")
    .render($ => <.div("Hello ", $.props))
    .build
}
