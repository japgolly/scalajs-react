package japgolly.scalajs.react

import org.scalajs.dom.html
import monocle.macros.Lenses
import scala.scalajs.js
import scalaz.Equal
import utest._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import MonocleReact._

object MiscTest extends TestSuite {

  lazy val CA = ScalaComponent.builder[Unit]("CA").render_C(c => <.div(c)).build
  lazy val CB = ScalaComponent.builder[Unit]("CB").render_C(c => <.span(c)).build

  @Lenses
  case class StrInt(str: String, int: Int)
  implicit def equalStrInt: Equal[StrInt] = Equal.equalA

  @Lenses
  case class StrIntWrap(strInt: StrInt)
  implicit def equalStrIntWrap: Equal[StrIntWrap] = Equal.equalA

  val witnessOptionCallbackToCallback: Option[Callback] => Callback =
    _.getOrEmpty

  override def tests = Tests {

    'children - {
      'argsToComponents - {

        'listOfScalatags - assertRender(
          CA(<.h1("nice"), <.h2("good")),
          "<div><h1>nice</h1><h2>good</h2></div>")

        'listOfReactComponents - assertRender(
          CA(CB(<.h1("nice")), CB(<.h2("good"))),
          "<div><span><h1>nice</h1></span><span><h2>good</h2></span></div>")
      }

      'rendersGivenChildren - {
        'none - assertRender(CA(), "<div></div>")
        'one - assertRender(CA(<.h1("yay")), "<div><h1>yay</h1></div>")
        'two - assertRender(CA(<.h1("yay"), <.h3("good")), "<div><h1>yay</h1><h3>good</h3></div>")
        'nested - assertRender(CA(CB(<.h1("nice"))), "<div><span><h1>nice</h1></span></div>")
      }

    }

    'selectWithMultipleValues - {
      val s = ScalaComponent.builder[Unit]("s").renderStatic(
          <.select(^.multiple := true, ^.value := js.Array("a", "c"))(
            <.option(^.value := "a")("a"),
            <.option(^.value := "b")("b"),
            <.option(^.value := "c")("c"))
        ).build

      val c = ReactTestUtils.renderIntoDocument(s())
      val sel = c.getDOMNode.domCast[html.Select]
      val options = sel.options.asInstanceOf[js.Array[html.Option]] // https://github.com/scala-js/scala-js-dom/pull/107
      val selectedOptions = options filter (_.selected) map (_.value)
      assert(selectedOptions.toSet == Set("a", "c"))
    }

    'renderScopeZoomState - {
      case class SI(s: String, i: Int)
      val C = ScalaComponent.builder[SI]("C").initialStateFromProps(p => p).render { $ =>
        val f = $.mountedImpure.zoomState(_.i)(b => _.copy(i = b))
        <.div($.state.s + "/" + (f.state * 3))
      }.build
      assertRender(C(SI("Me",7)), "<div>Me/21</div>")
    }

    'multiModState - {
      'simple - {
        val C = ScalaComponent.builder[Unit]("multiModState")
          .initialState(3)
          .render { $ =>
            val add7 = $.modState(_ + 7)
            val add1 = $.modState(_ + 1)
            <.button(^.onClick --> (add1 >> add7))
          }
          .build
        val c = ReactTestUtils.renderIntoDocument(C())
        assertEq(c.state, 3)
        Simulation.click run c
        assertEq(c.state, 11)
      }

      'zoomState - {
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

      'zoomStateL - {
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

      'zoomStateL2 - {
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

    'domExt - {
      import org.scalajs.dom.raw._
      import InferenceUtil._
      'domCast   - test[Node](_.domCast[HTMLInputElement]).expect[HTMLInputElement]
      'domAsHtml - test[Node](_.domAsHtml).expect[HTMLElement]
      'domToHtml - {
        import org.scalajs.dom._
        val input = document.createElement("input")
        assert(input.domToHtml == Option(input.asInstanceOf[HTMLElement]))
      }
    }

    /*
    'future - {
      val X = ScalaComponent.builder[Unit]("X")
        .initialState(1)
        .render_S(s => <.div("state = ", s))
        .build

      val x = ReactTestUtils renderIntoDocument X()

      'direct - {
        var i = 10
        val ff = x.future.modState(_ + 1, Callback(i -= 7))
        val f: Future[Unit] = ff
        f.map { _ =>
          assert(x.state == 2)
          assert(i == 3)
        }
      }

      'callback - {
        var i = 90
        val cbb = x.accessCB.future.modState(_ + 1, Callback(i -= 5))
        val cb: CallbackTo[Future[Unit]] = cbb
        // Look, it's repeatable
        for {
          _ <- cb.runNow()
          _ <- cb.runNow()
        } yield {
          assert(x.state == 3)
          assert(i == 80)
        }
      }
    }
    */

  }
}
