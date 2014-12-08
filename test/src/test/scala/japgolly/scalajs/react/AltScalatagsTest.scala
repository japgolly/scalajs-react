package japgolly.scalajs.react

import japgolly.scalajs.react.TestUtil._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.HTMLInputElement
import utest._
import scala.scalajs.js
import scala.scalajs.js.{Array => JArray}

object AltScalatagsTest extends TestSuite {

  lazy val CA = ReactComponentB[Unit]("CA").render((_,c) => <.div(c)).buildU
  lazy val CB = ReactComponentB[Unit]("CB").render((_,c) => <.span(c)).buildU
  lazy val H1 = ReactComponentB[String]("H").render(p => <.h1(p)).build

  lazy val SI = ReactComponentB[Unit]("SI")
    .initialState(123)
    .render(T => <.input(^.value := T.state.toString))
    .domType[HTMLInputElement]
    .buildU

  val tests = TestSuite {

    'altScalatags {
      def test(subj: ReactElement, exp: String): Unit =
        ReactComponentB[Unit]("tmp").render((_,_) => subj).buildU.apply() shouldRender exp
      def reactNode: ReactNode = H1("cool")
      def checkbox(check: Boolean) = <.input(^.`type` := "checkbox", ^.checked := check)

      'int       - test(<.div(123),                              "<div>123</div>")
      'long      - test(<.div(123L),                             "<div>123</div>")
      'double    - test(<.div(12.3),                             "<div>12.3</div>")
      'jsNumber  - test(<.div(123: js.Number),                   "<div>123</div>")
      'string    - test(<.div("yo"),                             "<div>yo</div>")
      'reactNode - test(<.div(reactNode),                        "<div><h1>cool</h1></div>")
      'comp      - test(<.div(H1("a")),                          "<div><h1>a</h1></div>")
//      'raw     - test(<.div(raw("<div>hehe</div>")),           """<div>&lt;div&gt;hehe&lt;/div&gt;</div>""")
      'seqTag    - test(<.div(Seq (<.span(1), <.span(2))),       "<div><span>1</span><span>2</span></div>")
      'listTag   - test(<.div(List(<.span(1), <.span(2))),       "<div><span>1</span><span>2</span></div>")
      'listComp  - test(<.div(List(H1("a"), H1("b"))),           "<div><h1>a</h1><h1>b</h1></div>")
      'list2jAry - test(<.div(List(H1("a"), H1("b")).toJsArray), "<div><h1>a</h1><h1>b</h1></div>")
      'jAryTag   - test(<.div(JArray(<.span(1), <.span(2))),     "<div><span>1</span><span>2</span></div>")
      'jAryComp  - test(<.div(JArray(H1("a"), H1("b"))),         "<div><h1>a</h1><h1>b</h1></div>")
      'checkboxT - test(checkbox(true),                          """<input type="checkbox" checked>""")
      'checkboxF - test(checkbox(false),                         """<input type="checkbox">""")

      'dangerouslySetInnerHtml - test(<.div(^.dangerouslySetInnerHtml("<span>")), "<div><span></div>")

      'combination {
        test(<.div(^.cls := "hi", "Str: ", 123, JArray(H1("a"), H1("b")), <.p(^.cls := "pp")("!")),
        """<div class="hi">Str: 123<h1>a</h1><h1>b</h1><p class="pp">!</p></div>""")
      }
    }

    'classSet {
      'allConditional {
        val r = ReactComponentB[(Boolean,Boolean)]("C").render(p => <.div(^.classSet("p1" -> p._1, "p2" -> p._2))("x")).build
        r((false, false)) shouldRender """<div>x</div>"""
        r((true,  false)) shouldRender """<div class="p1">x</div>"""
        r((false, true))  shouldRender """<div class="p2">x</div>"""
        r((true,  true))  shouldRender """<div class="p1 p2">x</div>"""
      }
      'hasMandatory {
        val r = ReactComponentB[Boolean]("C").render(p => <.div(^.classSet1("mmm", "ccc" -> p))("x")).build
        r(false) shouldRender """<div class="mmm">x</div>"""
        r(true)  shouldRender """<div class="mmm ccc">x</div>"""
      }
    }
  }
}
