package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils2
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import utest._

object OnUnmountTest extends TestSuite {

  var i = 0

  val dec_i = Callback(i -= 1)
  val inc_i = Callback(i += 1)

  val C = ScalaComponent.builder[Unit]("")
    .backend(_ => OnUnmount())
    .renderStatic(<.div)
    .configure(OnUnmount.install)
    .componentDidMount(_.backend onUnmount dec_i)
    .componentDidMountConst(inc_i)
    .build

  override def tests = Tests {
    ReactTestUtils2.withReactRootSync { r =>
      r.renderSync(C())
      assertEq(i, 1)
      r.renderSync(())
      assertEq(i, 0)
    }
  }
}
