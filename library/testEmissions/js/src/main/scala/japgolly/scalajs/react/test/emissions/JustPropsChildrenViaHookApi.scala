package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object JustPropsChildrenViaHookApi {

  val Component = ScalaFnComponent.withHooks[Int]
    .withPropsChildren
    .renderRR { (p, c) =>
      val sum = p + c.count
      sum
    }
}
