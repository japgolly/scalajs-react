package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.internal.CoreGeneral.BackendScope
import japgolly.scalajs.react.vdom.Exports.VdomNode

sealed trait CompilationTest {
  import CompilationTest._

  // ===================================================================================================================
  sealed trait TestComponentBuilder {
    val step1 = ScalaComponent.builder[Int]("")

    step1.renderBackend[B2]
      .configure(Reusability.shouldComponentUpdateAndLog("omg"))
      .configure(Reusability.shouldComponentUpdateAnd(_.log("omg")))
      .configure(ReusabilityOverlay.install)
  }
}

object CompilationTest {
  class B2($: BackendScope[Int, Unit]) {
    def render = VdomNode.cast(123)
  }
}
