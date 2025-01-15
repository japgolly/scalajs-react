package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils2
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.html_<^._
import sourcecode.Line
import utest._

object RenderableTest extends AsyncTestSuite {

  private def test[A: Renderable](source: A, expectHtml: String)(implicit l: Line): AsyncCallback[Unit] =
    ReactTestUtils2.withRendered_(source) { r =>
      r.outerHTML.assert(expectHtml)
    }

  override def tests = Tests {

    // TODO: Add undefined

    "text" - {
      test("cool", "cool")
    }

    "short" - {
      test(6.toShort, "6")
    }

    "int" - {
      test(3, "3")
    }

    "double" - {
      test(3.2, "3.2")
    }

    "long" - {
      test(500000000L, "500000000")
    }

    "rawNode" - {
      test(VdomNode("abc").rawNode, "abc")
    }

    "rawElement" - {
      test(<.div("oof").rawElement, "<div>oof</div>")
    }

    "vdomNode" - {
      test(VdomNode("hehe"), "hehe")
    }

    "vdomElement" - {
      test(<.div("ah"), "<div>ah</div>")
    }

    "jsComponent" - {
      import JsComponentEs6PTest._
      test(Component(JsProps("Nim")), "<div>Hello Nim</div>")
    }

    "jsFnComponent" - {
      import JsFnComponentTest._
      test(Component(JsProps("Aiden")), "<div>Hello Aiden</div>")
    }

    "scalaComponent" - {
      val ScalaComp = ScalaComponent.builder[Unit].render_(<.div("scala!")).build
      test(ScalaComp(), "<div>scala!</div>")
    }

    "scalaFnComponent" - {
      val ScalaFnComp = ScalaFnComponent[Unit](_ => <.div("scala fn!"))
      test(ScalaFnComp(), "<div>scala fn!</div>")
    }

    "jsForwardRef" - {
      import RefTest.TestRefForwarding.JsToVdom._
      test(Forwarder(), nullaryExpectation)
    }

    "scalaForwardRef" - {
      import RefTest.TestRefForwarding.ScalaToVdom._
      test(Forwarder(), nullaryExpectation)
    }
  }

}
