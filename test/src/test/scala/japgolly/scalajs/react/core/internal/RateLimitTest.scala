package japgolly.scalajs.react.core.internal

import japgolly.scalajs.react.AsyncCallback
import japgolly.scalajs.react.internal.RateLimit
import japgolly.scalajs.react.test.TestUtil._
import utest._

object RateLimitTest extends TestSuite {

  private final class Tester {
    var time = System.currentTimeMillis()
    val clock: RateLimit.Clock = () => time

    var runs = 0
    val run = () => runs += 1

    def apply(maxRuns: Int, windowSizeMs: Long) =
      RateLimit.fn0(run, maxRuns, windowSizeMs, clock)
  }

  override def tests = Tests {
    val t = new Tester

    "single" - {
      val f = t(1, 50L)
      assertEq(t.runs, 0)

      f()
      assertEq(t.runs, 1)

      t.time += 20
      f()
      assertEq(t.runs, 1)

      t.time += 10
      f()
      assertEq(t.runs, 1)

      t.time += 19
      f()
      assertEq(t.runs, 1)

      t.time += 2
      f()
      assertEq(t.runs, 2)

      t.time += 20
      f()
      assertEq(t.runs, 2)

      t.time += 200
      f()
      assertEq(t.runs, 3)

      t.time += 1
      f()
      assertEq(t.runs, 3)
    }

    "multiple" - {
      val f = t(3, 500L)
      assertEq(t.runs, 0)

      f()
      assertEq(t.runs, 1)

      t.time += 100 // [-100, 0]
      f()
      assertEq(t.runs, 2)

      t.time += 100 // [-200, -100, 0]
      f()
      assertEq(t.runs, 3)

      t.time += 100 // [-300, -200, -100]
      f()
      assertEq(t.runs, 3)

      t.time += 250 // [-450, -350, 0]
      f()
      assertEq(t.runs, 4)

      t.time += 30 // [-480, -380, -30]
      f()
      assertEq(t.runs, 4)

      t.time += 30 // [-410, -60, 0]
      f()
      assertEq(t.runs, 5)

      t.time += 30 // [-440, -90, -30]
      f()
      assertEq(t.runs, 5)

      t.time += 460 // [-490, 0]
      f()
      assertEq(t.runs, 6)

      f(); assertEq(t.runs, 7) // [-490, 0, 0]
      f(); assertEq(t.runs, 7)
      f(); assertEq(t.runs, 7)
      f(); assertEq(t.runs, 7)
      f(); assertEq(t.runs, 7)

      t.time += 1000
      f()
      assertEq(t.runs, 8)
    }

    "async" - {
      var result = Option.empty[Boolean]
      def results() = (t.runs, result)
      val f = AsyncCallback.delay(t.run())._rateLimitMs(50L, 1, t.clock).map(x => result = Some(x.isDefined)).toCallback.toScalaFn

      f()
      assertEq(results(), (1, Some(true)))

      f()
      assertEq(results(), (1, Some(false)))

      t.time += 40
      f()
      assertEq(results(), (1, Some(false)))

      t.time += 40
      f()
      assertEq(results(), (2, Some(true)))
    }

  }
}
