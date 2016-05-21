package japgolly.scalajs.react

import scalajs.js
import utest._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.DebugJs._
import japgolly.scalajs.react.test.TestUtil._

object JsClassPTest extends TestSuite {

  @js.native
  trait JsProps extends js.Object {
    val name: String
  }

  def JsProps(name: String): JsProps =
    js.Dynamic.literal("name" -> name).asInstanceOf[JsProps]

  val RawClass = js.Dynamic.global.ComponentClassP.asInstanceOf[raw.ReactClass]

  val Component = CompJs3.Constructor[JsProps, Null](RawClass)

  override def tests = TestSuite {

    'render {
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


  }
}


object JsClassSTest extends TestSuite {

  @js.native
  trait JsState extends js.Object {
    val num: Int
  }

  @js.native
  trait JsMethods extends js.Object {
    def inc(): Unit
  }

  val RawClass = js.Dynamic.global.ComponentClassS.asInstanceOf[raw.ReactClass]
  val Component = CompJs3.Constructor[Null, JsState](RawClass)

  override def tests = TestSuite {
    def JsState(num: Int): JsState =
      js.Dynamic.literal("num" -> num).asInstanceOf[JsState]

    'render {
      val unmounted = Component(null)
      assertEq(unmounted.propsChildren, raw.emptyReactNodeList)
      assertEq(unmounted.key, None)
      assertEq(unmounted.ref, None)
      withBodyContainer { mountNode =>
        val mounted = unmounted.renderIntoDOM(mountNode) //.asInstanceOf[raw.ReactComponent[js.Object] with JsMethods]
        val n = mounted.getDOMNode

        assertOuterHTML(n, "<div>State = 123</div>")
        assertEq(mounted.isMounted, true)
        assertEq(mounted.propsChildren, raw.emptyReactNodeList)
        assertEq(mounted.state.num, 123)

        mounted.setState(JsState(666))
        assertOuterHTML(n, "<div>State = 666</div>")
        assertEq(mounted.isMounted, true)
        assertEq(mounted.propsChildren, raw.emptyReactNodeList)
        assertEq(mounted.state.num, 666)

//        mounted.inc()
//        assertOuterHTML(n, "<div>State = 667</div>")
//        assertEq(mounted.isMounted, true)
//        assertEq(mounted.props.propsChildren, raw.emptyReactNodeList)
//        assertEq(mounted.state.asInstanceOf[JsState].num, 667)
      }
    }


  }
}


object JsClassSTestX extends TestSuite {

  @js.native
  trait JsState extends js.Object {
    val num: Int
  }

  @js.native
  trait JsMethods extends js.Object {
    def inc(): Unit = js.native
  }

  val RawClass = js.Dynamic.global.ComponentClassS.asInstanceOf[raw.ReactClass]
  val Component = CompJs3.Constructor_NoProps[JsState](RawClass).mapMounted(_.addRawType[JsMethods])


  override def tests = TestSuite {
    def JsState(num: Int): JsState =
      js.Dynamic.literal("num" -> num).asInstanceOf[JsState]

    'render {
      val unmounted = Component()
      assertEq(unmounted.propsChildren, raw.emptyReactNodeList)
      assertEq(unmounted.key, None)
      assertEq(unmounted.ref, None)
      withBodyContainer { mountNode =>
        val mounted = unmounted.renderIntoDOM(mountNode) //.asInstanceOf[raw.ReactComponent[js.Object] with JsMethods]
        val n = mounted.getDOMNode

        assertOuterHTML(n, "<div>State = 123</div>")
        assertEq(mounted.isMounted, true)
        assertEq(mounted.propsChildren, raw.emptyReactNodeList)
        assertEq(mounted.state.num, 123)

        mounted.setState(JsState(666))
        assertOuterHTML(n, "<div>State = 666</div>")
        assertEq(mounted.isMounted, true)
        assertEq(mounted.propsChildren, raw.emptyReactNodeList)
        assertEq(mounted.state.num, 666)

        mounted.rawInstance.inc()
        assertOuterHTML(n, "<div>State = 667</div>")
        assertEq(mounted.isMounted, true)
        assertEq(mounted.propsChildren, raw.emptyReactNodeList)
        assertEq(mounted.state.num, 667)
      }
    }


  }
}

