package japgolly.scalajs.react

import japgolly.scalajs.react.vdom.html_<^._
import utest._

// Checked with bin/test-elision via Travis CI
object ElisionTest extends TestSuite {

  private val Normal =
    ScalaComponent.builder[Int]("COMP_NAMES_ARE_ELIDABLE").render_P(<.div(_)).build

  private val Static =
    ScalaComponent.builder.static("COMP_NAMES_ARE_ELIDABLE")(<.div).build

  override def tests = Tests {
    "normal" - s"[${Normal.raw.displayName}]"
    "static" - s"[${Static.raw.displayName}]"
  }

}
