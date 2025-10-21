package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import scala.scalajs.js.annotation._

object LinkingTest {
  import CompilationTest._

  @JSExportTopLevel("LinkingTest")
  def _test(): Long = {
    var h = 0L

    def test(a: Any): Unit =
      h += a.##

    test {
      ScalaComponent.builder[Int]("")
        .backend(new B2(_))
        .render(_.backend.render)
        .configure(Reusability.shouldComponentUpdateAndLog("omg"))
        .configure(Reusability.shouldComponentUpdateAnd(_.log("omg")))
        .configure(ReusabilityOverlay.install)
    }

    h
  }
}
