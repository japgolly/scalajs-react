package japgolly.scalajs.react

import utest._
import scala.scalajs.js, js.{Array => JArray}
import org.scalajs.dom.HTMLInputElement
import vdom.all._
import TestUtil._
import test.{DebugJs, ReactTestUtils}

object CoreTest extends TestSuite {

  lazy val CA = ReactComponentB[Unit]("CA").render((_,c) => div(c)).buildU
  lazy val CB = ReactComponentB[Unit]("CB").render((_,c) => span(c)).buildU
  lazy val H1 = ReactComponentB[String]("H").render(p => h1(p)).build

  lazy val SI = ReactComponentB[Unit]("SI")
    .initialState(123)
    .render(T => input(value := T.state.toString))
    .domType[HTMLInputElement]
    .buildU

  val tagmod  : TagMod       = cls := "ho"
  val reacttag: ReactTag     = span()
  val relement: ReactElement = span()

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
      'seqTag    - test(div(Seq (span(1), span(2))),           "<div><span>1</span><span>2</span></div>")
      'listTag   - test(div(List(span(1), span(2))),           "<div><span>1</span><span>2</span></div>")
      'listComp  - test(div(List(H1("a"), H1("b"))),           "<div><h1>a</h1><h1>b</h1></div>")
      'list2jAry - test(div(List(H1("a"), H1("b")).toJsArray), "<div><h1>a</h1><h1>b</h1></div>")
      'jAryTag   - test(div(JArray(span(1), span(2))),         "<div><span>1</span><span>2</span></div>")
      'jAryComp  - test(div(JArray(H1("a"), H1("b"))),         "<div><h1>a</h1><h1>b</h1></div>")
      'checkboxT - test(checkbox(true),                        """<input type="checkbox" checked>""")
      'checkboxF - test(checkbox(false),                       """<input type="checkbox">""")

      'dangerouslySetInnerHtml - test(div(dangerouslySetInnerHtml("<span>")), "<div><span></div>")

      'optional {
        'option {
          'attr_some    - test(div(cls := "hi".some),    """<div class="hi"></div>""")
          'attr_none    - test(div(cls := "hi".none),    """<div></div>""")
          'style_some   - test(div(color := "red".some), """<div style="color:red;"></div>""")
          'style_none   - test(div(color := "red".none), """<div></div>""")
          'tagmod_some  - test(div(tagmod.some),         """<div class="ho"></div>""")
          'tagmod_none  - test(div(tagmod.none),         """<div></div>""")
          'tag_some     - test(div(reacttag.some),       """<div><span></span></div>""")
          'tag_none     - test(div(reacttag.none),       """<div></div>""")
          'element_some - test(div(relement.some),       """<div><span></span></div>""")
          'element_none - test(div(relement.none),       """<div></div>""")
          'comp_some    - test(div(H1("yoo").some),      """<div><h1>yoo</h1></div>""")
          'comp_none    - test(div(H1("yoo").none),      """<div></div>""")
        }
        'jsUndefOr {
          'attr_def      - test(div(cls := "hi".jsdef),    """<div class="hi"></div>""")
          'attr_undef    - test(div(cls := "h1".undef),    """<div></div>""")
          'style_def     - test(div(color := "red".jsdef), """<div style="color:red;"></div>""")
          'style_undef   - test(div(color := "red".undef), """<div></div>""")
          'tagmod_def    - test(div(tagmod.jsdef),         """<div class="ho"></div>""")
          'tagmod_undef  - test(div(tagmod.undef),         """<div></div>""")
          'tag_def       - test(div(reacttag.jsdef),       """<div><span></span></div>""")
          'tag_undef     - test(div(reacttag.undef),       """<div></div>""")
          'element_def   - test(div(relement.jsdef),       """<div><span></span></div>""")
          'element_undef - test(div(relement.undef),       """<div></div>""")
          'comp_def      - test(div(H1("yoo").jsdef),      """<div><h1>yoo</h1></div>""")
          'comp_undef    - test(div(H1("yoo").undef),      """<div></div>""")
        }
        'maybe {
          import ScalazReact._
          'attr_just     - test(div(cls := "hi".just),        """<div class="hi"></div>""")
          'attr_empty    - test(div(cls := "h1".maybeNot),    """<div></div>""")
          'style_just    - test(div(color := "red".just),     """<div style="color:red;"></div>""")
          'style_empty   - test(div(color := "red".maybeNot), """<div></div>""")
          'tagmod_just   - test(div(tagmod.just),             """<div class="ho"></div>""")
          'tagmod_empty  - test(div(tagmod.maybeNot),         """<div></div>""")
          'tag_just      - test(div(reacttag.just),           """<div><span></span></div>""")
          'tag_empty     - test(div(reacttag.maybeNot),       """<div></div>""")
          'element_just  - test(div(relement.just),           """<div><span></span></div>""")
          'element_empty - test(div(relement.maybeNot),       """<div></div>""")
          'comp_just     - test(div(H1("yoo").just),          """<div><h1>yoo</h1></div>""")
          'comp_empty    - test(div(H1("yoo").maybeNot),      """<div></div>""")
        }
      }

      'tagmodComposition {
        val a: TagMod = cls := "hehe"
        val b: TagMod = h3("Good")
        val c = a compose b
        test(div(c), """<div class="hehe"><h3>Good</h3></div>""")
      }

      'combination - test(
        div(cls := "hi", "Str: ", 123, JArray(H1("a"), H1("b")), p(cls := "pp")("!")),
        """<div class="hi">Str: 123<h1>a</h1><h1>b</h1><p class="pp">!</p></div>""")

      'styles - test(
        div(backgroundColor := "red", marginTop := "10px", "!"),
        """<div style="background-color:red;margin-top:10px;">!</div>""")

      'noImplicitUnit - assertTypeMismatch(compileError("""val x: TagMod = ()"""))

      'numericStyleUnits {
        'px  - test(div(marginTop := 2.px),  """<div style="margin-top:2px;"></div>""")
        'ex  - test(div(marginTop := 2.ex),  """<div style="margin-top:2ex;"></div>""")
        'em  - test(div(marginTop := 2.em),  """<div style="margin-top:2em;"></div>""")
        'str - assertContains(compileError("""div(marginTop := "hehe".em)""").msg, "not a member of String")
      }

      "?=" - test(
        span(
          true ?= (color := "red"), false ?= (color := "black"),
          true ?= (cls := "great"), false ?= (cls := "saywhat"),
          "ok"),
        """<span class="great" style="color:red;">ok</span>""")

      // Copied from Scalatags
      'copied {

        'attributeChaining - test(
          div(`class` := "thing lol", id := "cow"),
          """<div class="thing lol" id="cow"></div>""")

        'mixingAttributesStylesAndChildren - test(
          div(id := "cow", float.left, p("i am a cow")),
          """<div id="cow" style="float:left;"><p>i am a cow</p></div>""")

        //class/style after attr appends, but attr after class/style overwrites
//        'classStyleAttrOverwriting - test(
//          div(cls := "my-class", style := "background-color: red;", float.left, p("i am a cow")),
//          """<div class="my-class" style="background-color:red;float:left;"><p>i am a cow</p></div>""")

        'intSeq - test(
          div(h1("Hello"), for (i <- 0 until 5) yield i),
          """<div><h1>Hello</h1>01234</div>""")

        'stringArray - {
          val strArr = Array("hello")
          test(div(Some("lol"), Some(1), None: Option[String], h1("Hello"), Array(1, 2, 3), strArr, EmptyTag),
            """<div>lol1<h1>Hello</h1>123hello</div>""")
        }

        'applyChaining - test(
          a(tabIndex := 1, cls := "lol")(href := "boo", alt := "g"),
          """<a tabindex="1" class="lol" href="boo" alt="g"></a>""")
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
        val r = ReactComponentB[Boolean]("C").render(p => div(classSet1("mmm", "ccc" -> p))("x")).build
        r(false) shouldRender """<div class="mmm">x</div>"""
        r(true)  shouldRender """<div class="mmm ccc">x</div>"""
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

    'refs {
      class WB(t: BackendScope[String,_]) { def getName = t.props }
      val W = ReactComponentB[String]("").stateless.backend(new WB(_)).render((_,c,_,_) => div(c)).build

      // 'simple - simple refs are tested in TestTest

      'parameterised {
        val r = Ref.param[Int, TopNode](i => s"ref-$i")
        val C = ReactComponentB[Unit]("").render(_ => div(p(ref := r(1), "One"), p(ref := r(2), "Two"))).buildU
        val c = ReactTestUtils.renderIntoDocument(C())
        r(1)(c).get.getDOMNode().innerHTML mustEqual "One"
        r(2)(c).get.getDOMNode().innerHTML mustEqual "Two"
        assert(r(3)(c).isEmpty)
      }

      'onOwnedComponenets {
        val innerRef = Ref.to(W, "inner")
        val outerRef = Ref.to(W, "outer")
        val innerWName = "My name is IN"
        val outerWName = "My name is OUT"
        var tested = false
        val C = ReactComponentB[Unit]("")
          .render(P => {
            val inner = W.set(ref = innerRef)(innerWName)
            val outer = W.set(ref = outerRef)(outerWName, inner)
            div(outer)
           })
          .componentDidMount(scope => {
            innerRef(scope).get.backend.getName mustEqual innerWName
            outerRef(scope).get.backend.getName mustEqual outerWName
            tested = true
          })
          .buildU
        ReactTestUtils.renderIntoDocument(C())
        assert(tested) // just in case
      }

      'shouldNotHaveRefsOnUnmountedComponents {
        val C = ReactComponentB[Unit]("child").render((P,C) => div()).buildU
        val P = ReactComponentB[Unit]("parent")
          .render(P => C(div(ref := "test"))) // div here discarded by C.render
          .componentDidMount(scope => assert(scope.refs("test").get == null))
      }
    }

    'inference {
      import TestUtil.Inference._
      def st_get: S => T = null
      def st_set: (S, T) => S = null

      "BackendScope ops"    - test[BackendScope[Unit, S]      ](_.focusState[T](st_get)(st_set)).expect[ComponentStateFocus[T]]
      "ComponentScopeM ops" - test[ComponentScopeM[U, S, U]   ](_.focusState[T](st_get)(st_set)).expect[ComponentStateFocus[T]]
      "ReactComponentM ops" - test[ReactComponentM[U, S, U, N]](_.focusState[T](st_get)(st_set)).expect[ComponentStateFocus[T]]
    }
  }
}
