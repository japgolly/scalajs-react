package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object HooksWithChildrenCtxObj {

  val Component = ScalaFnComponent.withHooks[Int]
    .withPropsChildren
    .useState(123)
    .renderRR { $ =>
      val sum = $.props + $.hook1.value + $.propsChildren.count
      sum
    }
}
