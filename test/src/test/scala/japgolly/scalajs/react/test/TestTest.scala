package japgolly.scalajs.react.test

import sizzle.Sizzle
import japgolly.scalajs.react.vdom.ReactAttr
import org.scalajs.dom.raw.{HTMLElement, HTMLInputElement}
import utest._
import japgolly.scalajs.react._
import vdom.prefix_<^._
import TestUtil._
import TestUtil2._

object TestTest extends TestSuite {

  lazy val A = ReactComponentB[Unit]("A").render_C(c => <.p(^.cls := "AA", c)).build
  lazy val B = ReactComponentB[Unit]("B").render(_ => <.p(^.cls := "BB", "hehehe")).build
  lazy val rab = ReactTestUtils.renderIntoDocument(A(B()))

  val inputRef = Ref[HTMLInputElement]("r")
  lazy val IC = ReactComponentB[Unit]("IC").initialState(true).renderS(($,s) => {
    val ch = (e: ReactEvent) => $.modState(x => !x)
    <.label(
      <.input.checkbox(^.checked := s, ^.onClick ==> ch, ^.ref := inputRef),
      <.span(s"s = $s")
    )
  }).build

  lazy val IT = ReactComponentB[Unit]("IT").initialState("NIL").renderS(($,s) => {
    val ch = (e: SyntheticEvent[HTMLInputElement]) => $.setState(e.target.value.toUpperCase)
    <.input.text(^.value := s, ^.onChange ==> ch)
  }).build

  val tests = TestSuite {

    'findRenderedDOMComponentWithClass {
      val n = ReactDOM findDOMNode ReactTestUtils.findRenderedDOMComponentWithClass(rab, "BB")
      assert(n.matchesBy[HTMLElement](_.className == "BB"))
    }

    'findRenderedComponentWithType {
      val n = ReactDOM findDOMNode ReactTestUtils.findRenderedComponentWithType(rab, B)
      assert(n.matchesBy[HTMLElement](_.className == "BB"))
    }

    'renderIntoDocument {
      def test(c: ComponentM, exp: String): Unit =
        assertOuterHTML(ReactDOM.findDOMNode(c), exp)
      'plainElement {
        val re: ReactElement = <.div("Good")
        val c = ReactTestUtils.renderIntoDocument(re)
        test(c, """<div>Good</div>""")
      }
      'component {
        val c: ReactComponentM[Unit, Unit, Unit, TopNode] = ReactTestUtils.renderIntoDocument(B())
        test(c, """<p class="BB">hehehe</p>""")
      }
    }

    'Simulate {
      'click {
        val c = ReactTestUtils.renderIntoDocument(IC())
        val i = inputRef(c).get
        val s = ReactTestUtils.findRenderedDOMComponentWithTag(c, "span")
        val a = ReactDOM.findDOMNode(s).innerHTML
        ReactTestUtils.Simulate.click(i)
        val b = ReactDOM.findDOMNode(s).innerHTML
        assert(a != b)
      }

      'eventTypes {
        val eventTypes = Seq[(ReactAttr, ReactOrDomNode ⇒ Unit)](
          (^.onBeforeInput,       n ⇒ ReactTestUtils.Simulate.beforeInput(n)),
          (^.onBlur,              n ⇒ ReactTestUtils.Simulate.blur(n)),
          (^.onChange,            n ⇒ ReactTestUtils.Simulate.change(n)),
          (^.onClick,             n ⇒ ReactTestUtils.Simulate.click(n)),
          (^.onCompositionEnd,    n ⇒ ReactTestUtils.Simulate.compositionEnd(n)),
          (^.onCompositionStart,  n ⇒ ReactTestUtils.Simulate.compositionStart(n)),
          (^.onCompositionUpdate, n ⇒ ReactTestUtils.Simulate.compositionUpdate(n)),
          (^.onContextMenu,       n ⇒ ReactTestUtils.Simulate.contextMenu(n)),
          (^.onCopy,              n ⇒ ReactTestUtils.Simulate.copy(n)),
          (^.onCut,               n ⇒ ReactTestUtils.Simulate.cut(n)),
          (^.onDrag,              n ⇒ ReactTestUtils.Simulate.drag(n)),
          (^.onDblClick,          n ⇒ ReactTestUtils.Simulate.doubleClick(n)),
          (^.onDragEnd,           n ⇒ ReactTestUtils.Simulate.dragEnd(n)),
          (^.onDragEnter,         n ⇒ ReactTestUtils.Simulate.dragEnter(n)),
          (^.onDragExit,          n ⇒ ReactTestUtils.Simulate.dragExit(n)),
          (^.onDragLeave,         n ⇒ ReactTestUtils.Simulate.dragLeave(n)),
          (^.onDragOver,          n ⇒ ReactTestUtils.Simulate.dragOver(n)),
          (^.onDragStart,         n ⇒ ReactTestUtils.Simulate.dragStart(n)),
          (^.onDrop,              n ⇒ ReactTestUtils.Simulate.drop(n)),
          (^.onError,             n ⇒ ReactTestUtils.Simulate.error(n)),
          (^.onFocus,             n ⇒ ReactTestUtils.Simulate.focus(n)),
          (^.onInput,             n ⇒ ReactTestUtils.Simulate.input(n)),
          (^.onKeyDown,           n ⇒ ReactTestUtils.Simulate.keyDown(n)),
          (^.onKeyPress,          n ⇒ ReactTestUtils.Simulate.keyPress(n)),
          (^.onKeyUp,             n ⇒ ReactTestUtils.Simulate.keyUp(n)),
          (^.onLoad,              n ⇒ ReactTestUtils.Simulate.load(n)),
          (^.onMouseDown,         n ⇒ ReactTestUtils.Simulate.mouseDown(n)),
          (^.onMouseEnter,        n ⇒ ReactTestUtils.Simulate.mouseEnter(n)),
          (^.onMouseLeave,        n ⇒ ReactTestUtils.Simulate.mouseLeave(n)),
          (^.onMouseMove,         n ⇒ ReactTestUtils.Simulate.mouseMove(n)),
          (^.onMouseOut,          n ⇒ ReactTestUtils.Simulate.mouseOut(n)),
          (^.onMouseOver,         n ⇒ ReactTestUtils.Simulate.mouseOver(n)),
          (^.onMouseUp,           n ⇒ ReactTestUtils.Simulate.mouseUp(n)),
          (^.onPaste,             n ⇒ ReactTestUtils.Simulate.paste(n)),
          (^.onReset,             n ⇒ ReactTestUtils.Simulate.reset(n)),
          (^.onScroll,            n ⇒ ReactTestUtils.Simulate.scroll(n)),
          (^.onSelect,            n ⇒ ReactTestUtils.Simulate.select(n)),
          (^.onSubmit,            n ⇒ ReactTestUtils.Simulate.submit(n)),
          (^.onTouchCancel,       n ⇒ ReactTestUtils.Simulate.touchCancel(n)),
          (^.onTouchEnd,          n ⇒ ReactTestUtils.Simulate.touchEnd(n)),
          (^.onTouchMove,         n ⇒ ReactTestUtils.Simulate.touchMove(n)),
          (^.onTouchStart,        n ⇒ ReactTestUtils.Simulate.touchStart(n)),
          (^.onWheel,             n ⇒ ReactTestUtils.Simulate.wheel(n))
        )

        val results = eventTypes map {
          case (eventType, simF) ⇒
            val IDC = ReactComponentB[Unit]("IC").initialState(true).render($ => {
              val ch = (e: ReactEvent) => $.modState(x => !x)
              <.label(
                <.input.text(^.value := $.state, eventType ==> ch, ^.ref := inputRef),
                <.span(s"s = ${$.state}")
              )
            }).build

            val c = ReactTestUtils.renderIntoDocument(IDC())
            val s = ReactTestUtils.findRenderedDOMComponentWithTag(c, "span")

            val a = ReactDOM.findDOMNode(s).innerHTML
            simF(inputRef(c).get)
            val b = ReactDOM.findDOMNode(s).innerHTML

            (eventType, a != b)
        }

        val failed = results collect {
          case (attr, false) ⇒ attr.name
        }

        assert(failed == Seq.empty)
      }

      'change {
        val c = ReactTestUtils.renderIntoDocument(IT()).domType[HTMLInputElement]
        ChangeEventData("hehe").simulate(c)
        val t = ReactDOM.findDOMNode(c).value
        t mustEqual "HEHE"
      }
      'focusChangeBlur {
        var events = Vector.empty[String]
        val C = ReactComponentB[Unit]("C").initialState("ey").render(T => {
          def e(s: String) = Callback(events :+= s)
          def chg(ev: ReactEventI) =
            e("change") >> T.setState(ev.target.value)
          <.input.text(^.value := T.state, ^.ref := inputRef, ^.onFocus --> e("focus"), ^.onChange ==> chg, ^.onBlur --> e("blur"))
        }).build
        val c = ReactTestUtils.renderIntoDocument(C())
        val i = inputRef(c).get
        Simulation.focusChangeBlur("good") run i
        events mustEqual Vector("focus", "change", "blur")
        i.value mustEqual "good"
      }
      'targetByName {
        val c = ReactTestUtils.renderIntoDocument(IC())
        var count = 0
        def tgt = {
          count += 1
          Sizzle("input", ReactDOM findDOMNode c).sole
        }
        Simulation.focusChangeBlur("-") run tgt
        assert(count == 3)
      }
    }

    'withRenderedIntoDocument {
      var m: IC.Mounted = null
      ReactTestUtils.withRenderedIntoDocument(IC()) { mm =>
        m = mm
        val n = m.getDOMNode()
        assert(ReactTestUtils.removeReactDataAttr(n.outerHTML) startsWith "<label><input ")
        assert(m.isMounted())
      }
      assert(!m.isMounted())
    }
  }
}
