package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.vdom.html_<^._
import utest._

object ActivityTest extends AsyncTestSuite {
  japgolly.scalajs.react.test.InitTestEnv()

  private def testVisible() = {
    val comp = React.Activity(React.Activity.Visible)(
      <.div("Hello")
    )
    ReactTestUtils.withRenderedSync(comp) { t =>
      t.outerHTML.assert("<div>Hello</div>")
    }
  }

  private def testHidden() = {
    val comp = React.Activity(React.Activity.Hidden)(
      <.div("Hello")
    )
    ReactTestUtils.withRenderedSync(comp) { t =>
      t.outerHTML.assert("<div style=\"display: none;\">Hello</div>")
    }
  }

  override def tests = Tests {
    "visible" - testVisible()
    "hidden" - testHidden()
  }
}
