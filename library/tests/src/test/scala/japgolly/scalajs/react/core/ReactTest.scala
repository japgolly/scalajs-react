package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import scala.scalajs.js
import utest._

object ReactTest extends AsyncTestSuite {
  japgolly.scalajs.react.test.InitTestEnv()

  // I don't think this would ever be used from idiomatic scalajs-react code
  // which is why it uses the facade to Suspense rather than the scalajs-react way of using Suspense.
  private def testUseAsyncFulfilled() = {
    val promise = AsyncCallback.pure(123)
    val comp = ScalaFnComponent[Unit] { _ =>
      val result = React.use(promise)
      <.div(result)
    }
    val props = js.Dynamic.literal(fallback = "Loading...").asInstanceOf[facade.SuspenseProps]
    val susp = VdomElement(facade.React.createElement(facade.Suspense, props, comp().rawNode))
    ReactTestUtils.withRendered(susp) { t =>
      for {
        _ <- AsyncCallback.unit.delayMs(1)
        _ <- AsyncCallback.delay(t.outerHTML.assert("<div>123</div>"))
      } yield ()
    }
  }

  private def testUseContext() = {
    val Ctx = React.createContext("default")
    val comp = ScalaFnComponent[Unit] { _ =>
      val result = React.use(Ctx)
      <.div(result)
    }
    ReactTestUtils.withRenderedSync(comp()) { t =>
      t.outerHTML.assert("<div>default</div>")
    }
  }

  override def tests = Tests {
    "use" - {
      "async_fulfilled" - testUseAsyncFulfilled()
      "context" - testUseContext()
    }
  }
}
