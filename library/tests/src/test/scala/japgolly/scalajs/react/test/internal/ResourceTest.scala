package japgolly.scalajs.react.test.internal

import japgolly.scalajs.react._
import japgolly.microlibs.testutil.TestUtil._
import utest._

object ResourceTest extends TestSuite {

  override def tests = Tests {

    "error_in_acquire" - {
      var n = 0
      val nRes = Resource.make[CallbackTo, Unit](Callback { n += 1 }, _ => Callback { n -= 1 })
      val errRes = Resource.make[CallbackTo, Int](CallbackTo { ??? : Int }, _ => Callback { n -= 100 })
      val r = nRes.flatMap(_ => nRes.flatMap(_ => errRes))
      val t = r.use_(_ => ()).attemptTry.runNow()
      assert(t.isFailure)
      assertEq(n, 0)
    }

  }
}
