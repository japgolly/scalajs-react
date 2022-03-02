package japgolly.scalajs.react.test.reactrefresh

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Counter {

  val Component = ScalaFnComponent.withHooks[Unit]
    .useState(33333333)
    .render { (_, s) =>
      <.button(
        "Count is ", s.value,
        ^.onClick --> s.modState(_ + 1),
      )
    }
}
