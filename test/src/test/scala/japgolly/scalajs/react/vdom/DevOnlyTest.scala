package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.TestUtil._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all._
import scala.scalajs.LinkingInfo.developmentMode
import utest._

// Also checked with bin/checkDevOnly via Travis CI
object DevOnlyTest extends TestSuite {

  val tests = TestSuite {

    def test(t: ReactTag)(dev: => String, prod: => String): Unit = {
      val exp = if (developmentMode) dev else prod
      assertRender(FunctionalComponent[Unit](_ => t)(()), exp)
    }

    'tagMod - testTagMod()
    def testTagMod() =
      test(div(1, TagMod devOnly p("DEVONLY-TEST"), 2))(
        "<div>1<p>DEVONLY-TEST</p>2</div>",
        "<div>12</div>")

    'tagMod - testTagMod2()
    def testTagMod2() =
      test(div(1, TagMod devOnly TagMod(cls := "DEVONLY-TEST", p("DEVONLY-TEST")), 2))(
        """<div class="DEVONLY-TEST">1<p>DEVONLY-TEST</p>2</div>""",
        "<div>12</div>")

    'attr - testAttr()
    def testAttr() =
      test(div(ReactAttr.devOnly("data-devonly-test") := "!DEVONLY-TEST!", 123))(
        """<div data-devonly-test="!DEVONLY-TEST!">123</div>""",
        "<div>123</div>")

    'style - testStyle()
    def testStyle() =
      test(div(ReactStyle.devOnly("devonly-test") := "!DEVONLY-TEST!", 123))(
        """<div style="devonly-test:!DEVONLY-TEST!;">123</div>""",
        "<div>123</div>")

  }
}
