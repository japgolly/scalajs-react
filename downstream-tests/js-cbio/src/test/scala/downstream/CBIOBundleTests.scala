package downstream

import concurrent.ExecutionContext.Implicits.global
import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react.test.ReactTestUtils2
import scala.concurrent.Future
import scala.concurrent.Promise
import scalajs.js
import utest._

object CBIOBundleTests extends TestSuite {

  private def delay(milliseconds: Int): Future[Unit] = {
    val p = Promise[Unit]()
    js.timers.setTimeout(milliseconds) {
      p.success(())
    }
    p.future
  }

  override def tests = Tests {
    Globals.clear()

    "catnip" - {
      ReactTestUtils2.withRenderedSync(Catnip.Component("omg")).future { m =>
        delay(500).map { _ =>
          assertEq(Globals.catnipMounts, List("omg"))
          m.outerHTML.assert("<div>Hello(1) omg</div>")
        }
      }.map { _ =>
        assertEq(Globals.catnipMounts, List("omg"))
      }
    }

  }
}
