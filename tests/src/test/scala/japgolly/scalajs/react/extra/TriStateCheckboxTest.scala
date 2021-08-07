package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.components.TriStateCheckbox
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test.ReactTestUtils._
import utest._
import japgolly.scalajs.react.test._

object TriStateCheckboxTest extends TestSuite {

  private object OnKeyDown {

    final class Backend($: BackendScope[Unit, Int]) {
      val onKey: ReactEvent => Callback =
        e => $.modState(_ + 1).unless_(e.defaultPrevented)
      def render(s: Int): VdomNode =
        <.div(
          s"s=$s",
          ^.onKeyDown ==> onKey,
          TriStateCheckbox.Props(TriStateCheckbox.Checked, Callback.empty).render
        )
    }

    val Component = ScalaComponent.builder[Unit]
      .initialState(0)
      .renderBackend[Backend]
      .build

    def test() = {
      withRenderedIntoBody(Component()).withParent { p =>
        assertEq(p.textContent, "s=0")
        val i = p.querySelector("input")

        SimEvent.Keyboard.Space.simulateKeyDown(i)
        assertEq(p.textContent, "s=0")

        SimEvent.Keyboard.A.simulateKeyDown(i)
        assertEq(p.textContent, "s=1")
      }
    }
  }

  override def tests = Tests {
    "onKeyDown" - OnKeyDown.test()
  }
}
