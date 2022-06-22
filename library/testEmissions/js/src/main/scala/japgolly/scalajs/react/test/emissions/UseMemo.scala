package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object UseMemo {

  val Component = ScalaFnComponent.withHooks[Int]
    .useMemo("blah")(_.length)
    .useMemoBy((p, a) => (p + a).toString)((p, a) => _.length + p + a)
    .useMemoBy($ => ($.props + $.hook1 + $.hook2).toString)($ => _.length + $.props + $.hook1 + $.hook2)
    .renderRR { (p, a, b, c) =>
      val sum = p + a + b + c
      sum
    }
}
