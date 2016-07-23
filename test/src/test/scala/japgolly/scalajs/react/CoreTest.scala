package japgolly.scalajs.react

import japgolly.scalajs.react.CompScope._
import japgolly.scalajs.react.CompState._
import japgolly.scalajs.react.TestUtil._
import japgolly.scalajs.react.TestUtil2._
import japgolly.scalajs.react.test.{ReactTestUtils, Simulation}
import japgolly.scalajs.react.vdom.all._
import monocle.macros.Lenses
import org.scalajs.dom.raw._
import scala.scalajs.js
import utest._

object CoreTest extends TestSuite {

  lazy val CA = ReactComponentB[Unit]("CA").render_C(c => div(c)).build
  lazy val CB = ReactComponentB[Unit]("CB").render_C(c => span(c)).build
  lazy val H1 = ReactComponentB[String]("H").render_P(p => h1(p)).build

  lazy val SI = ReactComponentB[Unit]("SI")
    .initialState(123)
    .render(T => input(value := T.state.toString))
    .domType[HTMLInputElement]
    .componentDidMount($ => Callback {
      val s: String = ReactDOM.findDOMNode($).value // Look, it knows its DOM node type
    })
    .build

  @Lenses
  case class StrInt(str: String, int: Int)

  @Lenses
  case class StrIntWrap(strInt: StrInt)

  val tests = TestSuite {

    'props {
      'unit {
        val r = ReactComponentB[Unit]("U").render_C(c => h1(c)).build
        r(div("great")) shouldRender "<h1><div>great</div></h1>"
      }

      'required {
        val r = ReactComponentB[String]("C").render_P(name => div("Hi ", name)).build
        r("Mate") shouldRender "<div>Hi Mate</div>"
      }

      val O = ReactComponentB[String]("C").render_P(name => div("Hey ", name)).propsDefault("man").build
      'optionalNone {
        O() shouldRender "<div>Hey man</div>"
      }
      'optionalSome {
        O(Some("dude")) shouldRender "<div>Hey dude</div>"
      }

      'always {
        val r = ReactComponentB[String]("C").render_P(name => div("Hi ", name)).propsConst("there").build
        r() shouldRender "<div>Hi there</div>"
      }
    }

    'builder {
      'configure {
        var called = 0
        val f = (_: ReactComponentB[Unit,Unit,Unit,TopNode]).componentWillMount(_ => Callback(called += 1))
        val c = ReactComponentB[Unit]("X").render(_ => div("")).configure(f, f).build
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
      // def inc(s: CompStateFocus[Int]) = s.modState(_ * 3)
      case class SI(s: String, i: Int)
      val C = ReactComponentB[SI]("C").initialState_P(p => p).render($ => {
        val f = $.zoom(_.i)((a,b) => a.copy(i = b))
        // inc(f)
        div($.state.s + "/" + (f.state*3))
      }).build
      C(SI("Me",7)) shouldRender "<div>Me/21</div>"
    }

    'mountedStateAccess {
      val c = ReactTestUtils.renderIntoDocument(SI())
      assert(c.state == 123)
    }

    'builtWithDomType {
      val c = ReactTestUtils.renderIntoDocument(SI())
      val v = ReactDOM.findDOMNode(c).value // Look, it knows its DOM node type
      assert(v == "123")
    }

    'selectWithMultipleValues {
      val s = ReactComponentB[Unit]("s")
        .render(T =>
          select(multiple := true, value := js.Array("a", "c"))(
            option(value := "a")("a"),
            option(value := "b")("b"),
            option(value := "c")("c")
          )
        )
        .domType[HTMLSelectElement]
        .build

      val c = ReactTestUtils.renderIntoDocument(s())
      val sel = ReactDOM.findDOMNode(c)
      val options = sel.options.asInstanceOf[js.Array[HTMLOptionElement]] // https://github.com/scala-js/scala-js-dom/pull/107
      val selectedOptions = options filter (_.selected) map (_.value)
      assert(selectedOptions.toSet == Set("a", "c"))
    }

    'inference {
      import TestUtil.Inference._
      def st_get: S => T = null
      def st_set: (S, T) => S = null

      "DuringCallbackU ops" - test[DuringCallbackU[P, S, U]   ](_.zoom(st_get)(st_set)).expect[ReadDirectWriteCallbackOps[T]]
      "DuringCallbackM ops" - test[DuringCallbackM[P, S, U, N]](_.zoom(st_get)(st_set)).expect[ReadDirectWriteCallbackOps[T]]
      "BackendScope    ops" - test[BackendScope   [P, S]      ](_.zoom(st_get)(st_set)).expect[ReadCallbackWriteCallbackOps[T]]
      "ReactComponentM ops" - test[ReactComponentM[P, S, U, N]](_.zoom(st_get)(st_set)).expect[ReadDirectWriteDirectOps[T]]

      "DuringCallbackU props" - test[DuringCallbackU[P, S, U]   ](_.props).expect[P]
      "DuringCallbackM props" - test[DuringCallbackM[P, S, U, N]](_.props).expect[P]
      "WillUpdate      props" - test[WillUpdate     [P, S, U, N]](_.props).expect[P]
      "BackendScope    props" - test[BackendScope   [P, S]      ](_.props).expect[CallbackTo[P]]
      "ReactComponentM props" - test[ReactComponentM[P, S, U, N]](_.props).expect[P]

      "DuringCallbackU state" - test[DuringCallbackU[P, S, U]   ](_.state).expect[S]
      "DuringCallbackM state" - test[DuringCallbackM[P, S, U, N]](_.state).expect[S]
      "WillUpdate      state" - test[WillUpdate     [P, S, U, N]](_.state).expect[S]
      "BackendScope    state" - test[BackendScope   [P, S]      ](_.state).expect[CallbackTo[S]]
      "ReactComponentM state" - test[ReactComponentM[P, S, U, N]](_.state).expect[S]

      "DuringCallbackU state" - test[DuringCallbackU[P, S, U]   ](_.zoom(st_get)(st_set).state).expect[T]
      "DuringCallbackM state" - test[DuringCallbackM[P, S, U, N]](_.zoom(st_get)(st_set).state).expect[T]
      "BackendScope    state" - test[BackendScope   [P, S]      ](_.zoom(st_get)(st_set).state).expect[CallbackTo[T]]
      "ReactComponentM state" - test[ReactComponentM[P, S, U, N]](_.zoom(st_get)(st_set).state).expect[T]
    }

    'shouldCorrectlyDetermineIfComponentIsMounted {
      val C = ReactComponentB[Unit]("IsMountedTestComp")
          .render(P => div())
          .componentWillMount(scope => Callback(assert(!scope.isMounted())))
          .componentDidMount(scope => Callback(assert(scope.isMounted())))
          .build
      val instance =  ReactTestUtils.renderIntoDocument(C())
      assert(instance.isMounted())
    }

    'findDOMNode {
      val m = ReactTestUtils renderIntoDocument H1("good")
      assertOuterHTML(ReactDOM.findDOMNode(m), "<h1>good</h1>")
    }

    // Changed to an extension method that calls ReactDOM.findDOMNode
    'getDOMNode {
      val m = ReactTestUtils renderIntoDocument H1("good")
      assertOuterHTML(m.getDOMNode(), "<h1>good</h1>")
    }

    'domTypeBeforeCallbacks {
      ReactComponentB[Unit]("").stateless
        .render(_ => canvas())
        .domType[HTMLCanvasElement]
        .componentDidMount($ => Callback(ReactDOM.findDOMNode($).getContext("2d")))
        .build
    }

    'multiModState {
      'simple {
        val C = ReactComponentB[Unit]("multiModState")
          .initialState(3)
          .render { $ =>
            val add7 = $.modState(_ + 7)
            val add1 = $.modState(_ + 1)
            button(onClick --> (add1 >> add7))
          }
          .build
        val c = ReactTestUtils.renderIntoDocument(C())
        c.state mustEqual 3
        Simulation.click run c
        c.state mustEqual 11
      }
      'zoom {
        val C = ReactComponentB[Unit]("multiModState")
          .initialState(StrInt("yay", 3))
          .render { $ =>
            val $$ = $.zoom(_.int)((a,b) => a.copy(int = b))
            val add7 = $$.modState(_ + 7)
            val add1 = $$.modState(_ + 1)
            button(onClick --> (add1 >> add7))
          }
          .build
        val c = ReactTestUtils.renderIntoDocument(C())
        c.state mustEqual StrInt("yay", 3)
        Simulation.click run c
        c.state mustEqual StrInt("yay", 11)
        c.setState(StrInt("oh", 100))
        Simulation.click run c
        c.state mustEqual StrInt("oh", 108)
      }
      'zoomL {
        import MonocleReact._ // TODO Move
        val C = ReactComponentB[Unit]("multiModState")
          .initialState(StrInt("yay", 3))
          .render { $ =>
            val $$ = $ zoomL StrInt.int
            val add7 = $$.modState(_ + 7)
            val add1 = $$.modState(_ + 1)
            button(onClick --> (add1 >> add7))
          }
          .build
        val c = ReactTestUtils.renderIntoDocument(C())
        c.state mustEqual StrInt("yay", 3)
        Simulation.click run c
        c.state mustEqual StrInt("yay", 11)
        c.setState(StrInt("oh", 100))
        Simulation.click run c
        c.state mustEqual StrInt("oh", 108)
      }
      'zoomL2 {
        import MonocleReact._ // TODO Move
        val C = ReactComponentB[Unit]("multiModState")
          .initialState(StrIntWrap(StrInt("yay", 3)))
          .render { $ =>
            val $$ = $ zoomL StrIntWrap.strInt zoomL StrInt.int
            val add7 = $$.modState(_ + 7)
            val add1 = $$.modState(_ + 1)
            button(onClick --> (add1 >> add7))
          }
          .build
        val c = ReactTestUtils.renderIntoDocument(C())
        c.state mustEqual StrIntWrap(StrInt("yay", 3))
        Simulation.click run c
        c.state mustEqual StrIntWrap(StrInt("yay", 11))
        c.setState(StrIntWrap(StrInt("oh", 100)))
        Simulation.click run c
        c.state mustEqual StrIntWrap(StrInt("oh", 108))
      }
    }

    'domExt {
      import TestUtil.Inference._
      'domCast   - test[Node](_.domCast[HTMLInputElement]).expect[HTMLInputElement]
      'domAsHtml - test[Node](_.domAsHtml).expect[HTMLElement]
      'domToHtml {
        import org.scalajs.dom._
        val input = document.createElement("input")
        input.domToHtml mustEqual Some(input.asInstanceOf[HTMLElement])
      }
    }

    'displayName {
      assertEq(CA.displayName, "CA")
      val mounted = ReactTestUtils.renderIntoDocument(CA())
      assertEq(mounted.displayName, "CA")
    }
  }
}
