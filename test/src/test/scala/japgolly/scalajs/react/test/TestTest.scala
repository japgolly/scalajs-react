package japgolly.scalajs.react.test

import scala.scalajs.js
import utest._
import japgolly.scalajs.react._
import vdom.ReactVDom._
import all._

object TestTest extends TestSuite {

  lazy val A = ReactComponentB[Unit]("A").render((_,c) => p(cls := "AA", c)).createU
  lazy val B = ReactComponentB[Unit]("B").render(_ => p(cls := "BB", "hehehe")).createU
  lazy val rab = ReactTestUtils.renderIntoDocument(A(B()))

  val tests = TestSuite {
    'isTextComponent {
      val r = ReactTestUtils.isTextComponent(A())
      assert(!r)
    }

    if (!js.isUndefined(js.Dynamic.global.document)) {

      'findRenderedDOMComponentWithClass {
        val n = ReactTestUtils.findRenderedDOMComponentWithClass(rab, "BB").getDOMNode()
        assert(n.className == "BB")
      }

      'findRenderedComponentWithType {
        val n = ReactTestUtils.findRenderedComponentWithType(rab, B).getDOMNode()
        assert(n.className == "BB")
      }
    }
  }
}
