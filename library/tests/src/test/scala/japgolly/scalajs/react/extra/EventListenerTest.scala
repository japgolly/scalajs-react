package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom._
import utest._

object EventListenerTest extends TestSuite {
  japgolly.scalajs.react.test.InitTestEnv()

  private class Backend() extends OnUnmount {
    val ref = Ref.toVdom[html.Div]
    def render(state: Int) = <.div.withRef(ref)(s"Hit $state times")
  }

  private val C = ScalaComponent.builder[Unit]("")
    .initialState(0)
    .backend(_ => new Backend())
    .renderS(_.backend.render(_))
    .configure(EventListener.install2(
      "hello",
      _.modState(_ + 1),
      _.backend.ref.get.runNow().get
    ))
    .build

  override def tests = Tests {
    ReactTestUtils.withRenderedSync(C()) { t =>

      def dispatch(name: String) = {
        val args: EventInit = new EventInit{}
        args.bubbles = true
        args.cancelable = true
        val e = new Event(name, args)
        ReactTestUtils.actSync(t.asElement() dispatchEvent e)
      }

      t.outerHTML.assert("<div>Hit 0 times</div>")
      dispatch("xx")
      t.outerHTML.assert("<div>Hit 0 times</div>")
      dispatch("hello")
      t.outerHTML.assert("<div>Hit 1 times</div>")
    }
  }
}
