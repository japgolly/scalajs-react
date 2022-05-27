package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.LegacyReactTestUtils
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

  val Outer = ScalaComponent.builder[Unit]("")
    .initialState(true)
    .render_S(s => if (s) C() else <.div)
    .build

  override def tests = Tests {
    val c = LegacyReactTestUtils.renderIntoDocument(Outer())
    assert(i == 1)
    c.setState(false)
    assert(i == 0)
  }
}
