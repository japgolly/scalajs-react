package japgolly.scalajs.react

import scalajs.js
import utest._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.DebugJs._
import japgolly.scalajs.react.test.TestUtil._

object ScalaClassPTest extends TestSuite {

  case class Props(name: String)

  val Component =
    CompScala.build[Props]("HelloMessage")
      .stateless
      .noBackend
      .render_P(p => raw.React.createElement("div", null, "Hello ", p.name))
      .build

  override def tests = TestSuite {

    'render {
      val unmounted = Component(Props("Bob"))
      assertEq(unmounted.props.name, "Bob")
      assertEq(unmounted.children, raw.emptyReactNodeList)
      assertEq(unmounted.key, None)
      assertEq(unmounted.ref, None)
      withBodyContainer { mountNode =>
        val mounted = unmounted.renderIntoDOM(mountNode)
        val n = mounted.getDOMNode.runNow()
        assertOuterHTML(n, "<div>Hello Bob</div>")
        assertEq(mounted.isMounted.runNow(), true)
        assertEq(mounted.props.runNow().name, "Bob")
        assertEq(mounted.children.runNow(), raw.emptyReactNodeList)
        assertEq(mounted.state.runNow(), ())
        assertEq(mounted.backend, ())
      }
    }


  }
}


object ScalaClassSTest extends TestSuite {

  case class State(num: Int)

  class Backend($: CompScala.BackendScope[Unit, State]) {
    val inc: Callback =
      $.modState(s => State(s.num + 1))
  }

  val Component =
    CompScala.build[Unit]("State, no Props")
      .initialState(State(123))
      .backend(new Backend(_))
      .render_S(s => raw.React.createElement("div", null, "State = ", s.num))
      .build

  override def tests = TestSuite {

    'render {
      val unmounted = Component()
      assertEq(unmounted.children, raw.emptyReactNodeList)
      assertEq(unmounted.key, None)
      assertEq(unmounted.ref, None)
      withBodyContainer { mountNode =>
        val mounted = unmounted.renderIntoDOM(mountNode)
        val n = mounted.getDOMNode.runNow()

        assertOuterHTML(n, "<div>State = 123</div>")
        assertEq(mounted.isMounted.runNow(), true)
        assertEq(mounted.children.runNow(), raw.emptyReactNodeList)
        assertEq(mounted.state.runNow().num, 123)
        val b = mounted.backend

        mounted.setState(State(666)).runNow()
        assertOuterHTML(n, "<div>State = 666</div>")
        assertEq(mounted.isMounted.runNow(), true)
        assertEq(mounted.children.runNow(), raw.emptyReactNodeList)
        assertEq(mounted.state.runNow().num, 666)
        assert(mounted.backend eq b)

        mounted.backend.inc.runNow()
        assertOuterHTML(n, "<div>State = 667</div>")
        assertEq(mounted.isMounted.runNow(), true)
        assertEq(mounted.children.runNow(), raw.emptyReactNodeList)
        assertEq(mounted.state.runNow().num, 667)
        assert(mounted.backend eq b)
      }
    }


  }
}