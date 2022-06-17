package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object JustPropsViaHookApi {

  val Component = ScalaFnComponent.withHooks[Int]
    .renderRR { p =>
      val magicNumber = p + 654
      magicNumber
    }
}
