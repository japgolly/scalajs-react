package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object NoHooksWithChildren {

  val Component = ScalaFnComponent.withHooks[Int]
    .withPropsChildren
    .render { (p, c) =>
      val sum = p + c.count
      <.div("DEBUG = ", sum)
    }
}
