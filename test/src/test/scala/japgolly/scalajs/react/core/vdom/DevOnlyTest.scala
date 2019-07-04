package japgolly.scalajs.react.core.vdom

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.all._
import scala.scalajs.LinkingInfo.developmentMode
import utest._

// Also checked with bin/checkDevOnly via Travis CI
object DevOnlyTest extends TestSuite {

  val tests = Tests {

    def test(t: VdomTag)(dev: => String, prod: => String): Unit = {
      val exp = if (developmentMode) dev else prod
      assertRender(ScalaComponent.static("")(t)(), exp)
    }

    "tagMod" - testTagMod()
    def testTagMod() =
      test(div(1, TagMod devOnly p("DEVONLY-TEST"), 2))(
        "<div>1<p>DEVONLY-TEST</p>2</div>",
        "<div>12</div>")

    "tagMod" - testTagMod2()
    def testTagMod2() =
      test(div(1, TagMod devOnly TagMod(cls := "DEVONLY-TEST", p("DEVONLY-TEST")), 2))(
        """<div class="DEVONLY-TEST">1<p>DEVONLY-TEST</p>2</div>""",
        "<div>12</div>")

    "attr" - testAttr()
    def testAttr() =
      test(div(VdomAttr.devOnly("data-devonly-test") := "!DEVONLY-TEST!", 123))(
        """<div data-devonly-test="!DEVONLY-TEST!">123</div>""",
        "<div>123</div>")

    "style" - testStyle()
    def testStyle() =
      test(div(VdomStyle.devOnly("devonly-test") := "!DEVONLY-TEST!", 123))(
        """<div style="devonly-test:!DEVONLY-TEST!">123</div>""",
        "<div>123</div>")

  }
}
