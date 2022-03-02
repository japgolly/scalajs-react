package japgolly.scalajs.react.test.reactrefresh

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object UseStateMulti {

  val Component = ScalaFnComponent.withHooks[Unit]
    .useState(1)
    .useState(2)
    // .useStateBy()
    .render { (_, s1, s2) =>
      <.div(
        <.button("Count #1 is ", s1.value, ^.onClick --> s1.modState(_ + 1)),
        <.button("Count #2 is ", s2.value, ^.onClick --> s2.modState(_ + 1)),
      )
    }
}
