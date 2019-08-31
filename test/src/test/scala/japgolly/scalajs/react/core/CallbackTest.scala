package japgolly.scalajs.react.core

import japgolly.scalajs.react.Callback
import utest._

object CallbackTest extends TestSuite {

  override def tests = Tests {

    'memo {
      var count = 0
      val inc = Callback(count += 1)
      val c = inc.memo()
      (c >> c.memo >> c).runNow()
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

  }
}
