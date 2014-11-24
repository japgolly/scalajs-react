package japgolly.scalajs.react

import utest._
import org.scalajs.dom
import org.scalajs.dom.HTMLInputElement
import vdom.ReactVDom._, all.{Tag => _, _}
import TestUtil._
import test.{DebugJs, ReactTestUtils}
import scala.scalajs.js, js.{Array => JArray}

object CoreTest extends TestSuite {

  lazy val CA = ReactComponentB[Unit]("CA").render((_,c) => div(c)).buildU
  lazy val CB = ReactComponentB[Unit]("CB").render((_,c) => span(c)).buildU
  lazy val H1 = ReactComponentB[String]("H").render(p => h1(p)).build

  lazy val SI = ReactComponentB[Unit]("SI")
    .initialState(123)
    .render(T => input(value := T.state.toString))
    .domType[HTMLInputElement]
    .buildU

  val tests = TestSuite {

    'scalatags {
      def test(subj: ReactElement, exp: String): Unit =
        ReactComponentB[Unit]("tmp").render((_,_) => subj).buildU.apply() shouldRender exp
      def reactNode: ReactNode = H1("cool")
      def checkbox(check: Boolean) = input(`type` := "checkbox", checked := check)

      'int       - test(div(123),                              "<div>123</div>")
      'long      - test(div(123L),                             "<div>123</div>")
      'double    - test(div(12.3),                             "<div>12.3</div>")
      'jsNumber  - test(div(123: js.Number),                   "<div>123</div>")
      'string    - test(div("yo"),                             "<div>yo</div>")
      'reactNode - test(div(reactNode),                        "<div><h1>cool</h1></div>")
      'comp      - test(div(H1("a")),                          "<div><h1>a</h1></div>")
      'raw       - test(div(raw("<div>hehe</div>")),           """<div>&lt;div&gt;hehe&lt;/div&gt;</div>""")
      'seqTag    - test(div(Seq (span(1), span(2))),           "<div><span>1</span><span>2</span></div>")
      'listTag   - test(div(List(span(1), span(2))),           "<div><span>1</span><span>2</span></div>")
      'listComp  - test(div(List(H1("a"), H1("b"))),           "<div><h1>a</h1><h1>b</h1></div>")
      'list2jAry - test(div(List(H1("a"), H1("b")).toJsArray), "<div><h1>a</h1><h1>b</h1></div>")
      'jAryTag   - test(div(JArray(span(1), span(2))),         "<div><span>1</span><span>2</span></div>")
      'jAryComp  - test(div(JArray(H1("a"), H1("b"))),         "<div><h1>a</h1><h1>b</h1></div>")
      'checkboxT - test(checkbox(true),                        """<input type="checkbox" checked>""")
      'checkboxF - test(checkbox(false),                       """<input type="checkbox">""")

      'dangerouslySetInnerHtml - test(div(dangerouslySetInnerHtml("<span>")), "<div><span></div>")

      'combination {
        test(div(cls := "hi", "Str: ", 123, JArray(H1("a"), H1("b")), p(cls := "pp")("!")),
        """<div class="hi">Str: 123<h1>a</h1><h1>b</h1><p class="pp">!</p></div>""")
      }
    }

    'props {
      'unit {
        val r = ReactComponentB[Unit]("U").render((_,c) => h1(c)).buildU
        r(div("great")) shouldRender "<h1><div>great</div></h1>"
      }

      'required {
        val r = ReactComponentB[String]("C").render(name => div("Hi ", name)).build
        r("Mate") shouldRender "<div>Hi Mate</div>"
      }

      val O = ReactComponentB[String]("C").render(name => div("Hey ", name)).propsDefault("man").build
      'optionalNone {
        O() shouldRender "<div>Hey man</div>"
      }
      'optionalSome {
        O(Some("dude")) shouldRender "<div>Hey dude</div>"
      }

      'always {
        val r = ReactComponentB[String]("C").render(name => div("Hi ", name)).propsConst("there").build
        r() shouldRender "<div>Hi there</div>"
      }
    }

    'builder {
      'configure {
        var called = 0
        val f = (_: ReactComponentB[Unit,Unit,Unit]).componentWillMount(_ => called += 1)
        val c = ReactComponentB[Unit]("X").render(_ => div("")).configure(f, f).buildU
        ReactTestUtils.renderIntoDocument(c())
        assert(called == 2)
      }
    }

    'keys {
      'specifiableThruCtor {
        val k1 = "heasdf"
        val xx = CA.withKey(k1)()
        val k2 = xx.key
        k2 mustEqual k1
      }
    }

    'classSet {
      'allConditional {
        val r = ReactComponentB[(Boolean,Boolean)]("C").render(p => div(classSet("p1" -> p._1, "p2" -> p._2))("x")).build
        r((false, false)) shouldRender """<div>x</div>"""
        r((true,  false)) shouldRender """<div class="p1">x</div>"""
        r((false, true))  shouldRender """<div class="p2">x</div>"""
        r((true,  true))  shouldRender """<div class="p1 p2">x</div>"""
      }
      'hasMandatory {
        val r = ReactComponentB[Boolean]("C").render(p => div(classSet("mmm", "ccc" -> p))("x")).build
        r(false) shouldRender """<div class="mmm">x</div>"""
        r(true)  shouldRender """<div class="mmm ccc">x</div>"""
      }
    }

    'children {
      'argsToComponents {
        'listOfScalatags {
          CA(List(h1("nice"), h2("good"))) shouldRender "<div><h1>nice</h1><h2>good</h2></div>" }

        'listOfReactComponents {
          CA(List(CB(h1("nice")), CB(h2("good")))) shouldRender
            "<div><span><h1>nice</h1></span><span><h2>good</h2></span></div>" }
      }

      'rendersGivenChildren {
        'none { CA() shouldRender "<div></div>" }
        'one { CA(h1("yay")) shouldRender "<div><h1>yay</h1></div>" }
        'two { CA(h1("yay"), h3("good")) shouldRender "<div><h1>yay</h1><h3>good</h3></div>" }
        'nested { CA(CB(h1("nice"))) shouldRender "<div><span><h1>nice</h1></span></div>" }
      }

      'forEach {
        val C1 = collectorNC[ReactNode]((l, c) => c.forEach(l append _))
        val C2 = collectorNC[(ReactNode, Int)]((l, c) => c.forEach((a, b) => l.append((a, b))))

        'withoutIndex {
          val x = runNC(C1, h1("yay"), h3("good"))
          assert(x.size == 2)
        }

        'withIndex {
          val x = runNC(C2, h1("yay"), h3("good"))
          assert(x.size == 2)
          assert(x.toList.map(_._2) == List(0,1))
        }
      }

      'only {
        val A = collector1C[Option[ReactNode]](_.only)

        'one {
          val r = run1C(A, div("Voyager (AU) is an awesome band"))
          assert(r.isDefined)
        }

        'two {
          val r = run1C(A, div("The Pensive Disarray"), div("is such a good song"))
          assert(r == None)
        }
      }
    }

    'stateFocus {
      // def inc(s: ComponentStateFocus[Int]) = s.modState(_ * 3)
      case class SI(s: String, i: Int)
      val C = ReactComponentB[SI]("C").initialStateP(p => p).render(T => {
        val f = T.focusState(_.i)((a,b) => a.copy(i = b))
        // inc(f)
        div(T.state.s + "/" + (f.state*3))
      }).build
      C(SI("Me",7)) shouldRender "<div>Me/21</div>"
    }

    'mountedStateAccess {
      val c = ReactTestUtils.renderIntoDocument(SI())
      assert(c.state == 123)
    }

    'builtWithDomType {
      val c = ReactTestUtils.renderIntoDocument(SI())
      val v = c.getDOMNode().value // Look, it knows its DOM node type
      assert(v == "123")
    }
  }
}
