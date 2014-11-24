package japgolly.scalajs.react.test

import org.scalajs.dom.HTMLInputElement
import utest._
import japgolly.scalajs.react._
import vdom.ReactVDom._
import vdom.ReactVDom.all._
import TestUtil._

object TestTest extends TestSuite {

  lazy val A = ReactComponentB[Unit]("A").render((_,c) => p(cls := "AA", c)).buildU
  lazy val B = ReactComponentB[Unit]("B").render(_ => p(cls := "BB", "hehehe")).buildU
  lazy val rab = ReactTestUtils.renderIntoDocument(A(B()))

  val inputRef = Ref[HTMLInputElement]("r")
  lazy val IC = ReactComponentB[Unit]("IC").initialState(true).renderS((t,_,s) => {
    val ch = (e: ReactEvent) => t.modState(x => !x)
    label(
      input(`type` := "checkbox", checked := s, onclick ==> ch, ref := inputRef),
      span(s"s = $s")
    )
  }).buildU

  lazy val IT = ReactComponentB[Unit]("IT").initialState("NIL").renderS((t,_,s) => {
    val ch = (e: SyntheticEvent[HTMLInputElement]) => t.setState(e.target.value.toUpperCase)
    input(`type` := "text", value := s, onchange ==> ch)
  }).buildU

  val tests = TestSuite {
    'isTextComponent {
      val r = ReactTestUtils.isTextComponent(A())
      assert(!r)
    }

    'findRenderedDOMComponentWithClass {
      val n = ReactTestUtils.findRenderedDOMComponentWithClass(rab, "BB").getDOMNode()
      assert(n.className == "BB")
    }

    'findRenderedComponentWithType {
      val n = ReactTestUtils.findRenderedComponentWithType(rab, B).getDOMNode()
      assert(n.className == "BB")
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
          input(value := T.state, ref := inputRef, onfocus --> e("focus"), onchange ==> chg, onblur --> e("blur"))
        }).buildU
        val c = ReactTestUtils.renderIntoDocument(C())
        val i = inputRef(c).get
        Simulation.focusChangeBlur("good") run i
        events mustEqual Vector("focus", "change", "blur")
        i.getDOMNode().value mustEqual "good"
      }
    }
  }
}