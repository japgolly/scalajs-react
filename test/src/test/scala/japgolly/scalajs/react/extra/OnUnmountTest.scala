package japgolly.scalajs.react.extra

import utest._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.vdom.prefix_<^._

object OnUnmountTest extends TestSuite {

  var i = 0

  val dec_i = Callback(i -= 1)
  val inc_i = Callback(i += 1)

  val C = ReactComponentB[Unit]("")
    .stateless
    .backend(_ => new OnUnmount.Backend)
    .render(_ => <.div)
    .configure(OnUnmount.install)
    .componentDidMount(_.backend onUnmount dec_i)
    .componentDidMountCB(inc_i)
    .buildU

  val Outer = ReactComponentB[Unit]("")
    .initialState(true)
    .render((_, s) => if (s) C() else <.div)
    .buildU

  override def tests = TestSuite {
    val c = ReactTestUtils.renderIntoDocument(Outer())
    assert(i == 1)
    c.setState(false).runNow()
    assert(i == 0)
  }
}
