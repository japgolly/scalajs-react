package japgolly.scalajs.react.test

import utest._
import TestUtil._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object ReactTestVarTest extends TestSuite {

  override def tests = Tests {

    val v = ReactTestVar(3)

    "stateSnapshot" - {
      val t = v.stateSnapshot()
      assertEq(t.value, 3)
      t.setState(7).runNow()
      assertEq(v.value(), 7)
      t.setState(9).runNow()
      assertEq(v.value(), 9)
    }

    "stateSnapshotWithReuse" - {
      val t = v.stateSnapshotWithReuse()
      assertEq(t.value, 3)
      t.setState(9).runNow()
      assertEq(v.value(), 9)
      t.setState(7).runNow()
      assertEq(v.value(), 7)
    }

    "stateAccess" - {
      val $ = v.stateAccess.withEffectsImpure
      assertEq($.state, 3)
      $.setState(14)
      assertEq($.state, 14)
      $.setState(8)
      assertEq($.state, 8)
      var called = 0
      $.modState(_ - 2, Callback(called += 1))
      assertEq($.state, 6)
      assertEq(called, 1)
    }

    "reset" - {
      v.setValue(666)
      assertEq(v.value(), 666)
      assertEq(v.initialValue, 3)
      v.reset()
      assertEq(v.value(), 3)
      v.reset()
      assertEq(v.value(), 3)
    }

    "history" - {
      assertEq(v.history(), Vector(3))
      v.setValue(5)
      assertEq(v.history(), Vector(3, 5))
      v.stateSnapshot().setState(7).runNow()
      assertEq(v.history(), Vector(3, 5, 7))
      v.stateSnapshotWithReuse().setState(8).runNow()
      assertEq(v.history(), Vector(3, 5, 7, 8))
      v.stateAccess.setState(1).runNow()
      assertEq(v.history(), Vector(3, 5, 7, 8, 1))
      v.setValue(1)
      assertEq(v.history(), Vector(3, 5, 7, 8, 1, 1))
      v.reset()
      assertEq(v.history(), Vector(3))
      v.setValue(3)
      assertEq(v.history(), Vector(3, 3))
    }

    "mockParentComponent" - {
      val c = ScalaComponent.builder[StateAccessPure[Int]]("")
        .render_P(parent => <.div(parent.state.runNow(), ^.onClick --> parent.modState(_ + 1)))
        .build
      val v = ReactTestVar(1)
      ReactTestUtils.withRenderedIntoDocument(c(v.stateAccess)) { m =>
        v.onUpdate(m.forceUpdate)
        assertRendered(m.getDOMNode.asMounted().asElement(), "<div>1</div>")
        Simulate.click(m.getDOMNode.asMounted().asElement())
        assertEq(v.value(), 2)
        assertRendered(m.getDOMNode.asMounted().asElement(), "<div>2</div>")
      }
    }
  }
}
