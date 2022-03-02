package japgolly.scalajs.react.test.reactrefresh

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object UseState2 {

  val Component = ScalaFnComponent.withHooks[Unit]
    .useState(2)
    .render { (_, s) =>
      <.button(
        "Count is ", s.value,
        ^.onClick --> s.modState(_ + 1),
      )
    }
}
