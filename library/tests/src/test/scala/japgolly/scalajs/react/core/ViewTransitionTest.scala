package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import scala.scalajs.js
import utest._

object ViewTransitionTest extends TestSuite {
  japgolly.scalajs.react.test.InitTestEnv()

  override def tests = Tests {
    "basic" - assertRender(React.ViewTransition(3), "3")
    "props" - {
      val ref = Ref.toViewTransition()
      assertRender(React.ViewTransition
        .enter("test")
        .onEnter_((_, _) => Callback.log("Entered"))
        .withRef(ref)
        (123), "123")
    }
  }
}
