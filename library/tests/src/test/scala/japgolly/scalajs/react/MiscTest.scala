package japgolly.scalajs.react

import japgolly.scalajs.react.ReactMonocle._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.util.JsUtil
import japgolly.scalajs.react.vdom.html_<^._
import java.time.Duration
import monocle._
import scala.scalajs.js
import scala.util.Try
import utest._

object MiscTest extends TestSuite {

  lazy val CA = ScalaComponent.builder[Unit]("CA").render_C(c => <.div(c)).build
  lazy val CB = ScalaComponent.builder[Unit]("CB").render_C(c => <.span(c)).build

  case class StrInt(str: String, int: Int)
  object StrInt {
    val str = Lens[StrInt, String](_.str)(x => _.copy(str = x))
    val int = Lens[StrInt, Int   ](_.int)(x => _.copy(int = x))
  }
  implicit def equalStrInt: UnivEq[StrInt] = UnivEq.force

  case class StrIntWrap(strInt: StrInt)
  object StrIntWrap {
    val strInt = Lens[StrIntWrap, StrInt](_.strInt)(x => _.copy(strInt = x))
  }
  implicit def equalStrIntWrap: UnivEq[StrIntWrap] = UnivEq.force

  val witnessOptionCallbackToCallback: Option[Callback] => Callback =
    _.getOrEmpty

  override def tests = Tests {

    "version" - {
      assertEq(React.version, ReactDOM.version)
      assertEq(React.version, ReactDOMServer.version)
    }

    "children" - {
      "argsToComponents" - {

        "listOfScalatags" - assertRender(
          CA(<.h1("nice"), <.h2("good")),
          "<div><h1>nice</h1><h2>good</h2></div>")

        "listOfReactComponents" - assertRender(
          CA(CB(<.h1("nice")), CB(<.h2("good"))),
          "<div><span><h1>nice</h1></span><span><h2>good</h2></span></div>")
      }

      "rendersGivenChildren" - {
        "none" - assertRender(CA(), "<div></div>")
        "one" - assertRender(CA(<.h1("yay")), "<div><h1>yay</h1></div>")
        "two" - assertRender(CA(<.h1("yay"), <.h3("good")), "<div><h1>yay</h1><h3>good</h3></div>")
        "nested" - assertRender(CA(CB(<.h1("nice"))), "<div><span><h1>nice</h1></span></div>")
      }

    }

    "selectWithMultipleValues" - {
      val s = ScalaComponent.builder[Unit]("s").renderStatic(
          <.select(^.multiple := true, ^.value := js.Array("a", "c"))(
            <.option(^.value := "a")("a"),
            <.option(^.value := "b")("b"),
            <.option(^.value := "c")("c"))
        ).build

      ReactTestUtils2.withRendered(s()) { d =>
        val sel = d.asSelect()
        val selectedOptions =sel.options.filter(_.selected).map(_.value)
        assertSet(selectedOptions.toSet, Set("a", "c"))
      }
    }

    "renderScopeZoomState" - {
      case class SI(s: String, i: Int)
      val C = ScalaComponent.builder[SI]("C").initialStateFromProps(p => p).render { $ =>
        val f = $.mountedImpure.zoomState(_.i)(b => _.copy(i = b))
        <.div($.state.s + "/" + (f.state * 3))
      }.build
      assertRender(C(SI("Me",7)), "<div>Me/21</div>")
    }

    "multiModState" - {
      "simple" - {
        val C = ScalaComponent.builder[Unit]("multiModState")
          .initialState(3)
          .render { $ =>
            val add7 = $.modState(_ + 7)
            val add1 = $.modState(_ + 1)
            <.button(<.span($.state), ^.onClick --> (add1 >> add7))
          }
          .build
        ReactTestUtils2.withRendered(C()) { d =>
          d.select("span").innerHTML.assert("3")
          d.node.foreach(Simulation.click.run(_))
          d.select("span").innerHTML.assert("11")
        }
      }

      "zoomState" - {
        val C = ScalaComponent.builder[Unit]("multiModState")
          .initialState(StrInt("yay", 3))
          .render { $ =>
            val $$ = $.mountedPure.zoomState(_.int)(b => _.copy(int = b))
            val add7 = $$.modState(_ + 7)
            val add1 = $$.modState(_ + 1)
            <.button(^.onClick --> (add1 >> add7))
          }
          .build
        val c = ReactTestUtils.renderIntoDocument(C())
        assertEq(c.state, StrInt("yay", 3))
        Simulation.click run c
        assertEq(c.state, StrInt("yay", 11))
        c.setState(StrInt("oh", 100))
        Simulation.click run c
        assertEq(c.state, StrInt("oh", 108))
      }

      "zoomStateL" - {
        val C = ScalaComponent.builder[Unit]("multiModState")
          .initialState(StrInt("yay", 3))
          .render { $ =>
            val $$ = $.mountedPure zoomStateL StrInt.int
            val add7 = $$.modState(_ + 7)
            val add1 = $$.modState(_ + 1)
            <.button(^.onClick --> (add1 >> add7))
          }
          .build
        val c = ReactTestUtils.renderIntoDocument(C())
        assertEq(c.state, StrInt("yay", 3))
        Simulation.click run c
        assertEq(c.state, StrInt("yay", 11))
        c.setState(StrInt("oh", 100))
        Simulation.click run c
        assertEq(c.state, StrInt("oh", 108))
      }

      "zoomStateL2" - {
        val C = ScalaComponent.builder[Unit]("multiModState")
          .initialState(StrIntWrap(StrInt("yay", 3)))
          .render { $ =>
            val $$ = $.mountedPure zoomStateL StrIntWrap.strInt zoomStateL StrInt.int
            val add7 = $$.modState(_ + 7)
            val add1 = $$.modState(_ + 1)
            <.button(^.onClick --> (add1 >> add7))
          }
          .build
        val c = ReactTestUtils.renderIntoDocument(C())
        assertEq(c.state, StrIntWrap(StrInt("yay", 3)))
        Simulation.click run c
        assertEq(c.state, StrIntWrap(StrInt("yay", 11)))
        c.setState(StrIntWrap(StrInt("oh", 100)))
        Simulation.click run c
        assertEq(c.state, StrIntWrap(StrInt("oh", 108)))
      }
    }

    "domExt" - {
      import org.scalajs.dom._
      "domCast"   - assertType[Node].map(_.domCast[HTMLInputElement]).is[HTMLInputElement]
      "domAsHtml" - assertType[Node].map(_.domAsHtml).is[HTMLElement]
      "domToHtml" - {
        import org.scalajs.dom._
        val input = document.createElement("input")
        assert(input.domToHtml == Option(input.asInstanceOf[HTMLElement]))
      }
    }

    "strictMode" -
      assertRender(
        React.StrictMode(CA(<.h2("nice"), <.h3("good"))),
        "<div><h2>nice</h2><h3>good</h3></div>")

    "symbolShouldntCrashToString" - {
      for (s <- List(js.Symbol.search, js.Symbol.forKey("ah"))) {
        JsUtil.inspectValue(s)
        JsUtil.safeToString(s)
        // TODO: https://github.com/lampepfl/dotty/issues/12247
        // Try(JsComponent[Null, Children.None, Null](s))
        // Try(JsFnComponent[Null, Children.None](s))
        def t1 = JsComponent[Null, Children.None, Null](s); Try(t1)
        def t2 = JsFnComponent[Null, Children.None](s); Try(t2)
      }
    }

    "Profiler" - {
      import React.Profiler.OnRenderData
      var results = Vector.empty[OnRenderData]
      val comp = ScalaComponent.builder[Int]("")
        .render_P(i =>
          React.Profiler("blah", d => Callback { results :+= d })(
            <.div("i = ", i)
          )
        ).build
      ReactTestUtils2.withRendered(comp(234)) { d =>
        d.outerHTML.assert("<div>i = 234</div>")
      }
      assertEq(results.length, 1)
      val r = results.head
      assertEq(r.id, "blah")
      assertEq(r.phase, "mount")
      assertEq(r.phaseIsMount, true)
      assertEq(r.phaseIsUpdate, false)
    }

    "durationFromDOMHighResTimeStamp" - {
      assertEq(JsUtil.durationFromDOMHighResTimeStamp(3), Duration.ofMillis(3))
    }

    "static" - {
      "named" - {
        val r = ScalaComponent.static("asdf")(<.div("hehe"))
        assertRender(r(), "<div>hehe</div>")
      }
      "auto" - {
        val r = ScalaComponent.static(<.div("hehe"))
        assertRender(r(), "<div>hehe</div>")
      }
    }
  }
}
