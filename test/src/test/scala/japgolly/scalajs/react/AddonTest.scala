package japgolly.scalajs.react

import vdom.prefix_<^._
import utest._
import test._
import Addons._

object AddonTest extends TestSuite {

  val componentA = ReactComponentB[Int]("A")
    .getInitialState(identity)
    .render((_, _, i) => <.div(
      (0 to i).map(j => componentB(s"$j² = ${j*j}"))
    ))
    .build

  val componentB = ReactComponentB[String]("B")
    .stateless
    .render((s, _) => <.div("Input is ", s))
    .build

  override def tests = TestSuite {
    'perf {
      val c = ReactTestUtils renderIntoDocument componentA(10)
      Perf.start()
      c.setState(20)
      c.setState(5)
      Perf.stop()
      val m = Perf.getLastMeasurements()
      assert(m.length == 2)
      DebugJs.inspectObject(m(0))
    }
  }
}
