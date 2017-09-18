package japgolly.scalajs.react.core

import scalajs.js
import scalajs.js.annotation._
import utest._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.TestUtil._

object JsFnComponentTest extends TestSuite {
  @JSGlobal("FnComp")
  @js.native
  object RawComp extends js.Object

  @js.native
  trait JsProps extends js.Object {
    val name: String
  }

  def JsProps(name: String): JsProps =
    js.Dynamic.literal("name" -> name).asInstanceOf[JsProps]

  lazy val Component = JsFnComponent[JsProps, Children.None](RawComp)

  override def tests = Tests {

//    'displayName {
//      println(Component.raw)
//      println(internal.JsUtil inspectObject Component.raw)
//    }

    'noChildren {
      'main {
        val unmounted = Component(JsProps("Bob"))
        assertEq(unmounted.props.name, "Bob")
        assertEq(unmounted.propsChildren.count, 0)
        assertEq(unmounted.propsChildren.isEmpty, true)
        assertEq(unmounted.key, None)
//        assertEq(unmounted.ref, None)
        ReactTestUtils.withNewBodyElement { mountNode =>
          val mounted = unmounted.renderIntoDOM(mountNode)
          val n = mountNode
          assertOuterHTML(n, "<div><div>Hello Bob</div></div>")
//          assertEq(mounted.isMounted, true)
//          assertEq(mounted.props.name, "Bob")
//          assertEq(mounted.propsChildren.count, 0)
//          assertEq(mounted.propsChildren.isEmpty, true)
//          assertEq(mounted.state, null)
        }
      }

      'key {
        val unmounted = Component.withKey("hehe")(JsProps("Bob"))
        assertEq(unmounted.props.name, "Bob")
        assertEq(unmounted.propsChildren.count, 0)
        assertEq(unmounted.propsChildren.isEmpty, true)
        assertEq(unmounted.key, Some("hehe": Key))
//        assertEq(unmounted.ref, None)
        ReactTestUtils.withNewBodyElement { mountNode =>
          val mounted = unmounted.renderIntoDOM(mountNode)
          val n = mountNode
          assertOuterHTML(n, "<div><div>Hello Bob</div></div>")
//          assertEq(mounted.isMounted, true)
//          assertEq(mounted.props.name, "Bob")
//          assertEq(mounted.propsChildren.count, 0)
//          assertEq(mounted.propsChildren.isEmpty, true)
//          assertEq(mounted.state, null)
        }
      }

      'ctorReuse -
        assert(Component(JsProps("a")) ne Component(JsProps("b")))
    }

  }
}