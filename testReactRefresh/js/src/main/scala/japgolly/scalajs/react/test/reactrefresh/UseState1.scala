package japgolly.scalajs.react.test.reactrefresh

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object UseState1 {

  val Component = ScalaFnComponent.withHooks[Unit]
    .useState(1)
    .useState(2)
    // .useCallback(Callback.log("HELLO"))
    .render2 { (_, s, _) =>
      org.scalajs.dom.console.log("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXx")
      org.scalajs.dom.console.log("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXx")
      org.scalajs.dom.console.log("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXx")
      val x =
      <.button(
        "Count is ", s.value,
        ^.onClick --> s.modState(_ + 1),
      )
      org.scalajs.dom.console.log("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXx")
      org.scalajs.dom.console.log("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXx")
      org.scalajs.dom.console.log("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXx")
      x
    }
}
