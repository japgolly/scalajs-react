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
        val n = mounted.getDOMNode()
        assertOuterHTML(n, "<div>Hello Bob</div>")
        assertEq(mounted.isMounted(), true)
        assertEq(mounted.props.name, "Bob")
        assertEq(mounted.children, raw.emptyReactNodeList)
        assertEq(mounted.state, ())
      }
    }


  }
}


object ScalaClassSTest extends TestSuite {

  case class State(num: Int)

  val Component =
    CompScala.build[Unit]("State, no Props")
      .initialState(State(123))
      .render_S(s => raw.React.createElement("div", null, "State = ", s.num))
      .build

  override def tests = TestSuite {

    'render {
      val unmounted = Component()
      assertEq(unmounted.children, raw.emptyReactNodeList)
      assertEq(unmounted.key, None)
      assertEq(unmounted.ref, None)
      withBodyContainer { mountNode =>
        val mounted = unmounted.renderIntoDOM(mountNode) //.asInstanceOf[raw.ReactComponent[js.Object] with JsMethods]
        val n = mounted.getDOMNode()

        assertOuterHTML(n, "<div>State = 123</div>")
        assertEq(mounted.isMounted(), true)
        assertEq(mounted.children, raw.emptyReactNodeList)
        assertEq(mounted.state.num, 123)

        mounted.setState(State(666))
        assertOuterHTML(n, "<div>State = 666</div>")
        assertEq(mounted.isMounted(), true)
        assertEq(mounted.children, raw.emptyReactNodeList)
        assertEq(mounted.state.num, 666)

//        mounted.inc()
//        assertOuterHTML(n, "<div>State = 667</div>")
//        assertEq(mounted.isMounted(), true)
//        assertEq(mounted.props.children, raw.emptyReactNodeList)
//        assertEq(mounted.state.asInstanceOf[JsState].num, 667)
      }
    }


  }
}