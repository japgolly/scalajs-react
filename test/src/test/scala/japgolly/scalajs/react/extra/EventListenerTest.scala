package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.Event
import utest._
import TestUtil._

object EventListenerTest extends TestSuite {

  val C = ReactComponentB[Unit]("")
    .initialState(0)
    .backend(_ => new OnUnmount.Backend)
    .renderS((_, state) => <.div(s"Hit $state times"))
    .configure(EventListener.install("hello", _.modState(_ + 1)))
    .buildU

  override def tests = TestSuite {
    val c = ReactTestUtils.renderIntoDocument(C())

    def dispatch(name: String) = {
      val e = new Event
      e.initEvent(name, true, true)
      ReactDOM findDOMNode c dispatchEvent e
    }

    c.state mustEqual 0
    dispatch("xx")
    c.state mustEqual 0
    dispatch("hello")
    c.state mustEqual 1
  }
}
