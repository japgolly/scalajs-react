package japgolly.scalajs.react

import scala.scalajs.js
import scala.scalajs.js.{Array => JArray}
import org.scalajs.dom.raw.HTMLInputElement
import utest._
import japgolly.scalajs.react.TestUtil._
import japgolly.scalajs.react.vdom.prefix_<^._

object PrefixedVdomTest extends TestSuite {

  lazy val CA = ReactComponentB[Unit]("CA").render((_,c) => <.div(c)).buildU
  lazy val CB = ReactComponentB[Unit]("CB").render((_,c) => <.span(c)).buildU
  lazy val H1 = ReactComponentB[String]("H").render(p => <.h1(p)).build

  lazy val SI = ReactComponentB[Unit]("SI")
    .initialState(123)
    .render(T => <.input(^.value := T.state.toString))
    .domType[HTMLInputElement]
    .buildU

  val tagmod  : TagMod       = ^.cls := "ho"
  val reacttag: ReactTag     = <.span()
  val relement: ReactElement = <.span()

  val tests = TestSuite {

    'altScalatags {
      def test(subj: ReactElement, exp: String): Unit =
        ReactComponentB[Unit]("tmp").render((_,_) => subj).buildU.apply() shouldRender exp
      def reactNode: ReactNode = H1("cool")
      def checkbox(check: Boolean) = <.input(^.`type` := "checkbox", ^.checked := check)

      'short     - test(<.div(45: Short),                        "<div>45</div>")
      'byte      - test(<.div(50: Byte),                         "<div>50</div>")
      'int       - test(<.div(666),                              "<div>666</div>")
      'long      - test(<.div(123L),                             "<div>123</div>")
      'double    - test(<.div(12.3),                             "<div>12.3</div>")
      'string    - test(<.div("yo"),                             "<div>yo</div>")
      'reactNode - test(<.div(reactNode),                        "<div><h1>cool</h1></div>")
      'comp      - test(<.div(H1("a")),                          "<div><h1>a</h1></div>")
      'seqTag    - test(<.div(Seq (<.span(1), <.span(2))),       "<div><span>1</span><span>2</span></div>")
      'listTag   - test(<.div(List(<.span(1), <.span(2))),       "<div><span>1</span><span>2</span></div>")
      'listComp  - test(<.div(List(H1("a"), H1("b"))),           "<div><h1>a</h1><h1>b</h1></div>")
      'list2jAry - test(<.div(List(H1("a"), H1("b")).toJsArray), "<div><h1>a</h1><h1>b</h1></div>")
      'jAryTag   - test(<.div(JArray(<.span(1), <.span(2))),     "<div><span>1</span><span>2</span></div>")
      'jAryComp  - test(<.div(JArray(H1("a"), H1("b"))),         "<div><h1>a</h1><h1>b</h1></div>")
      'checkboxT - test(checkbox(true),                        """<input type="checkbox" checked>""")
      'checkboxF - test(checkbox(false),                       """<input type="checkbox">""")
      'aria      - test(<.div(^.aria.label := "ow", "a"),      """<div aria-label="ow">a</div>""")

      'dangerouslySetInnerHtml - test(<.div(^.dangerouslySetInnerHtml("<span>")), "<div><span></div>")

      'optional {
        'option {
          'attr_some    - test(<.div(^.cls := "hi".some),    """<div class="hi"></div>""")
          'attr_none    - test(<.div(^.cls := "h1".none),    """<div></div>""")
          'style_some   - test(<.div(^.color := "red".some), """<div style="color:red;"></div>""")
          'style_none   - test(<.div(^.color := "red".none), """<div></div>""")
          'tagmod_some  - test(<.div(tagmod.some),           """<div class="ho"></div>""")
          'tagmod_none  - test(<.div(tagmod.none),           """<div></div>""")
          'tag_some     - test(<.div(reacttag.some),         """<div><span></span></div>""")
          'tag_none     - test(<.div(reacttag.none),         """<div></div>""")
          'element_some - test(<.div(relement.some),         """<div><span></span></div>""")
          'element_none - test(<.div(relement.none),         """<div></div>""")
          'comp_some    - test(<.div(H1("yoo").some),        """<div><h1>yoo</h1></div>""")
          'comp_none    - test(<.div(H1("yoo").none),        """<div></div>""")
        }
        'jsUndefOr {
          'attr_def      - test(<.div(^.cls := "hi".jsdef),    """<div class="hi"></div>""")
          'attr_undef    - test(<.div(^.cls := "hi".undef),    """<div></div>""")
          'style_def     - test(<.div(^.color := "red".jsdef), """<div style="color:red;"></div>""")
          'style_undef   - test(<.div(^.color := "red".undef), """<div></div>""")
          'tagmod_def    - test(<.div(tagmod.jsdef),           """<div class="ho"></div>""")
          'tagmod_undef  - test(<.div(tagmod.undef),           """<div></div>""")
          'tag_def       - test(<.div(reacttag.jsdef),         """<div><span></span></div>""")
          'tag_undef     - test(<.div(reacttag.undef),         """<div></div>""")
          'element_def   - test(<.div(relement.jsdef),         """<div><span></span></div>""")
          'element_undef - test(<.div(relement.undef),         """<div></div>""")
          'comp_def      - test(<.div(H1("yoo").jsdef),        """<div><h1>yoo</h1></div>""")
          'comp_undef    - test(<.div(H1("yoo").undef),        """<div></div>""")
        }
        'maybe {
          import ScalazReact._
          'attr_just     - test(<.div(^.cls := "hi".just),        """<div class="hi"></div>""")
          'attr_empty    - test(<.div(^.cls := "h1".maybeNot),    """<div></div>""")
          'style_just    - test(<.div(^.color := "red".just),     """<div style="color:red;"></div>""")
          'style_empty   - test(<.div(^.color := "red".maybeNot), """<div></div>""")
          'tagmod_just   - test(<.div(tagmod.just),               """<div class="ho"></div>""")
          'tagmod_empty  - test(<.div(tagmod.maybeNot),           """<div></div>""")
          'tag_just      - test(<.div(reacttag.just),             """<div><span></span></div>""")
          'tag_empty     - test(<.div(reacttag.maybeNot),         """<div></div>""")
          'element_just  - test(<.div(relement.just),             """<div><span></span></div>""")
          'element_empty - test(<.div(relement.maybeNot),         """<div></div>""")
          'comp_just     - test(<.div(H1("yoo").just),            """<div><h1>yoo</h1></div>""")
          'comp_empty    - test(<.div(H1("yoo").maybeNot),        """<div></div>""")
        }
      }

      'tagmodComposition {
        val a: TagMod = ^.cls := "hehe"
        val b: TagMod = <.h3("Good")
        val c = a compose b
        test(<.div(c), """<div class="hehe"><h3>Good</h3></div>""")
      }

      'combination - test(
        <.div(^.cls := "hi", "Str: ", 123, JArray(H1("a"), H1("b")), <.p(^.cls := "pp")("!")),
        """<div class="hi">Str: 123<h1>a</h1><h1>b</h1><p class="pp">!</p></div>""")

      'styles - test(
        <.div(^.backgroundColor := "red", ^.marginTop := "10px", "!"),
        """<div style="background-color:red;margin-top:10px;">!</div>""")

      'noImplicitUnit - assertTypeMismatch(compileError("""val x: TagMod = ()"""))

      'numericStyleUnits {
        'px  - test(<.div(^.marginTop := 2.px),  """<div style="margin-top:2px;"></div>""")
        'ex  - test(<.div(^.marginTop := 2.ex),  """<div style="margin-top:2ex;"></div>""")
        'em  - test(<.div(^.marginTop := 2.em),  """<div style="margin-top:2em;"></div>""")
        'str - assertContains(compileError("""<.div(^.marginTop := "hehe".em)""").msg, "not a member of String")
      }

      "?=" - test(
        <.span(
          true ?= (^.color := "red"), false ?= (^.color := "black"),
          true ?= (^.cls := "great"), false ?= (^.cls := "saywhat"),
          "ok"),
        """<span class="great" style="color:red;">ok</span>""")

      // Copied from Scalatags
      'copied {

        'attributeChaining - test(
          <.div(^.`class` := "thing lol", ^.id := "cow"),
          """<div id="cow" class="thing lol"></div>""")

        'mixingAttributesStylesAndChildren - test(
          <.div(^.id := "cow", ^.float.left, <.p("i am a cow")),
          """<div id="cow" style="float:left;"><p>i am a cow</p></div>""")

        //class/style after attr appends, but attr after class/style overwrites
        //        'classStyleAttrOverwriting - test(
        //          div(cls := "my-class", style := "background-color: red;", float.left, p("i am a cow")),
        //          """<div class="my-class" style="background-color:red;float:left;"><p>i am a cow</p></div>""")

        'intSeq - test(
          <.div(<.h1("Hello"), for (i <- 0 until 5) yield i),
          """<div><h1>Hello</h1>01234</div>""")

        'stringArray - {
          val strArr = Array("hello")
          test(<.div("lol".some, 1.some, None: Option[String], <.h1("Hello"), Array(1, 2, 3), strArr, EmptyTag),
            """<div>lol1<h1>Hello</h1>123hello</div>""")
        }

        'applyChaining - test(
          <.a(^.tabIndex := 1, ^.cls := "lol")(^.href := "boo", ^.alt := "g"),
          """<a tabindex="1" href="boo" alt="g" class="lol"></a>""")
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
      'appends {
        val r = ReactComponentB[Boolean]("C").render(p =>
          <.div(^.cls := "neat", ^.classSet1("mmm", "ccc" -> p), ^.cls := "slowclap", "x")).build
        r(false) shouldRender """<div class="neat mmm slowclap">x</div>"""
        r(true)  shouldRender """<div class="neat mmm ccc slowclap">x</div>"""
      }
    }
  }
}
