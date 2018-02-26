package japgolly.scalajs.react.core.vdom

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import utest._

object VdomTest extends TestSuite {

  val C = ScalaComponent.static("")(<.br)
  val Span = ScalaComponent.builder[Unit]("").render_C(<.span(_)).build

  override def tests = Tests {

    'returnTypes {
      def test(subj: VdomNode, exp: String): Unit = {
        val comp = ScalaComponent.static("tmp")(subj)
        assertRender(comp(), exp)
      }

      'byte      - test(50: Byte,                                      "50")
      'short     - test(45: Short,                                     "45")
      'int       - test(666,                                           "666")
      'long      - test(123L,                                          "123")
      'double    - test(12.3,                                          "12.3")
      'string    - test("yo",                                          "yo")
      'empty     - test(EmptyVdom,                                     "")
      'optionN   - test(Option.empty[Int],                             "")
      'optionS   - test(Option(13),                                    "13")
      'optionSN  - test(Option(Option.empty[Int]),                     "")
      'optionSS  - test(Option(Option(13)),                            "13")
      'vdomArray - test(VdomArray("hehe", <.div(^.key := 1, "one")),   "hehe<div>one</div>") // needs keys apparently
      'fragment  - test(VdomFragment("hehe", <.div("one")),            "hehe<div>one</div>") // keys not required
      'fragmentK - test(VdomFragment.withKey(1)("hehe", <.div("one")), "hehe<div>one</div>") // keyABLE

      'booleanF  - compileError("""test(false, "")""")
      'booleanT  - compileError("""test(true, "")""")
    }

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
