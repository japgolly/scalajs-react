package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

// This also tests:
//   - render with Ctx instead of CtxFn[_]
//   - returning a primative as vdom
object UseStateWithReuse {

  val Component = ScalaFnComponent.withHooks[Int]
    .useStateWithReuse(123)
    .useStateWithReuseBy((p, s1) => p + s1.value)
    .useStateWithReuseBy($ => $.props + $.hook1.value + $.hook2.value)
    .renderRR { $ =>
      val sum = $.hook1.value + $.hook2.value + $.hook3.value
      sum
    }
}
