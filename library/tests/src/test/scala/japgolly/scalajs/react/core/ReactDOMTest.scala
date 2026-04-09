package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import utest._

object ReactDOMTest extends TestSuite {
  japgolly.scalajs.react.test.InitTestEnv()

  override def tests = Tests {

    "requestFormReset" - {
      val inputRef = Ref[dom.html.Input]
      val formRef  = Ref[dom.html.Form]
      val comp = ScalaFnComponent[Unit] { _ =>
        <.form.withRef(formRef)(
          <.input.withRef(inputRef)(^.defaultValue := "initial")
        )
      }
      withRenderedSync(comp()) { _ =>
        val input = inputRef.unsafeGet()
        val form  = formRef.unsafeGet()
        input.value = "changed"
        assertEq(input.value, "changed")
        actSync(React.startTransition(Callback(ReactDOM.requestFormReset(form))).runNow())
        assertEq(input.value, "initial")
      }
    }

    "prefetchDNS" - ReactDOM.prefetchDNS("https://example.com")
    "preconnect"  - ReactDOM.preconnect("https://example.com", crossOrigin = "anonymous")
    "preload"     - ReactDOM.preload("data:text/javascript,console.log('hi')", "script", crossOrigin = "anonymous")
    "preinit"     - ReactDOM.preinit("data:text/css,.foo{color:red}", "style", crossOrigin = "anonymous", precedence = "med")

  }
}
