package japgolly.scalajs.react.core.vdom

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import utest._

object VdomTest extends TestSuite {

  val C = ScalaComponent.static("")(<.br)
  val Span = ScalaComponent.builder[Unit]("").render_C(<.span(_)).build

  override def tests = Tests {

    'tagModToJs - {
      'childrenAsVdomNodes - {
        val vdom = TagMod("hehe", 123, <.em(456L), C())
        val expect = "<span>hehe123<em>456</em><br/></span>"
        assertRender(<.span(vdom), expect)
        assertRender(Span(vdom.toJs.childrenAsVdomNodes: _*), expect)
      }
    }

  }
}
