package downstream

import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react.test.ReactTestUtils._
import utest._
import scala.concurrent.Future
import scala.concurrent.Promise
import concurrent.ExecutionContext.Implicits.global
import cats.effect.IO
import scalajs.js

object CBIOBundleTests extends TestSuite {

  def delay(milliseconds: Int): Future[Unit] = {
    val p = Promise[Unit]()
    js.timers.setTimeout(milliseconds) {
      p.success(())
    }
    p.future
  }

  override def tests = Tests {
    Globals.clear()

    "catnip" - {
      // withRenderedIntoDocumentAsync(Catnip.Component("omg")) { m =>
      withRenderedIntoDocumentFuture(Catnip.Component("omg")) { m =>
        delay(500).map(_ =>
          // assertEq(Globals.catnipMounts, List("omg"))
            assertEq(m.showDom(), "<div>Hello(1) omg</div>")
          )
      }
      // assertEq(Globals.catnipMounts, List("omg"))
    }

  }
}
