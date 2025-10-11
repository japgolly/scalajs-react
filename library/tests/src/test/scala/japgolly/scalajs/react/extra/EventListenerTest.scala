package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils2
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
    ReactTestUtils2.withRenderedSync(C()) { t =>

      def dispatch(name: String) = {
        val args: EventInit = new EventInit{}
        args.bubbles = true
        args.cancelable = true
        val e = new Event(name, args)
        ReactTestUtils2.actSync(t.asElement() dispatchEvent e)
      }

      t.outerHTML.assert("<div>Hit 0 times</div>")
      dispatch("xx")
      t.outerHTML.assert("<div>Hit 0 times</div>")
      dispatch("hello")
      t.outerHTML.assert("<div>Hit 1 times</div>")
    }
  }
}
