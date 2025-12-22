package downstream

import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react.test.ReactTestUtils
import utest._
import cats.effect.testing.utest.EffectTestSuite
import cats.effect.IO
import scala.concurrent.duration._

object CBIOBundleTests extends EffectTestSuite[IO] {

  override def tests = Tests {
    Globals.clear()

    "catnip" - {
      ReactTestUtils.withRendered(Catnip.Component("omg")) { m =>
        IO.sleep(500.millis).map { _ =>
          assertEq(Globals.catnipMounts, List("omg"))
          m.outerHTML.assert("<div>Hello(1) omg</div>")
        }
      }.map { _ =>
        assertEq(Globals.catnipMounts, List("omg"))
      }
    }

  }
}
