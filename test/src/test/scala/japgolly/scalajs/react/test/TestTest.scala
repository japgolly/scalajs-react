package japgolly.scalajs.react.test

import japgolly.scalajs.react.vdom.Attr
import org.scalajs.dom.raw.{HTMLElement, HTMLInputElement}
import utest._
import japgolly.scalajs.react._
import vdom.all._
import TestUtil._

object TestTest extends TestSuite {

  lazy val A = ReactComponentB[Unit]("A").render((_,c) => p(cls := "AA", c)).buildU
  lazy val B = ReactComponentB[Unit]("B").render(_ => p(cls := "BB", "hehehe")).buildU
  lazy val rab = ReactTestUtils.renderIntoDocument(A(B()))

  val inputRef = Ref[HTMLInputElement]("r")
  lazy val IC = ReactComponentB[Unit]("IC").initialState(true).renderS((t,_,s) => {
    val ch = (e: ReactEvent) => t.modState(x => !x)
    label(
      input(`type` := "checkbox", checked := s, onClick ==> ch, ref := inputRef),
      span(s"s = $s")
    )
  }).buildU

  lazy val IT = ReactComponentB[Unit]("IT").initialState("NIL").renderS((t,_,s) => {
    val ch = (e: SyntheticEvent[HTMLInputElement]) => t.setState(e.target.value.toUpperCase)
    input(`type` := "text", value := s, onChange ==> ch)
  }).buildU

  val tests = TestSuite {
    'isTextComponent {
      val r = ReactTestUtils.isTextComponent(A())
      assert(!r)
    }

    'findRenderedDOMComponentWithClass {
      val n = ReactTestUtils.findRenderedDOMComponentWithClass(rab, "BB").getDOMNode()
      assert(n.matchesBy[HTMLElement](_.className == "BB"))
    }

    'findRenderedComponentWithType {
      val n = ReactTestUtils.findRenderedComponentWithType(rab, B).getDOMNode()
      assert(n.matchesBy[HTMLElement](_.className == "BB"))
    }

    'renderIntoDocument {
      def test(c: ComponentM, exp: String): Unit = {
        val h = removeReactDataAttr(c.getDOMNode().outerHTML)
        h mustEqual exp
      }
      'plainElement {
        val re: ReactElement = div("Good")
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
        val a = s.getDOMNode().innerHTML
        ReactTestUtils.Simulate.click(i)
        val b = s.getDOMNode().innerHTML
        assert(a != b)
      }

      'eventTypes {
        val eventTypes = Seq[(Attr, ReactOrDomNode ⇒ Unit)](
          (onBlur,        n ⇒ ReactTestUtils.Simulate.blur(n)),
          (onChange,      n ⇒ ReactTestUtils.Simulate.change(n)),
          (onClick,       n ⇒ ReactTestUtils.Simulate.click(n)),
          (onDblClick,    n ⇒ ReactTestUtils.Simulate.doubleClick(n)),
          (onDragEnd,     n ⇒ ReactTestUtils.Simulate.dragEnd(n)),
          (onDragEnter,   n ⇒ ReactTestUtils.Simulate.dragEnter(n)),
          (onDragLeave,   n ⇒ ReactTestUtils.Simulate.dragLeave(n)),
          (onDragOver,    n ⇒ ReactTestUtils.Simulate.dragOver(n)),
          (onDragStart,   n ⇒ ReactTestUtils.Simulate.dragStart(n)),
          (onDrop,        n ⇒ ReactTestUtils.Simulate.drop(n)),
          (onFocus,       n ⇒ ReactTestUtils.Simulate.focus(n)),
          (onKeyDown,     n ⇒ ReactTestUtils.Simulate.keyDown(n)),
          (onKeyPress,    n ⇒ ReactTestUtils.Simulate.keyPress(n)),
          (onKeyUp,       n ⇒ ReactTestUtils.Simulate.keyUp(n)),
          (onLoad,        n ⇒ ReactTestUtils.Simulate.load(n)),
          (onMouseDown,   n ⇒ ReactTestUtils.Simulate.mouseDown(n)),
          (onMouseMove,   n ⇒ ReactTestUtils.Simulate.mouseMove(n)),
          (onMouseOut,    n ⇒ ReactTestUtils.Simulate.mouseOut(n)),
          (onMouseOver,   n ⇒ ReactTestUtils.Simulate.mouseOver(n)),
          (onMouseUp,     n ⇒ ReactTestUtils.Simulate.mouseUp(n)),
          (onReset,       n ⇒ ReactTestUtils.Simulate.reset(n)),
          (onScroll,      n ⇒ ReactTestUtils.Simulate.scroll(n)),
          (onSubmit,      n ⇒ ReactTestUtils.Simulate.submit(n)),
          (onTouchCancel, n ⇒ ReactTestUtils.Simulate.touchCancel(n)),
          (onTouchEnd,    n ⇒ ReactTestUtils.Simulate.touchEnd(n)),
          (onTouchMove,   n ⇒ ReactTestUtils.Simulate.touchMove(n)),
          (onTouchStart,  n ⇒ ReactTestUtils.Simulate.touchStart(n))
        )

        val results = eventTypes map {
          case (eventType, simF) ⇒
            val IDC = ReactComponentB[Unit]("IC").initialState(true).render($ => {
              val ch = (e: ReactEvent) => $.modState(x => !x)
              label(
                input(`type` := "text", value := $.state, eventType ==> ch, ref := inputRef),
                span(s"s = ${$.state}")
              )
            }).buildU

            val c = ReactTestUtils.renderIntoDocument(IDC())
            val s = ReactTestUtils.findRenderedDOMComponentWithTag(c, "span")

            val a = s.getDOMNode().innerHTML
            simF(inputRef(c).get)
            val b = s.getDOMNode().innerHTML

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
        val t = c.getDOMNode().value
        t mustEqual "HEHE"
      }
      'focusChangeBlur {
        var events = Vector.empty[String]
        val C = ReactComponentB[Unit]("C").initialState("ey").render(T => {
          def e(s: String): Unit = events :+= s
          def chg: ReactEventI => Unit = ev => {
            e("change")
            T.setState(ev.target.value)
          }
          input(value := T.state, ref := inputRef, onFocus --> e("focus"), onChange ==> chg, onBlur --> e("blur"))
        }).buildU
        val c = ReactTestUtils.renderIntoDocument(C())
        val i = inputRef(c).get
        Simulation.focusChangeBlur("good") run i
        events mustEqual Vector("focus", "change", "blur")
        i.getDOMNode().value mustEqual "good"
      }
      'targetByName {
        val c = ReactTestUtils.renderIntoDocument(IC())
        var count = 0
        def tgt = {
          count += 1
          Sel("input").findIn(c)
        }
        Simulation.focusChangeBlur("-") run tgt
        assert(count == 3)
      }
    }
  }
}