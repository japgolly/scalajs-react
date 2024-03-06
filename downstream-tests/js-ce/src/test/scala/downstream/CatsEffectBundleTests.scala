package downstream

import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react.test.ReactTestUtils2
import utest._

object CatsEffectBundleTests extends TestSuite {

  override def tests = Tests {
    Globals.clear()

    "catnip" - {
      ReactTestUtils2.withRendered(Catnip.Component("omg")) { m =>
        assertEq(Globals.catnipMounts, List("omg"))
        m.outerHTML.assert("<div>Hello omg</div>")
      }
      assertEq(Globals.catnipMounts, List("omg"))
    }

  }
}
