package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import utest._

object ReactDOMServerTest extends TestSuite {
  japgolly.scalajs.react.test.InitTestEnv()

  override def tests = Tests {
    "vdomNode" - {
      val result = ReactDOMServer.renderToStaticMarkup(123)
      result ==> "123"
    }
  }
}
