package downstream

import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react.test.ReactTestUtils2
import utest._
import cats.effect.testing.utest.EffectTestSuite
import cats.effect.IO

object CatsEffectBundleTests extends EffectTestSuite[IO] {

  override def tests = Tests {
    Globals.clear()

    "catnip" - {
      ReactTestUtils2.withRendered_(Catnip.Component("omg")) { m =>
        assertEq(Globals.catnipMounts, List("omg"))
        m.outerHTML.assert("<div>Hello omg</div>")
      }.map(_ =>
        assertEq(Globals.catnipMounts, List("omg"))
      )
    }

  }
}
