package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.LegacyReactTestUtils
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom._
import utest._

object EventListenerTest extends TestSuite {

  val C = ScalaComponent.builder[Unit]("")
    .initialState(0)
    .backend(_ => OnUnmount())
    .renderS((_, state) => <.div(s"Hit $state times"))
    .configure(EventListener.install("hello", _.modState(_ + 1)))
    .build

  override def tests = Tests {
    val c = LegacyReactTestUtils.renderIntoDocument(C())

    def dispatch(name: String) = {
      val args: EventInit = new EventInit{}
      args.bubbles = true
      args.cancelable = true
      val e = new Event(name, args)
      c.getDOMNode.asMounted().asElement() dispatchEvent e
    }

    assertEq(c.state, 0)
    dispatch("xx")
    assertEq(c.state, 0)
    dispatch("hello")
    assertEq(c.state, 1)
  }
}
