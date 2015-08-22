package japgolly.scalajs.react

import vdom.prefix_<^._
import utest._
import test._
import Addons._

object AddonTest extends TestSuite {

  val componentA = ReactComponentB[Int]("A")
    .initialState_P(identity)
    .render_S(i => <.div(
      (0 to i).map(j => componentB(s"$j² = ${j*j}"))
    ))
    .build

  val componentB = ReactComponentB[String]("B")
    .render_P(str => <.div("Input is ", str))
    .build

  override def tests = TestSuite {
    'perf {
      val c = ReactTestUtils renderIntoDocument componentA(10)
      Perf.start()
      c.setState(20).runNow()
      c.setState(5).runNow()
      Perf.stop()
      val m = Perf.getLastMeasurements()
      assert(m.length == 2)
      DebugJs.inspectObject(m(0))
    }
  }
}
