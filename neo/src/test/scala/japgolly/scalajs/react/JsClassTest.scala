package japgolly.scalajs.react

import scalajs.js
import utest._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.DebugJs._
import japgolly.scalajs.react.test.TestUtil._
import org.scalajs.dom.document

object JsClassPTest extends TestSuite {

  @js.native
  trait JsProps extends js.Object {
    val name: String
  }

  def JsProps(name: String): JsProps =
    js.Dynamic.literal("name" -> name).asInstanceOf[JsProps]

  val RawClass =
    js.Dynamic.global.ComponentClassP.asInstanceOf[raw.ReactClass[JsProps]]

  override def tests = TestSuite {

    'render {
      val unmounted = raw.React.createElement(RawClass, JsProps("Bob")).asInstanceOf[raw.ReactComponentElement[JsProps]]
      assertEq(unmounted.props.name, "Bob")
      assertEq(unmounted.props.children, raw.emptyReactNodeList)
      assertEq(unmounted.key, null)
      assertEq(unmounted.ref, null)
      withBodyContainer { mountNode =>
        val mounted = raw.ReactDOM.render(unmounted, mountNode).asInstanceOf[raw.ReactComponent[JsProps]]
        val n = raw.ReactDOM.findDOMNode(mounted)
        assertOuterHTML(n, "<div>Hello Bob</div>")
        assertEq(mounted.isMounted(), true)
        assertEq(mounted.props.name, "Bob")
        assertEq(mounted.props.children, raw.emptyReactNodeList)
        assertEq(mounted.state, null)
      }
    }


  }
}

object JsClassSTest extends TestSuite {

  @js.native
  trait JsState extends js.Object {
    val num: Int
  }
  def JsState(num: Int): JsState =
    js.Dynamic.literal("num" -> num).asInstanceOf[JsState]

  @js.native
  trait JsMethods extends js.Object {
    def inc(): Unit
  }

  val RawClass =
    js.Dynamic.global.ComponentClassS.asInstanceOf[raw.ReactClass[Nothing]]

  override def tests = TestSuite {

    'render {
      val unmounted = raw.React.createElement(RawClass).asInstanceOf[raw.ReactComponentElement[js.Object]]
      assertEq(unmounted.props.children, raw.emptyReactNodeList)
      assertEq(unmounted.key, null)
      assertEq(unmounted.ref, null)
      withBodyContainer { mountNode =>
        val mounted = raw.ReactDOM.render(unmounted, mountNode).asInstanceOf[raw.ReactComponent[js.Object] with JsMethods]
        val n = raw.ReactDOM.findDOMNode(mounted)

        assertOuterHTML(n, "<div>State = 123</div>")
        assertEq(mounted.isMounted(), true)
        assertEq(mounted.props.children, raw.emptyReactNodeList)
        assertEq(mounted.state.asInstanceOf[JsState].num, 123)

        mounted.setState(JsState(666))
        assertOuterHTML(n, "<div>State = 666</div>")
        assertEq(mounted.isMounted(), true)
        assertEq(mounted.props.children, raw.emptyReactNodeList)
        assertEq(mounted.state.asInstanceOf[JsState].num, 666)

        mounted.inc()
        assertOuterHTML(n, "<div>State = 667</div>")
        assertEq(mounted.isMounted(), true)
        assertEq(mounted.props.children, raw.emptyReactNodeList)
        assertEq(mounted.state.asInstanceOf[JsState].num, 667)
      }
    }


  }
}
