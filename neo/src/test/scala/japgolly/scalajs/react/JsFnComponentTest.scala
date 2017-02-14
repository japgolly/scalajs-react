package japgolly.scalajs.react

import scalajs.js
import utest._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.internal.JsUtil.inspectObject
import japgolly.scalajs.react.test.TestUtil._

object JsFnComponentTest extends TestSuite {
  @js.native
  trait JsProps extends js.Object {
    val name: String
  }

  def JsProps(name: String): JsProps =
    js.Dynamic.literal("name" -> name).asInstanceOf[JsProps]

  val Component = JsFnComponent[JsProps, ChildrenArg.None]("FnComp")

  override def tests = TestSuite {

    'noChildren {
      'main {
        val unmounted = Component(JsProps("Bob"))
        assertEq(unmounted.props.name, "Bob")
        assertEq(unmounted.propsChildren.count, 0)
        assertEq(unmounted.propsChildren.isEmpty, true)
        assertEq(unmounted.key, None)
//        assertEq(unmounted.ref, None)
        withBodyContainer { mountNode =>
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
        withBodyContainer { mountNode =>
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