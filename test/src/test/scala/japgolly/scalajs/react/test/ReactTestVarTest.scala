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
  }
}
