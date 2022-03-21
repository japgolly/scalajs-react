package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.univeq.UnivEq
import scala.annotation.nowarn
import scala.util.NotGiven

sealed trait Compilation3Test {
  import CompilationTest._
  import Compilation3Test._

  sealed trait TestComponentBuilder {
    val step1 = ScalaComponent.builder[Int]("")

    step1.renderBackend[B3b]
    step1.backend[B3b](new B3b(_)).renderBackend
  }

  // Ensure that the ScalaJsReactConfig.Defaults trait contains a default value for every config method
  class ScalaJsReactConfigDefaults extends ScalaJsReactConfig.Defaults

  // Reusability derives
  locally {
    case class Mono(a: Int) derives Reusability, UnivEq
    implicitly[Reusability[Mono]]

    case class Poly[+A](a: A) derives Reusability, UnivEq
    implicitly[Reusability[Poly[Int]]]
    implicitly[NotGiven[Reusability[Poly[B3b]]]]
  }
}

@nowarn
object Compilation3Test {
  import japgolly.scalajs.react.vdom.html_<^._
  import CompilationTest._

  class B3b($: BackendScope[Int, Unit])(using i: Imp) {
    def render: VdomNode = 123
  }
}
