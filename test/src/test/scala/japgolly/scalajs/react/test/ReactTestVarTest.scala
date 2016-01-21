package japgolly.scalajs.react.test

import utest._
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.TestUtil2._

object ReactTestVarTest extends TestSuite {

  override def tests = TestSuite {

    val v = ReactTestVar(3)

    'externalVar {
      val t = v.externalVar()
      assertEq(t.value, 3)
      t.set(7).runNow()
      assertEq(v.value(), 7)
      t.set(9).runNow()
      assertEq(v.value(), 9)
    }

    'reusableVar {
      val t = v.reusableVar()
      assertEq(t.value, 3)
      t.set(9).runNow()
      assertEq(v.value(), 9)
      t.set(7).runNow()
      assertEq(v.value(), 7)
    }

    'compStateAccess {
      val $ = v.compStateAccess().accessDirect
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

    'reset {
      v.setValue(666)
      assertEq(v.value(), 666)
      assertEq(v.initialValue, 3)
      v.reset()
      assertEq(v.value(), 3)
      v.reset()
      assertEq(v.value(), 3)
    }

    'history {
      assertEq(v.history(), Vector(3))
      v.setValue(5)
      assertEq(v.history(), Vector(3, 5))
      v.externalVar().set(7).runNow()
      assertEq(v.history(), Vector(3, 5, 7))
      v.reusableVar().set(8).runNow()
      assertEq(v.history(), Vector(3, 5, 7, 8))
      v.compStateAccess().setState(1).runNow()
      assertEq(v.history(), Vector(3, 5, 7, 8, 1))
      v.setValue(1)
      assertEq(v.history(), Vector(3, 5, 7, 8, 1, 1))
      v.reset()
      assertEq(v.history(), Vector(3))
      v.setValue(3)
      assertEq(v.history(), Vector(3, 3))
    }
  }
}
