package japgolly.scalajs.react

import scalajs.js
import utest._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.DebugJs._
import japgolly.scalajs.react.test.TestUtil._

object JsEs3PTest extends TestSuite {

  @js.native
  trait JsProps extends js.Object {
    val name: String
  }

  def JsProps(name: String): JsProps =
    js.Dynamic.literal("name" -> name).asInstanceOf[JsProps]

  val RawClass = js.Dynamic.global.ES3_P.asInstanceOf[raw.ReactClass]
  val Component = CompJs3.Constructor[JsProps, Null](RawClass)
  compileError(""" Component() """)

  override def tests = TestSuite {

    'main {
      val unmounted = Component(JsProps("Bob"))
      assertEq(unmounted.props.name, "Bob")
      assertEq(unmounted.propsChildren, raw.emptyReactNodeList)
      assertEq(unmounted.key, None)
      assertEq(unmounted.ref, None)
      withBodyContainer { mountNode =>
        val mounted = unmounted.renderIntoDOM(mountNode)
        val n = mounted.getDOMNode
        assertOuterHTML(n, "<div>Hello Bob</div>")
        assertEq(mounted.isMounted, true)
        assertEq(mounted.props.name, "Bob")
        assertEq(mounted.propsChildren, raw.emptyReactNodeList)
        assertEq(mounted.state, null)
      }
    }

    'ctorReuse -
      assert(Component(JsProps("a")) ne Component(JsProps("b")))

  }
}


object JsEs3STest extends TestSuite {

  @js.native
  trait JsState extends js.Object {
    val num1: Int
    val num2: Int
  }

  @js.native
  trait JsMethods extends js.Object {
    def inc(): Unit = js.native
  }

  val RawClass = js.Dynamic.global.ES3_S.asInstanceOf[raw.ReactClass]
  val Component = CompJs3.Constructor[Null, JsState](RawClass).mapMounted(_.addRawType[JsMethods])
  compileError(""" Component(null) """)

  override def tests = TestSuite {
    def JsState1(num1: Int): JsState =
      js.Dynamic.literal("num1" -> num1).asInstanceOf[JsState]
    def JsState(num1: Int, num2: Int): JsState =
      js.Dynamic.literal("num1" -> num1, "num2" -> num2).asInstanceOf[JsState]

    'main {
      val unmounted = Component()
      assertEq(unmounted.propsChildren, raw.emptyReactNodeList)
      assertEq(unmounted.key, None)
      assertEq(unmounted.ref, None)
      withBodyContainer { mountNode =>
        val mounted = unmounted.renderIntoDOM(mountNode) //.asInstanceOf[raw.ReactComponent[js.Object] with JsMethods]
        val n = mounted.getDOMNode

        assertOuterHTML(n, "<div>State = 123 + 500</div>")
        assertEq(mounted.isMounted, true)
        assertEq(mounted.propsChildren, raw.emptyReactNodeList)
        assertEq(mounted.state.num1, 123)
        assertEq(mounted.state.num2, 500)

        mounted.setState(JsState1(666))
        assertOuterHTML(n, "<div>State = 666 + 500</div>")
        assertEq(mounted.isMounted, true)
        assertEq(mounted.propsChildren, raw.emptyReactNodeList)
        assertEq(mounted.state.num1, 666)
        assertEq(mounted.state.num2, 500)

        mounted.rawInstance.inc()
        assertOuterHTML(n, "<div>State = 667 + 500</div>")
        assertEq(mounted.isMounted, true)
        assertEq(mounted.propsChildren, raw.emptyReactNodeList)
        assertEq(mounted.state.num1, 667)
        assertEq(mounted.state.num2, 500)

        val zoomed = mounted.zoomState(_.num2)((s, n) => JsState(s.num1, n))
        assertEq(zoomed.state, 500)
        zoomed.modState(_ + 1)
        assertOuterHTML(n, "<div>State = 667 + 501</div>")
        assertEq(mounted.isMounted, true)
        assertEq(mounted.propsChildren, raw.emptyReactNodeList)
        assertEq(mounted.state.num1, 667)
        assertEq(mounted.state.num2, 501)
      }
    }

    'ctorReuse -
      assert(Component() eq Component())

  }
}

