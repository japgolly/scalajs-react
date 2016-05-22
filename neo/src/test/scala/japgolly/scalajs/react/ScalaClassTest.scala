package japgolly.scalajs.react

import scalajs.js
import utest._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.DebugJs._
import japgolly.scalajs.react.test.TestUtil._
import scalaz.Equal

object ScalaClassPTest extends TestSuite {

  case class Props(name: String)

  val Component =
    CompScala.build[Props]("HelloMessage")
      .stateless
      .noBackend
      .render_P(p => raw.React.createElement("div", null, "Hello ", p.name))
      .build

  override def tests = TestSuite {

    val unmounted = Component(Props("Bob"))
    assertEq(unmounted.props.name, "Bob")
    assertEq(unmounted.propsChildren, raw.emptyReactNodeList)
    assertEq(unmounted.key, None)
    assertEq(unmounted.ref, None)
    withBodyContainer { mountNode =>
      val mounted = unmounted.renderIntoDOM(mountNode)
      val n = mounted.getDOMNode.runNow()
      assertOuterHTML(n, "<div>Hello Bob</div>")
      assertEq(mounted.isMounted.runNow(), true)
      assertEq(mounted.props.runNow().name, "Bob")
      assertEq(mounted.propsChildren.runNow(), raw.emptyReactNodeList)
      assertEq(mounted.state.runNow(), ())
      assertEq(mounted.backend, ())
    }

  }
}


object ScalaClassSTest extends TestSuite {

  case class State(num1: Int, s2: State2)
  case class State2(num2: Int, num3: Int)

  implicit val equalState: Equal[State] = Equal.equalA
  implicit val equalState2: Equal[State2] = Equal.equalA

  class Backend($: CompScala.BackendScope[Unit, State]) {
    val inc: Callback =
      $.modState(s => s.copy(s.num1 + 1))
  }

  val Component =
    CompScala.build[Unit]("State, no Props")
      .initialState(State(123, State2(400, 7)))
      .backend(new Backend(_))
      .render_S(s => raw.React.createElement("div", null, "State = ", s.num1, " + ", s.s2.num2, " + ", s.s2.num3))
      .build

  override def tests = TestSuite {

    val unmounted = Component()
    assertEq(unmounted.propsChildren, raw.emptyReactNodeList)
    assertEq(unmounted.key, None)
    assertEq(unmounted.ref, None)
    withBodyContainer { mountNode =>
      val mounted = unmounted.renderIntoDOM(mountNode)
      val n = mounted.getDOMNode.runNow()

      assertOuterHTML(n, "<div>State = 123 + 400 + 7</div>")
      assertEq(mounted.isMounted.runNow(), true)
      assertEq(mounted.propsChildren.runNow(), raw.emptyReactNodeList)
      assertEq(mounted.state.runNow(), State(123, State2(400, 7)))
      val b = mounted.backend

      mounted.setState(State(666, State2(500, 7))).runNow()
      assertOuterHTML(n, "<div>State = 666 + 500 + 7</div>")
      assertEq(mounted.isMounted.runNow(), true)
      assertEq(mounted.propsChildren.runNow(), raw.emptyReactNodeList)
      assertEq(mounted.state.runNow(), State(666, State2(500, 7)))
      assert(mounted.backend eq b)

      mounted.backend.inc.runNow()
      assertOuterHTML(n, "<div>State = 667 + 500 + 7</div>")
      assertEq(mounted.isMounted.runNow(), true)
      assertEq(mounted.propsChildren.runNow(), raw.emptyReactNodeList)
      assertEq(mounted.state.runNow(), State(667, State2(500, 7)))
      assert(mounted.backend eq b)

      val zoomed = mounted
        .zoomState(_.s2)((s, b) => State(s.num1, b))
        .zoomState(_.num2)((s, n) => State2(n, s.num3))
      assertEq(zoomed.state.runNow(), 500)
      zoomed.modState(_ + 1).runNow()
      assertOuterHTML(n, "<div>State = 667 + 501 + 7</div>")
      assertEq(mounted.isMounted.runNow(), true)
      assertEq(mounted.propsChildren.runNow(), raw.emptyReactNodeList)
      assertEq(mounted.state.runNow(), State(667, State2(501, 7)))
      assert(mounted.backend eq b)
    }

  }
}