package downstream

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Carrot {

  Globals.onComponentInit()

  val Component = ScalaComponent.builder[String]("CarRot!")
    .render { $ =>
      Globals.carrotRenders += 1
      <.div("Hello ", $.props)
    }
    .configure(Reusability.shouldComponentUpdate)
    .build
}
