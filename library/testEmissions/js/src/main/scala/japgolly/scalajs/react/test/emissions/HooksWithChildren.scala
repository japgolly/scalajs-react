package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object HooksWithChildren {

  val Component = ScalaFnComponent.withHooks[Int]
    .withPropsChildren
    .useState(123)
    .render { (p, c, s1) =>
      val sum = p + s1.value + c.count
      <.button(
        "Sum = ", sum,
        ^.onClick --> s1.modState(_ + 1),
        c
      )
    }
}