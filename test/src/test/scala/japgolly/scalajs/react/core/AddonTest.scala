package japgolly.scalajs.react.core

import org.scalajs.dom.raw.HTMLElement
import utest._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test.JsEnvUtils.requiresRealBrowser
import japgolly.scalajs.react.test.ReactTestUtils
import ReactAddons._

object AddonTest extends TestSuite {

  lazy val componentA = ScalaComponent.build[Int]("A")
    .initialState_P(identity)
    .render_S(i => <.div(
      (0 to i).map(j => componentB(s"$j² = ${j*j}")).toReactArray
    ))
    .build

  lazy val componentB = ScalaComponent.build[String]("B")
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
