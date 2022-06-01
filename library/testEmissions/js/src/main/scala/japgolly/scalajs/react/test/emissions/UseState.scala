package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object UseState {

  val Component = ScalaFnComponent.withHooks[Unit]
    .useState(123)
    .render { (_, s) =>
      <.button(
        "Count is ", s.value,
        ^.onClick --> s.modState(_ + 1),
      )
    }
}
