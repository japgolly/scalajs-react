package japgolly.scalajs.react.vdom

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.raw.HTMLInputElement
import scala.scalajs.js
import scala.scalajs.js.{Array => JArray}
import utest._

object PrefixedTest extends TestSuite {
  lazy val CA = ScalaComponent.build[Unit]("CA").render_C(c => <.div(c)).build
  lazy val CB = ScalaComponent.build[Unit]("CB").render_C(c => <.span(c)).build
  lazy val H1 = ScalaComponent.build[String]("H").render_P(p => <.h1(p)).build

  def jsComp = JsComponentPTest.Component(JsComponentPTest.JsProps("yo"))

  lazy val tagmod  : TagMod       = ^.cls := "ho"
  lazy val reacttag: ReactTag     = <.span
  lazy val relement: ReactElement = <.p
  lazy val jsObj   : js.Object    = js.Dynamic.literal("a" -> "b").asInstanceOf[js.Object]

  def reactNode: ReactNode = H1("cool")
  def checkbox(check: Boolean) = <.input.checkbox(^.checked := check, ^.readOnly := true)

  def test(subj: ReactElement, exp: String): Unit = {
    val comp = ScalaComponent.build[Unit]("tmp")
        .renderStatic(subj)
        .build
    assertRender(comp(), exp)
  }

  val tests = TestSuite {
    'void      - test(<.br,                                         """<br/>""")
    'short     - test(<.div(45: Short),                             """<div>45</div>""")
    'byte      - test(<.div(50: Byte),                              """<div>50</div>""")
    'int       - test(<.div(666),                                   """<div>666</div>""")
    'long      - test(<.div(123L),                                  """<div>123</div>""")
    'double    - test(<.div(12.3),                                  """<div>12.3</div>""")
    'string    - test(<.div("yo"),                                  """<div>yo</div>""")

    'reactNode - test(<.div(reactNode),                             """<div><h1>cool</h1></div>""")
    'reactTag  - test(<.div(reacttag),                              """<div><span></span></div>""")
    'reactEl   - test(<.div(relement),                              """<div><p></p></div>""")
    'tagMod    - test(<.div(tagmod),                                """<div class="ho"></div>""")
    'tagHtml   - test(<.div(<.span),                                """<div><span></span></div>""")
    'tagHtmlM  - test(<.div(<.span(^.size := 3)),                   """<div><span size="3"></span></div>""")
    'compScala - test(<.div(H1("a")),                               """<div><h1>a</h1></div>""")
    'compJS    - test(<.div(jsComp),                                """<div><div>Hello yo</div></div>""")

    'checkboxT  - test(checkbox(true),                              """<input type="checkbox" checked="" readonly=""/>""")
    'checkboxF  - test(checkbox(false),                             """<input type="checkbox" readonly=""/>""")
    'aria       - test(<.div(^.aria.label := "ow", "a"),            """<div aria-label="ow">a</div>""")
    'attrs      - test(<.div(^.rowSpan := 1, ^.colSpan := 3),       """<div rowspan="1" colspan="3"></div>""")
    'styleObj   - test(<.div(^.style := jsObj),                     """<div style="a:b;"></div>""")
    'styleDict  - test(<.div(^.style := js.Dictionary("x" -> "y")), """<div style="x:y;"></div>""")
    'styleAttrs - test(<.div(^.color := "red", ^.cursor.auto),      """<div style="color:red;cursor:auto;"></div>""")

    'ReactArray {

      'ctorRN - test(<.div(ReactArray(reactNode, reactNode)), "<div><h1>cool</h1><h1>cool</h1></div>")

      'ctorMix - test(<.div(ReactArray(reactNode, <.br, relement)), "<div><h1>cool</h1><br/><p></p></div>")

      'toReactArray {
        'seqReactNode - test(<.div(Seq     (reactNode, reactNode).toReactArray), "<div><h1>cool</h1><h1>cool</h1></div>")
        'lstReactTag  - test(<.div(List    (reacttag , reacttag ).toReactArray), "<div><span></span><span></span></div>")
        'strReactEl   - test(<.div(Stream  (relement , relement ).toReactArray), "<div><p></p><p></p></div>")
     // 'seqTagMod    - test(<.div(Seq     (tagmod   , tagmod   ).toReactArray), """<div class="ho ho"></div>""")
        'seqTagHtml   - test(<.div(Seq     (<.span   , <.span   ).toReactArray), "<div><span></span><span></span></div>")
        'seqCompScala - test(<.div(Seq     (H1("a")  , CA("b")  ).toReactArray), """<div><h1>a</h1><div>b</div></div>""")
        'seqCompJS    - test(<.div(Seq     (jsComp   , jsComp   ).toReactArray), "<div><div>Hello yo</div><div>Hello yo</div></div>")
        'vecCompMix   - test(<.div(Vector  (jsComp   , jsComp   ).toReactArray), "<div><div>Hello yo</div><div>Hello yo</div></div>")
        'arrayScala   - test(<.div(Array   (reactNode, reactNode).toReactArray), "<div><h1>cool</h1><h1>cool</h1></div>")
        'arrayJs      - test(<.div(js.Array(reactNode, reactNode).toReactArray), "<div><h1>cool</h1><h1>cool</h1></div>")
      }

      "Seq requires .toReactArray" - compileError("<.div(Seq(reactNode))")
    }

    'dangerouslySetInnerHtml - test(<.div(^.dangerouslySetInnerHtml := "<span>"), "<div><span></div>")

    'optional {
      'option {
        'attr_some    - test(<.div(^.cls :=? "hi".some),     """<div class="hi"></div>""")
        'attr_none    - test(<.div(^.cls :=? "h1".none),     """<div></div>""")
        'style_some   - test(<.div(^.color :=? "red".some),  """<div style="color:red;"></div>""")
        'style_none   - test(<.div(^.color :=? "red".none),  """<div></div>""")
        'attr_some    - test(<.div((^.color := "red").some), """<div style="color:red;"></div>""")
        'attr_none    - test(<.div((^.color := "red").none), """<div></div>""")
        'tagmod_some  - test(<.div(tagmod.some),             """<div class="ho"></div>""")
        'tagmod_none  - test(<.div(tagmod.none),             """<div></div>""")
        'tag_some     - test(<.div(reacttag.some),           """<div><span></span></div>""")
        'tag_none     - test(<.div(reacttag.none),           """<div></div>""")
        'element_some - test(<.div(relement.some),           """<div><p></p></div>""")
        'element_none - test(<.div(relement.none),           """<div></div>""")
        'comp_some    - test(<.div(H1("yoo").some),          """<div><h1>yoo</h1></div>""")
        'comp_none    - test(<.div(H1("yoo").none),          """<div></div>""")
        'text_some    - test(<.div("yoo".some),              """<div>yoo</div>""")
        'text_none    - test(<.div("yoo".none),              """<div></div>""")
      }
      'jsUndefOr {
        'attr_def      - test(<.div(^.cls :=? "hi".jsdef),     """<div class="hi"></div>""")
        'attr_undef    - test(<.div(^.cls :=? "hi".undef),     """<div></div>""")
        'style_def     - test(<.div(^.color :=? "red".jsdef),  """<div style="color:red;"></div>""")
        'style_undef   - test(<.div(^.color :=? "red".undef),  """<div></div>""")
        'attr_def      - test(<.div((^.color := "red").jsdef), """<div style="color:red;"></div>""")
        'attr_undef    - test(<.div((^.color := "red").undef), """<div></div>""")
        'tagmod_def    - test(<.div(tagmod.jsdef),             """<div class="ho"></div>""")
        'tagmod_undef  - test(<.div(tagmod.undef),             """<div></div>""")
        'tag_def       - test(<.div(reacttag.jsdef),           """<div><span></span></div>""")
        'tag_undef     - test(<.div(reacttag.undef),           """<div></div>""")
        'element_def   - test(<.div(relement.jsdef),           """<div><p></p></div>""")
        'element_undef - test(<.div(relement.undef),           """<div></div>""")
        'comp_def      - test(<.div(H1("yoo").jsdef),          """<div><h1>yoo</h1></div>""")
        'comp_undef    - test(<.div(H1("yoo").undef),          """<div></div>""")
        'text_def      - test(<.div("yoo".jsdef),              """<div>yoo</div>""")
        'text_undef    - test(<.div("yoo".undef),              """<div></div>""")
      }
//      'maybe {
//        import ScalazReact._
//        'attr_just     - test(<.div(^.cls :=? "hi".just),        """<div class="hi"></div>""")
//        'attr_empty    - test(<.div(^.cls :=? "h1".maybeNot),    """<div></div>""")
//        'style_just    - test(<.div(^.color :=? "red".just),     """<div style="color:red;"></div>""")
//        'style_empty   - test(<.div(^.color :=? "red".maybeNot), """<div></div>""")
//        'tagmod_just   - test(<.div(tagmod.just),                """<div class="ho"></div>""")
//        'tagmod_empty  - test(<.div(tagmod.maybeNot),            """<div></div>""")
//        'tag_just      - test(<.div(reacttag.just),              """<div><span></span></div>""")
//        'tag_empty     - test(<.div(reacttag.maybeNot),          """<div></div>""")
//        'element_just  - test(<.div(relement.just),              """<div><span></span></div>""")
//        'element_empty - test(<.div(relement.maybeNot),          """<div></div>""")
//        'comp_just     - test(<.div(H1("yoo").just),             """<div><h1>yoo</h1></div>""")
//        'comp_empty    - test(<.div(H1("yoo").maybeNot),         """<div></div>""")
//      }
      'when {
        'tags - test(
          <.span(<.span("1").when(true), <.span("2").when(false)),
          """<span><span>1</span></span>""")
        'attrs - test(
          <.span((^.cls := "great").when(true), (^.cls := "saywhat").when(false), "ok"),
          """<span class="great">ok</span>""")
        'styles - test(
          <.span((^.color := "red").when(true), (^.color := "black").when(false), "ok"),
          """<span style="color:red;">ok</span>""")
      }
      'unless {
        'tags - test(
          <.span(<.span("1").unless(true), <.span("2").unless(false)),
          """<span><span>2</span></span>""")
        'attrs - test(
          <.span((^.cls := "great").unless(false), (^.cls := "saywhat").unless(true), "ok"),
          """<span class="great">ok</span>""")
        'styles - test(
          <.span((^.color := "red").unless(false), (^.color := "black").unless(true), "ok"),
          """<span style="color:red;">ok</span>""")
      }
    }

    'tagmodComposition {
      val a: TagMod = ^.cls := "hehe"
      val b: TagMod = <.h3("Good")
      val c = a(b)
      test(<.div(c), """<div class="hehe"><h3>Good</h3></div>""")
    }

//    'combination - test(
//      <.div(^.cls := "hi", "Str: ", 123, JArray(H1("a"), H1("b")), <.p(^.cls := "pp")("!")),
//      """<div class="hi">Str: 123<h1>a</h1><h1>b</h1><p class="pp">!</p></div>""")

    'styles - test(
      <.div(^.backgroundColor := "red", ^.marginTop := "10px", "!"),
      """<div style="background-color:red;margin-top:10px;">!</div>""")

    'noImplicitUnit - assertTypeMismatch(compileError("""val x: TagMod = ()"""))

    'numericStyleUnits {
    //'zero - test(<.div(^.marginTop := 0.em),    """<div style="margin-top:0;"></div>""")
      'px   - test(<.div(^.marginTop := 2.px),    """<div style="margin-top:2px;"></div>""")
    //'ex   - test(<.div(^.marginTop := 2.3f.ex), """<div style="margin-top:2.3ex;"></div>""")
      'em   - test(<.div(^.marginTop := 2.7.em),  """<div style="margin-top:2.7em;"></div>""")
      'rem  - test(<.div(^.marginTop := 2L.rem),  """<div style="margin-top:2rem;"></div>""")
      'str  - assertContains(compileError("""<.div(^.marginTop := "hehe".em)""").msg, "not a member of String")
    }

    'fromScalatags {
      'attributeChaining - test(
        <.div(^.`class` := "thing lol", ^.id := "cow"),
        """<div id="cow" class="thing lol"></div>""")

      'mixingAttributesStylesAndChildren - test(
        <.div(^.id := "cow", ^.float.left, <.p("i am a cow")),
        """<div id="cow" style="float:left;"><p>i am a cow</p></div>""")

      'applyChaining - test(
        <.a(^.tabIndex := 1, ^.cls := "lol")(^.href := "boo", ^.alt := "g"),
        """<a tabindex="1" href="boo" alt="g" class="lol"></a>""")
    }

    'classSet {
      'allConditional {
        val r = ScalaComponent.build[(Boolean,Boolean)]("C").render_P(p =>
          <.div(^.classSet("p1" -> p._1, "p2" -> p._2))("x")).build
        assertRender(r((false, false)), """<div>x</div>""")
        assertRender(r((true,  false)), """<div class="p1">x</div>""")
        assertRender(r((false, true)) , """<div class="p2">x</div>""")
        assertRender(r((true,  true)) , """<div class="p1 p2">x</div>""")
      }
      'hasMandatory {
        val r = ScalaComponent.build[Boolean]("C").render_P(p =>
          <.div(^.classSet1("mmm", "ccc" -> p))("x")).build
        assertRender(r(false), """<div class="mmm">x</div>""")
        assertRender(r(true) , """<div class="mmm ccc">x</div>""")
      }
      'appends {
        val r = ScalaComponent.build[Boolean]("C").render_P(p =>
          <.div(^.cls := "neat", ^.classSet1("mmm", "ccc" -> p), ^.cls := "slowclap", "x")).build
        assertRender(r(false), """<div class="neat mmm slowclap">x</div>""")
        assertRender(r(true) , """<div class="neat mmm ccc slowclap">x</div>""")
      }
    }

    'key {
      def t(m: TagMod) = test(<.span(m, "1"), "<span>1</span>")
      val obj = new AnyRef
      'str  - t(^.key := "1")
      'bool - t(^.key := false)
      'int  - t(^.key := 3)
      'long - t(^.key := 3L)
      'shrt - t(^.key := 3.toShort)
      'flt  - t(^.key := 3.4f)
      'dbl  - t(^.key := 3.4)
      'byte - t(^.key := 3.toByte)
      'unit - compileError(" ^.key := (())  ")
      'obj  - compileError(" ^.key := obj   ")
      'key  - compileError(" ^.key := ^.key ")
    }
  }
}
