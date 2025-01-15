package japgolly.scalajs.react.core.vdom

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html
import scala.annotation.nowarn
import utest._

object VdomTest extends AsyncTestSuite {

  val C = ScalaComponent.static("")(<.br)
  val Span = ScalaComponent.builder[Unit]("").render_C(<.span(_)).build

  override def tests = Tests {

    "returnTypes" - {
      def test(subj: VdomNode, exp: String): Unit = {
        val comp = ScalaComponent.static("tmp")(subj)
        assertRender(comp(), exp)
      }

      "byte"      - test(50: Byte,                                        "50")
      "short"     - test(45: Short,                                       "45")
      "int"       - test(666,                                             "666")
      "long"      - test(123L,                                            "123")
      "double"    - test(12.3,                                            "12.3")
      "string"    - test("yo",                                            "yo")
      "empty"     - test(EmptyVdom,                                       "")
      "optionN"   - test(Option.empty[Int],                               "")
      "optionS"   - test(Option(13),                                      "13")
      "optionSN"  - test(Option(Option.empty[Int]),                       "")
      "optionSS"  - test(Option(Option(13)),                              "13")
      "vdomArray" - test(VdomArray("hehe", <.div(^.key := 1, "one")),     "hehe<div>one</div>") // needs keys apparently
      "fragment"  - test(React.Fragment("hehe", <.div("one")),            "hehe<div>one</div>") // keys not required
      "fragmentK" - test(React.Fragment.withKey(1)("hehe", <.div("one")), "hehe<div>one</div>") // keyABLE

      "booleanF"  - compileError("""test(false, "")""")
      "booleanT"  - compileError("""test(true, "")""")

      "unionAttr" - {
        // JsNumber Byte | Short | Int | Float | Double
        "1/5" - test(<.div(^.aria.valueMax := (3.toByte  )), """<div aria-valuemax="3"></div>""")
        "2/5" - test(<.div(^.aria.valueMax := (3.toShort )), """<div aria-valuemax="3"></div>""")
        "3/5" - test(<.div(^.aria.valueMax := (3.toInt   )), """<div aria-valuemax="3"></div>""")
        "4/5" - test(<.div(^.aria.valueMax := (3.toFloat )), """<div aria-valuemax="3"></div>""")
        "5/5" - test(<.div(^.aria.valueMax := (3.toDouble)), """<div aria-valuemax="3"></div>""")
        "fail" - compileError(""" ^.aria.valueMax := "asdf" """)
      }
    }

    "tagModToJs" - {
      "childrenAsVdomNodes" - {
        val vdom = TagMod("hehe", 123, <.em(456L), C())
        val expect = "<span>hehe123<em>456</em><br/></span>"
        assertRender(<.span(vdom), expect)
        assertRender(Span(vdom.toJs.childrenAsVdomNodes: _*), expect)
      }
    }

    "noTagModOnElements" - {
      // https://github.com/japgolly/scalajs-react/issues/508
      @nowarn("cat=unused") val a: VdomElement = <.a
      val attr = ^.href := "#"
      <.a(attr)
      compileError("a(attr)")
    }

    "portal" - {
      ReactTestUtils2.withElement.use { portalTarget =>
        val comp = ScalaComponent.static("tmp")(
            <.div(
              "Here we go...",
              ReactPortal(<.div("NICE"), portalTarget)
            )
          )
        ReactTestUtils2.withRendered_(comp()) { d =>
          val compHtml = ReactTestUtils2.removeReactInternals(d.asHtml().outerHTML)
          val portalHtml = ReactTestUtils2.removeReactInternals(portalTarget.innerHTML)
          assertEq((compHtml, portalHtml), ("<div>Here we go...</div>", "<div>NICE</div>"))
        }
      }
    }

    "multipleEventHandlers" - {
      val c =
        ScalaComponent.builder[Unit]
          .initialState("init")
          .noBackend
          .renderS { ($, s) =>

            val eh1: ReactKeyboardEventFromInput => Callback =
              e => $.setState("enter!").when_(e.keyCode == KeyCode.Enter)

            val eh2: ReactKeyboardEventFromInput => Callback =
              e => $.setState("SPACE!").when_(e.keyCode == KeyCode.Space)

            <.input(
              ^.onKeyDown ==> eh1,
              ^.onKeyDown ==> eh2,
              ^.readOnly := true,
              ^.value := s)
          }
          .build

      ReactTestUtils2.withRendered_(c()) { d =>
        def txt() = d.asInput().value
        for {
          _ <- d.act_(SimEvent.Keyboard.Enter.simulateKeyDown(d.asHtml()))
          _ = assertEq(txt(), "enter!")
          _ <- d.act_(SimEvent.Keyboard.Space.simulateKeyDown(d.asHtml()))
          _ = assertEq(txt(), "SPACE!") 
        } yield ()
      }
    }

    "untypedRef" - {
      "fn" - {
        var value: AnyRef = null
        val c =
          ScalaComponent.builder[Unit]
            .initialState("init")
            .noBackend
            .render_S { s =>
              <.input(
                ^.untypedRef(value = _),
                ^.readOnly := true,
                ^.value := s)
            }
            .build

        ReactTestUtils2.withRendered_(c()) { _ =>
          assert(value.isInstanceOf[html.Input])
        }.map(_ => assert(value eq null))
      }

      "ref" - {
        val ref = Ref.toVdom[html.Input]
        val c =
          ScalaComponent.builder[Unit]
            .initialState("init")
            .noBackend
            .render_S { s =>
              <.input(
                ^.untypedRef := ref,
                ^.readOnly := true,
                ^.value := s)
            }
            .build

        ReactTestUtils2.withRendered_(c()) { _ =>
          val x = ref.get.runNow()
          assert(x.isDefined)
        }.map(_ => assert(ref.get.runNow().isEmpty))
      }
    }

    "callbackOption" - {
      "value" - assertCompiles(^.onKeyDown --> CallbackOption.pass)
      "fn" - {
        def keys(e: ReactKeyboardEventFromHtml): CallbackOption[Unit] = ???
        assertCompiles(^.onKeyDown ==> keys)
      }
    }

  }
}
