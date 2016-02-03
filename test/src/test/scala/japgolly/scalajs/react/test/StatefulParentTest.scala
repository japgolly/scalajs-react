package japgolly.scalajs.react.test

import utest._
import japgolly.scalajs.react._
import vdom.prefix_<^._
import TestUtil2._

object StatefulParentTest extends TestSuite {

  val I = ReactComponentB[(CompState.WriteAccess[Int], Int)]("I")
    .render_P { case (w, i) =>
      <.div(
        <.div("state = ", <.span(i)),
        <.button("inc", ^.onClick --> w.modState(_ + 1)) // weird here - just an example
      )
    }
    .build

  val O = StatefulParent[Int](($, i) => I(($, i)))

  override def tests = TestSuite {
    val c = ReactTestUtils renderIntoDocument O(3)
    def state = ReactTestUtils.findRenderedDOMComponentWithTag(c, "span").getDOMNode().innerHTML.toInt
    def button = ReactTestUtils.findRenderedDOMComponentWithTag(c, "button")
    assertEq(state, 3)
    ReactTestUtils.Simulate click button
    assertEq(state, 4)
  }
}
