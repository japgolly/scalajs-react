package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object UseState {

  val Component = ScalaFnComponent.withHooks[Int]
    .useState(123)
    .useStateBy((p, s1) => p + s1.value)
    .useStateBy($ => $.props + $.hook1.value + $.hook2.value)
    .renderRR { (_, s1, s2, s3) =>
      val sum = s1.value + s2.value + s3.value
      <.button(
        "Sum = ", sum,
        ^.onClick --> s1.modState(_ + 1),
      )
    }
}
