package japgolly.scalajs.react.core

import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test.TestTimer
import japgolly.scalajs.react.{Callback, CallbackTo}
import utest._

object CallbackTest extends TestSuite {

  override def tests = Tests {

    "attempt" - {
      val t = new RuntimeException("fake error")
      val a = CallbackTo.throwException(t).attempt.runNow()
      a ==> Left(t)
    }

    "memo" - {
      var count = 0
      val inc = Callback(count += 1)
      val c = inc.memo()
      (c >> c.memo() >> c).runNow()
      assert(count == 1)
    }

    /*
    "future" - {
      val X = ScalaComponent.builder[Unit]("X")
        .initialState(1)
        .render_S(s => <.div("state = ", s))
        .build

      val x = ReactTestUtils renderIntoDocument X()

      "direct" - {
        var i = 10
        val ff = x.future.modState(_ + 1, Callback(i -= 7))
        val f: Future[Unit] = ff
        f.map { _ =>
          assert(x.state == 2)
          assert(i == 3)
        }
      }

      "callback" - {
        var i = 90
        val cbb = x.accessCB.future.modState(_ + 1, Callback(i -= 5))
        val cb: CallbackTo[Future[Unit]] = cbb
        // Look, it's repeatable
        for {
          _ <- cb.runNow()
          _ <- cb.runNow()
        } yield {
          assert(x.state == 3)
          assert(i == 80)
        }
      }
    }
    */

    "stackSafety" - {
      import japgolly.scalajs.react.CatsReact._
      type F[A] = CallbackTo[A]
      "nestedFlatMapsInTailrecLoop"    - StackSafety.nestedFlatMapsInTailrecLoop[F]
      "nestedFlatMapsInNonTailrecLoop" - StackSafety.nestedFlatMapsInNonTailrecLoop[F]
    }

    "debounce" - {
      val t = new TestTimer
      var i = 0
      val c = Callback(i += 1)._debounceMs(100)(t)

      c.runNow()
      assertEq(i, 0)
      t.progressTimeBy(90)
      assertEq(i, 0)
      t.progressTimeBy(20)
      assertEq(i, 1)
      t.progressTimeBy(120)
      assertEq(i, 1)

      c.runNow()
      c.runNow()
      t.progressTimeBy(80)
      c.runNow()
      t.progressTimeBy(20)
      assertEq(i, 1)
      t.progressTimeBy(70)
      assertEq(i, 1)
      t.progressTimeBy(20)
      assertEq(i, 2)

      t.progressTimeBy(2000)
      assertEq(i, 2)
    }
  }
}
