package japgolly.scalajs.react.test

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import utest._
import TestUtil2._

object ComponentTesterTest extends TestSuite {

  val C = ReactComponentB[String]("asd")
    .initialState(0)
    .renderPS((_, p, s) => <.div(s" $p:$s "))
    .build

  override def tests = TestSuite {

    var ran = false
    ComponentTester(C)("a") { t =>
      import t._

      def assert(p: String, s: Int) = assertEq(component.outerHtmlWithoutReactDataAttr(), s"<div> $p:$s </div>")

      assert("a", 0)

      setState(2)
      assert("a", 2)

      setProps("b")
      assert("b", 2)

      setState(6)
      assert("b", 6)

      ran = true
    }
    assert(ran)
  }
}
