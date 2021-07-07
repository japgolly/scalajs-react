package downstream

import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react.test.ReactTestUtils._
import utest._

object CatsEffectBundleTests extends TestSuite {

  override def tests = Tests {
    Globals.clear()

    "catnip" - {
      withRenderedIntoDocument(Catnip.Component("omg")) { m =>
        assertEq(Globals.catnipMounts, List("omg"))
        assertEq(m.showDom(), "<div>Hello omg</div>")
      }
      assertEq(Globals.catnipMounts, List("omg"))
    }

  }
}
