package japgolly.scalajs.react.extra

import org.scalajs.dom.document
import utest._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._

object EventListenerTest extends TestSuite {

  val C = ScalaComponent.builder[Unit]("")
    .initialState(0)
    .backend(_ => new OnUnmount.Backend)
    .renderS((_, state) => <.div(s"Hit $state times"))
    .configure(EventListener.install("hello", _.modState(_ + 1)))
    .build

  override def tests = Tests {
    val c = ReactTestUtils.renderIntoDocument(C())

    def dispatch(name: String) = {
      val e = document.createEvent("Event")
      e.initEvent(name, true, true)
      c.getDOMNode.asMounted().asElement() dispatchEvent e
    }

    assertEq(c.state, 0)
    dispatch("xx")
    assertEq(c.state, 0)
    dispatch("hello")
    assertEq(c.state, 1)
  }
}
