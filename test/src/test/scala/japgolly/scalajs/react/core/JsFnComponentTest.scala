package japgolly.scalajs.react.core

import scalajs.js
import scalajs.js.annotation._
import utest._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._

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

//    "displayName" - {
//      println(Component.raw)
//      println(internal.JsUtil inspectObject Component.raw)
//    }

    "noChildren" - {
      "main" - {
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

      "key" - {
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

      "ctorReuse" -
        assert(Component(JsProps("a")) ne Component(JsProps("b")))
    }

    'fromScala {

      'const {
        val c = JsFnComponent.fromScala.const(<.div("ah"))
        assertRender(c(), "<div>ah</div>")
      }

      'byName {
        val c = JsFnComponent.fromScala.byName(<.div("ah"))
        assertRender(c(), "<div>ah</div>")
      }

      'apply {
        val c = JsFnComponent.fromScala[JsProps](p => <.div(p.name))
        assertRender(c(JsProps("hi")), "<div>hi</div>")
      }

      'withChildren {
        val c = JsFnComponent.fromScala.withChildren[JsProps]((p, c) => <.div(c, p.name))
        assertRender(c(JsProps("hi"))("name: "), "<div>name: hi</div>")
      }

      'justChildren {
        val c = JsFnComponent.fromScala.justChildren(c => <.div(c))
        assertRender(c("ok"), "<div>ok</div>")
      }

    }

    'toComponent {

      'nullary {
        val s = ScalaComponent.static("")(<.div("ah"))
        val c = s.toJsComponent
        assertRender(c(), "<div>ah</div>")
      }

      'props {
        val s = ScalaComponent.builder[String]("").render_P(<.div(_)).build
        val c = s.cmapCtorProps[JsProps](_.name).toJsComponent
        assertRender(c(JsProps("hi")), "<div>hi</div>")
      }

      'withChildren {
        val s = ScalaComponent.builder[String]("").render_PC((p, c) => <.div(c, p)).build
        val c = s.cmapCtorProps[JsProps](_.name).toJsComponent
        assertRender(c(JsProps("hi"))("name: "), "<div>name: hi</div>")
      }

      'justChildren {
        val s = ScalaComponent.builder[Unit]("").render_C(<.div(_)).build
        val c = s.toJsComponent
        assertRender(c("ok"), "<div>ok</div>")
      }

    }
  }
}