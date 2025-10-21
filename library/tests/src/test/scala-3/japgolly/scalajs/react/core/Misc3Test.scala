package japgolly.scalajs.react.core

import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import utest._
import japgolly.scalajs.react.Reusability

object Misc3Test extends TestSuite {
  japgolly.scalajs.react.test.InitTestEnv()

  override def tests = Tests {

    "cssUnits" - {
      "static" - {
        assertTastyEq(0.px, "0")
        assertTastyEq(0.0.px, "0")
        assertTastyEq(1.px, "1px")
      }
      "dynamic" - {
        def x(n: Int) = n.px
        assertEq(x(0), "0")
        assertEq(x(1), "1px")
      }
    }

    // "reusability" - {
    //   "boxing" - {
    //     case class P(i: Int, j: Int)
    //     showCode(Reusability.derive[P])
    //   }
    // }
  }
}
