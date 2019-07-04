package japgolly.scalajs.react.core.vdom

import japgolly.scalajs.react._
import japgolly.scalajs.react.core.JsComponentEs6PTest
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import scala.scalajs.js
import utest._

object PrefixedTest extends TestSuite {
  lazy val CA = ScalaComponent.builder[Unit]("CA").render_C(c => <.div(c)).build
  lazy val CB = ScalaComponent.builder[Unit]("CB").render_C(c => <.span(c)).build
  lazy val H1 = ScalaComponent.builder[String]("H").render_P(p => <.h1(p)).build

  def jsComp = JsComponentEs6PTest.Component(JsComponentEs6PTest.JsProps("yo"))

  lazy val tagMod     : TagMod      = ^.cls := "ho"
  lazy val vdomNode   : VdomNode    = H1("cool")
  lazy val vdomTag    : VdomTag     = <.span
  lazy val vdomElement: VdomElement = <.p
  lazy val jsObject   : js.Object   = js.Dynamic.literal("a" -> "b").asInstanceOf[js.Object]

  def checkbox(check: Boolean) = <.input.checkbox(^.checked := check, ^.readOnly := true)

  def test(subj: VdomNode, exp: String): Unit = {
    val comp = ScalaComponent.static("tmp")(subj)
    assertRender(comp(), exp)
  }

  val tests = Tests {
    "void" - test(<.br, """<br/>""")

    'inner {
      "byte"      - test(<.div(50: Byte),            """<div>50</div>""")
      "short"     - test(<.div(45: Short),           """<div>45</div>""")
      "int"       - test(<.div(666),                 """<div>666</div>""")
      "long"      - test(<.div(123L),                """<div>123</div>""")
      "double"    - test(<.div(12.3),                """<div>12.3</div>""")
      "string"    - test(<.div("yo"),                """<div>yo</div>""")
      "vdomNode"  - test(<.div(vdomNode),            """<div><h1>cool</h1></div>""")
      "vdomTag"   - test(<.div(vdomTag),             """<div><span></span></div>""")
      "vdomEl"    - test(<.div(vdomElement),         """<div><p></p></div>""")
      "tagMod"    - test(<.div(tagMod),              """<div class="ho"></div>""")
      "tagHtml"   - test(<.div(<.span),              """<div><span></span></div>""")
      "tagHtmlM"  - test(<.div(<.span(^.size := 3)), """<div><span size="3"></span></div>""")
      "compScala" - test(<.div(H1("a")),             """<div><h1>a</h1></div>""")
      "compJS"    - test(<.div(jsComp),              """<div><div>Hello yo</div></div>""")
    }

    "checkboxT"  - test(checkbox(true),  """<input type="checkbox" checked="" readonly=""/>""")
    "checkboxF"  - test(checkbox(false), """<input type="checkbox" readonly=""/>""")

     "attr" - {
      "aria"       - test(<.div(^.aria.label := "ow", "a"),            """<div aria-label="ow">a</div>""")
      "attrs"      - test(<.div(^.rowSpan := 1, ^.colSpan := 3),       """<div rowspan="1" colSpan="3"></div>""")
      "styleObj"   - test(<.div(^.style := jsObject),                  """<div style="a:b"></div>""")
      "styleDict"  - test(<.div(^.style := js.Dictionary("x" -> "y")), """<div style="x:y"></div>""")
      "styleAttrs" - test(<.div(^.color := "red", ^.cursor.auto),      """<div style="color:red;cursor:auto"></div>""")

      "any" - {
        def aa: VdomAttr[Any] = ^.profile
        def as = "profile"
        "short"     - test(<.div(aa := (45: Short)), s"""<div $as="45"></div>""")
        "byte"      - test(<.div(aa := (50: Byte)),  s"""<div $as="50"></div>""")
        "int"       - test(<.div(aa := 666),         s"""<div $as="666"></div>""")
        "long"      - test(<.div(aa := 123L),        s"""<div $as="123"></div>""")
        "float"     - test(<.div(aa := 321f),        s"""<div $as="321"></div>""")
        "double"    - test(<.div(aa := 12.3),        s"""<div $as="12.3"></div>""")
        "string"    - test(<.div(aa := "yo"),        s"""<div $as="yo"></div>""")
//        "booleanT"  - test(<.div(aa := true),        s"""<div $as="true"></div>""")
//        "booleanF"  - test(<.div(aa := false),       s"""<div $as="false"></div>""")
      }
      "boolean" - {
        "t" - test(<.div(^.disabled := true), """<div disabled=""></div>""")
        "f" - test(<.div(^.disabled := false), """<div></div>""")
      }
    }

    "VdomArray" - {
      "ctorRN" - test(<.div(VdomArray(vdomNode, vdomNode)), "<div><h1>cool</h1><h1>cool</h1></div>")
      "ctorMix" - test(<.div(VdomArray(vdomNode, <.br, vdomElement)), "<div><h1>cool</h1><br/><p></p></div>")
      "toVdomArray" - {
        "seqVdomNode"  - test(<.div(Seq     (vdomNode   , vdomNode    ).toVdomArray), "<div><h1>cool</h1><h1>cool</h1></div>")
        "lstVdomTag"   - test(<.div(List    (vdomTag    , vdomTag     ).toVdomArray), "<div><span></span><span></span></div>")
        "strVdomEl"    - test(<.div(Stream  (vdomElement, vdomElement ).toVdomArray), "<div><p></p><p></p></div>")
     // "seqTagMod"    - test(<.div(Seq     (tagMod     , tagMod      ).toVdomArray), """<div class="ho ho"></div>""")
        "seqTagHtml"   - test(<.div(Seq     (<.span     , <.span      ).toVdomArray), "<div><span></span><span></span></div>")
        "seqCompScala" - test(<.div(Seq     (H1("a")    , CA("b")     ).toVdomArray), """<div><h1>a</h1><div>b</div></div>""")
        "seqCompJS"    - test(<.div(Seq     (jsComp     , jsComp      ).toVdomArray), "<div><div>Hello yo</div><div>Hello yo</div></div>")
        "vecCompMix"   - test(<.div(Vector  (jsComp     , jsComp      ).toVdomArray), "<div><div>Hello yo</div><div>Hello yo</div></div>")
        "arrayScala"   - test(<.div(Array   (vdomNode   , vdomNode    ).toVdomArray), "<div><h1>cool</h1><h1>cool</h1></div>")
        "arrayJs"      - test(<.div(js.Array(vdomNode   , vdomNode    ).toVdomArray), "<div><h1>cool</h1><h1>cool</h1></div>")
      }
    }

    "Seq" - {
      "without expansion" - compileError("<.div(Seq(reactNode))")

      "seqReactNode"    - test(<.div(Seq     (vdomNode   , vdomNode    ).toTagMod), "<div><h1>cool</h1><h1>cool</h1></div>")
      "lstReactTag"     - test(<.div(List    (vdomTag    , vdomTag     ).toTagMod), "<div><span></span><span></span></div>")
      "strReactEl"      - test(<.div(Stream  (vdomElement, vdomElement ).toTagMod), "<div><p></p><p></p></div>")
      "seqTagMod"       - test(<.div(Seq     (tagMod     , tagMod      ).toTagMod), """<div class="ho ho"></div>""")
      "x_seqTagHtml"    - test(<.div(Seq     (<.span     , <.span      ).toTagMod), "<div><span></span><span></span></div>")
      "x_seqCompScala1" - test(<.div(Seq     (H1("a")    , H1("b")     ).toTagMod), """<div><h1>a</h1><h1>b</h1></div>""")
      "x_seqCompScala2" - test(<.div(Seq     (H1("a")    , CA("b")     ).toTagMod), """<div><h1>a</h1><div>b</div></div>""")
      "x_seqCompJS"     - test(<.div(Seq     (jsComp     , jsComp      ).toTagMod), "<div><div>Hello yo</div><div>Hello yo</div></div>")
      "x_vecCompMix"    - test(<.div(Vector  (jsComp     , jsComp      ).toTagMod), "<div><div>Hello yo</div><div>Hello yo</div></div>")
      "arrayScala"      - test(<.div(Array   (vdomNode   , vdomNode    ).toTagMod), "<div><h1>cool</h1><h1>cool</h1></div>")
      "arrayJs"         - test(<.div(js.Array(vdomNode   , vdomNode    ).toTagMod), "<div><h1>cool</h1><h1>cool</h1></div>")

      "mix" - test(<.div(TagMod(vdomNode, <.br, vdomElement)), "<div><h1>cool</h1><br/><p></p></div>")

      "mkTagMod" - {
        "sep0" - test(<.div(List[TagMod]()       .mkTagMod(" | "))              , "<div></div>")
        "sep1" - test(<.div(List(<.p)            .mkTagMod(" | "))              , "<div><p></p></div>")
        "sep2" - test(<.div(List(<.p, <.br)      .mkTagMod(" | "))              , "<div><p></p> | <br/></div>")
        "sep3" - test(<.div(List(<.p, <.br, <.hr).mkTagMod(" | "))              , "<div><p></p> | <br/> | <hr/></div>")
        "sep4" - test(<.div((1 to 4)             .mkTagMod(<.br) )              , "<div>1<br/>2<br/>3<br/>4</div>")
        "all"  - test(<.div(List(<.p, <.br, <.hr).mkTagMod(" [ ", " | ", " ] ")), "<div> [ <p></p> | <br/> | <hr/> ] </div>")
      }
      "mkReactFragment" - {
        "sep0" - test(List[VdomNode]()     .mkReactFragment(" | ")              , "")
        "sep1" - test(List(<.p)            .mkReactFragment(" | ")              , "<p></p>")
        "sep2" - test(List(<.p, <.br)      .mkReactFragment(" | ")              , "<p></p> | <br/>")
        "sep3" - test(List(<.p, <.br, <.hr).mkReactFragment(" | ")              , "<p></p> | <br/> | <hr/>")
        "sep4" - test((1 to 4)             .mkReactFragment(<.br)               , "1<br/>2<br/>3<br/>4")
        "all"  - test(List(<.p, <.br, <.hr).mkReactFragment(" [ ", " | ", " ] "), " [ <p></p> | <br/> | <hr/> ] ")
      }
    }

    "dangerouslySetInnerHtml" - test(<.div(^.dangerouslySetInnerHtml := "<span>"), "<div><span></div>")

    "optional" - {
      "option" - {
        def some[A](a: A): Option[A] = Some(a)
        def none[A](a: A): Option[A] = None
        "attr_some"    - test(<.div(^.cls   :=? some("hi")       ), """<div class="hi"></div>""")
        "attr_none"    - test(<.div(^.cls   :=? none("h1")       ), """<div></div>""")
        "style_some"   - test(<.div(^.color :=? some("red")      ), """<div style="color:red"></div>""")
        "style_none"   - test(<.div(^.color :=? none("red")      ), """<div></div>""")
        "tagMod_some"  - test(<.div(some(tagMod     ).whenDefined), """<div class="ho"></div>""")
        "tagMod_none"  - test(<.div(none(tagMod     ).whenDefined), """<div></div>""")
        "tag_some"     - test(<.div(some(vdomTag    ).whenDefined), """<div><span></span></div>""")
        "tag_none"     - test(<.div(none(vdomTag    ).whenDefined), """<div></div>""")
        "element_some" - test(<.div(some(vdomElement).whenDefined), """<div><p></p></div>""")
        "element_none" - test(<.div(none(vdomElement).whenDefined), """<div></div>""")
        "comp_some"    - test(<.div(some(H1("yoo")  ).whenDefined), """<div><h1>yoo</h1></div>""")
        "comp_none"    - test(<.div(none(H1("yoo")  ).whenDefined), """<div></div>""")
        "text_some"    - test(<.div(some("yoo"      ).whenDefined), """<div>yoo</div>""")
        "text_none"    - test(<.div(none("yoo"      ).whenDefined), """<div></div>""")
      }
      "jsUndefOr" - {
        def some[A](a: A): js.UndefOr[A] = a
        def none[A](a: A): js.UndefOr[A] = js.undefined
        "attr_some"    - test(<.div(^.cls   :=? some("hi")       ), """<div class="hi"></div>""")
        "attr_none"    - test(<.div(^.cls   :=? none("h1")       ), """<div></div>""")
        "style_some"   - test(<.div(^.color :=? some("red")      ), """<div style="color:red"></div>""")
        "style_none"   - test(<.div(^.color :=? none("red")      ), """<div></div>""")
        "tagMod_some"  - test(<.div(some(tagMod     ).whenDefined), """<div class="ho"></div>""")
        "tagMod_none"  - test(<.div(none(tagMod     ).whenDefined), """<div></div>""")
        "tag_some"     - test(<.div(some(vdomTag    ).whenDefined), """<div><span></span></div>""")
        "tag_none"     - test(<.div(none(vdomTag    ).whenDefined), """<div></div>""")
        "element_some" - test(<.div(some(vdomElement).whenDefined), """<div><p></p></div>""")
        "element_none" - test(<.div(none(vdomElement).whenDefined), """<div></div>""")
        "comp_some"    - test(<.div(some(H1("yoo")  ).whenDefined), """<div><h1>yoo</h1></div>""")
        "comp_none"    - test(<.div(none(H1("yoo")  ).whenDefined), """<div></div>""")
        "text_some"    - test(<.div(some("yoo"      ).whenDefined), """<div>yoo</div>""")
        "text_none"    - test(<.div(none("yoo"      ).whenDefined), """<div></div>""")
      }
      "maybe" - {
        import ScalazReact._
        import scalaz.Maybe
        def some[A](a: A): Maybe[A] = Maybe.Just(a)
        def none[A](a: A): Maybe[A] = Maybe.empty
        "attr_some"    - test(<.div(^.cls   :=? some("hi")       ), """<div class="hi"></div>""")
        "attr_none"    - test(<.div(^.cls   :=? none("h1")       ), """<div></div>""")
        "style_some"   - test(<.div(^.color :=? some("red")      ), """<div style="color:red"></div>""")
        "style_none"   - test(<.div(^.color :=? none("red")      ), """<div></div>""")
        "tagMod_some"  - test(<.div(some(tagMod     ).whenDefined), """<div class="ho"></div>""")
        "tagMod_none"  - test(<.div(none(tagMod     ).whenDefined), """<div></div>""")
        "tag_some"     - test(<.div(some(vdomTag    ).whenDefined), """<div><span></span></div>""")
        "tag_none"     - test(<.div(none(vdomTag    ).whenDefined), """<div></div>""")
        "element_some" - test(<.div(some(vdomElement).whenDefined), """<div><p></p></div>""")
        "element_none" - test(<.div(none(vdomElement).whenDefined), """<div></div>""")
        "comp_some"    - test(<.div(some(H1("yoo")  ).whenDefined), """<div><h1>yoo</h1></div>""")
        "comp_none"    - test(<.div(none(H1("yoo")  ).whenDefined), """<div></div>""")
        "text_some"    - test(<.div(some("yoo"      ).whenDefined), """<div>yoo</div>""")
        "text_none"    - test(<.div(none("yoo"      ).whenDefined), """<div></div>""")
      }
      "when" - {
        "tags" - test(
          <.span(<.span("1").when(true), <.span("2").when(false)),
          """<span><span>1</span></span>""")
        "attrs" - test(
          <.span((^.cls := "great").when(true), (^.cls := "saywhat").when(false), "ok"),
          """<span class="great">ok</span>""")
        "styles" - test(
          <.span((^.color := "red").when(true), (^.color := "black").when(false), "ok"),
          """<span style="color:red">ok</span>""")
      }
      "unless" - {
        "tags" - test(
          <.span(<.span("1").unless(true), <.span("2").unless(false)),
          """<span><span>2</span></span>""")
        "attrs" - test(
          <.span((^.cls := "great").unless(false), (^.cls := "saywhat").unless(true), "ok"),
          """<span class="great">ok</span>""")
        "styles" - test(
          <.span((^.color := "red").unless(false), (^.color := "black").unless(true), "ok"),
          """<span style="color:red">ok</span>""")
      }
    }

    "tagModComposition" - {
      val a: TagMod = ^.cls := "hehe"
      val b: TagMod = <.h3("Good")
      compileError("a(b)")
      val c = TagMod(a, b)
      test(<.div(c), """<div class="hehe"><h3>Good</h3></div>""")
    }

//    "combination" - test(
//      <.div(^.cls := "hi", "Str: ", 123, JArray(H1("a"), H1("b")), <.p(^.cls := "pp")("!")),
//      """<div class="hi">Str: 123<h1>a</h1><h1>b</h1><p class="pp">!</p></div>""")

    "styles" - {
      "named" - test(
        <.div(^.backgroundColor := "red", ^.marginTop := "10px", "!"),
        """<div style="background-color:red;margin-top:10px">!</div>""")

      "direct" - test(
        <.div(^.style := js.Dictionary("color" -> "black", "margin-left" -> "1em"), "!"),
        """<div style="color:black;margin-left:1em">!</div>""")

      "namedAndDirect" - test(
        <.div(^.backgroundColor := "red", ^.style := js.Dictionary("color" -> "black", "margin-left" -> "1em"), "!"),
        """<div style="background-color:red;color:black;margin-left:1em">!</div>""")

      "directAndNamed" - test(
        <.div(^.style := js.Dictionary("color" -> "black", "margin-left" -> "1em"), ^.backgroundColor := "red", "!"),
        """<div style="color:black;margin-left:1em;background-color:red">!</div>""")
    }

    "noImplicitUnit" - assertTypeMismatch(compileError("""val x: TagMod = ()"""))

    "numericStyleUnits" - {
    //"zero" - test(<.div(^.marginTop := 0.em),    """<div style="margin-top:0"></div>""")
      "px"   - test(<.div(^.marginTop := 2.px),    """<div style="margin-top:2px"></div>""")
    //"ex"   - test(<.div(^.marginTop := 2.3f.ex), """<div style="margin-top:2.3ex"></div>""")
      "em"   - test(<.div(^.marginTop := 2.7.em),  """<div style="margin-top:2.7em"></div>""")
      "rem"  - test(<.div(^.marginTop := 2L.rem),  """<div style="margin-top:2rem"></div>""")
      "str"  - assertContains(compileError("""<.div(^.marginTop := "hehe".em)""").msg, "not a member of String")
    }

    "fromScalatags" - {
      "attributeChaining" - test(
        <.div(^.`class` := "thing lol", ^.id := "cow"),
        """<div id="cow" class="thing lol"></div>""")

      "mixingAttributesStylesAndChildren" - test(
        <.div(^.id := "cow", ^.float.left, <.p("i am a cow")),
        """<div id="cow" style="float:left"><p>i am a cow</p></div>""")

      "applyChaining" - test(
        <.a(^.tabIndex := 1, ^.cls := "lol")(^.href := "boo", ^.alt := "g"),
        """<a tabindex="1" href="boo" alt="g" class="lol"></a>""")
    }

    "classSet" - {
      "allConditional" - {
        val r = ScalaComponent.builder[(Boolean,Boolean)]("C").render_P(p =>
          <.div(^.classSet("p1" -> p._1, "p2" -> p._2))("x")).build
        assertRender(r((false, false)), """<div>x</div>""")
        assertRender(r((true,  false)), """<div class="p1">x</div>""")
        assertRender(r((false, true)) , """<div class="p2">x</div>""")
        assertRender(r((true,  true)) , """<div class="p1 p2">x</div>""")
      }
      "hasMandatory" - {
        val r = ScalaComponent.builder[Boolean]("C").render_P(p =>
          <.div(^.classSet1("mmm", "ccc" -> p))("x")).build
        assertRender(r(false), """<div class="mmm">x</div>""")
        assertRender(r(true) , """<div class="mmm ccc">x</div>""")
      }
      "appends" - {
        val r = ScalaComponent.builder[Boolean]("C").render_P(p =>
          <.div(^.cls := "neat", ^.classSet1("mmm", "ccc" -> p), ^.cls := "slowclap", "x")).build
        assertRender(r(false), """<div class="neat mmm slowclap">x</div>""")
        assertRender(r(true) , """<div class="neat mmm ccc slowclap">x</div>""")
      }
    }

    "key" - {
      def t(m: TagMod) = test(<.span(m, "1"), "<span>1</span>")
      val anyref = new AnyRef
      val jsObject = js.Object()
      "string"   - t(^.key := "1")
      "byte"     - t(^.key := 3.toByte)
      "short"    - t(^.key := 3.toShort)
      "int"      - t(^.key := 3)
      "long"     - t(^.key := 3L)
      "float"    - t(^.key := 3.4f)
      "double"   - t(^.key := 3.4)
      "unit"     - compileError(" ^.key := (())     ")
      "boolean"  - compileError(" ^.key := false    ")
      "anyref"   - compileError(" ^.key := anyref   ")
      "jsObject" - compileError(" ^.key := jsObject ")
      "key"      - compileError(" ^.key := ^.key      ")
    }

    "anchorToNewWindow" - {
      test(<.a.toNewWindow("/ok")("OK!"), """<a target="_blank" href="/ok" rel="noopener">OK!</a>""")
    }
  }
}
