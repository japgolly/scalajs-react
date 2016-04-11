package japgolly.scalajs.react

import org.scalajs.dom.raw.HTMLElement
import vdom.prefix_<^._
import utest._
import test._
import Addons._
import TestUtil._
import JsEnvUtils.requiresRealBrowser

object AddonTest extends TestSuite {

  lazy val componentA = ReactComponentB[Int]("A")
    .initialState_P(identity)
    .render_S(i => <.div(
      (0 to i).map(j => componentB(s"$j² = ${j*j}"))
    ))
    .build

  lazy val componentB = ReactComponentB[String]("B")
    .render_P(str => <.div("Input is ", str))
    .build

  override def tests = TestSuite {

    // Doesn't work with PhantomJS due to: "ReferenceError: Can't find variable: WeakMap"
    'perf - requiresRealBrowser {
      val c = ReactTestUtils renderIntoDocument componentA(10)
      Perf.start()
      c.setState(20)
      c.setState(5)
      Perf.stop()
//      Disabled due to https://github.com/facebook/react/pull/6286
//      val m = Perf.getLastMeasurements()
//      assert(m.length == 2)
//      DebugJs.inspectObject(m(0))
    }
  }
}
