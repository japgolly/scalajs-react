package japgolly.scalajs.react

import japgolly.scalajs.react.raw.ReactComponent

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

  val RawClass = js.Dynamic.global.ComponentClassP.asInstanceOf[raw.ReactClass]

  val Component = new CompJs3.Constructor[JsProps, Null](RawClass)

  override def tests = TestSuite {

    'render {
      val unmounted = Component(JsProps("Bob"))
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

  val RawClass = js.Dynamic.global.ComponentClassS.asInstanceOf[raw.ReactClass]
  val Component = new CompJs3.Constructor[Null, JsState](RawClass)

  override def tests = TestSuite {

    'render {
      val unmounted = Component(null)
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

        mounted.setState(JsState(666))
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


object JsClassSTestX extends TestSuite {

  @js.native
  trait JsState extends js.Object {
    val num: Int
  }
  def JsState(num: Int): JsState =
    js.Dynamic.literal("num" -> num).asInstanceOf[JsState]

  trait JsMethods extends CompJs3X.HasRaw {
    def inc(): Unit =
      rawDyn.inc()
  }

  val RawClass = js.Dynamic.global.ComponentClassS.asInstanceOf[raw.ReactClass]
  val Component = new CompJs3X.Constructor_NoProps[JsState, JsMethods](RawClass)

  implicit val xxxxxxxxxxxxxxxxxxxx: raw.ReactComponent => CompJs3X.Mounted[Null, JsState] with JsMethods =
    r => new CompJs3X.Mounted[Null, JsState] with JsMethods {
      override val rawInstance = r
    }

  trait TestAmbiguity extends CompJs3X.HasRaw
  implicit def xxxxxxxxxxxxxxxxxxxx2: raw.ReactComponent => CompJs3X.Mounted[Null, JsState] with TestAmbiguity = ???

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

        mounted.setState(JsState(666))
        assertOuterHTML(n, "<div>State = 666</div>")
        assertEq(mounted.isMounted(), true)
        assertEq(mounted.children, raw.emptyReactNodeList)
        assertEq(mounted.state.num, 666)

        mounted.inc()
        assertOuterHTML(n, "<div>State = 667</div>")
        assertEq(mounted.isMounted(), true)
        assertEq(mounted.children, raw.emptyReactNodeList)
        assertEq(mounted.state.num, 667)
      }
    }


  }
}

