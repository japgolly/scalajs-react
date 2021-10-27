package downstream

import cats.effect.IO
import japgolly.microlibs.compiletime.CompileTimeInfo
import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.test.ReactTestUtils._
import japgolly.scalajs.react.util.JsUtil
import org.scalajs.dom.console
import scala.scalajs.js
import scala.scalajs.LinkingInfo.developmentMode
import scala.util.Try
import utest._
import japgolly.scalajs.react.test.ReactTestUtils

object RuntimeTests extends TestSuite {

  val compNameAuto      = CompileTimeInfo.sysProp("japgolly.scalajs.react.component.names.implicit")
  val compNameAll       = CompileTimeInfo.sysProp("japgolly.scalajs.react.component.names.all")
  val configClass       = CompileTimeInfo.sysProp("japgolly.scalajs.react.config.class")
  val testWarningsReact = CompileTimeInfo.sysProp("japgolly.scalajs.react.test.warnings.react")

  val dsCfg1 = configClass.contains("downstream.DownstreamConfig1")
  val dsCfg2 = configClass.contains("downstream.DownstreamConfig2")
  val dsCfg3 = configClass.contains("downstream.DownstreamConfig3")

  import Main.reusabilityDev

  // Pre-test initialisation
  locally {
    assert(!Globals.componentInitStarted())

    // Polyfill window.requestAnimationFrame for ReusabilityOverlay
    js.Dynamic.global.window.requestAnimationFrame = ((() => 0): js.Function0[Int])

    Main.init()
  }

  override def tests = Tests {
    Globals.clear()

    "reusabilityOverride" - {
      val expectedReusabilityLog = if (configClass.isEmpty) 0 else 2
      val reusabilityAllowed     = !(developmentMode && reusabilityDev.contains("disable"))

      val expectedCarrots =
        (configClass.isEmpty, dsCfg1, reusabilityAllowed) match {
          case (true , _    , true ) => 2 // default
          case (true , _    , false) => 3 // default with disableGloballyInDev
          case (_    , true , _    ) => 2 // custom1 only allows reusability for Carrot
          case (false, _    , _    ) => 3 // custom2+ disable reusability
        }

      val expectedPumpkins =
        (configClass.isEmpty, dsCfg1, reusabilityAllowed) match {
          case (true , _    , true ) => 2 // default
          case (true , _    , false) => 3 // default with disableGloballyInDev
          case (_    , true , _    ) => 3 // custom1 only allows reusability for Carrot
          case (false, _    , _    ) => 3 // custom2+ disable reusability
        }

      assertEq(Globals.reusabilityLog.length, expectedReusabilityLog)

      val (promise, completePromise) = JsUtil.newPromise[Unit]()
      val io = IO(completePromise(Try(()))())

      withRenderedIntoDocument(Carrot.Props("1", io).render) { m =>
        replaceProps(Carrot.Component, m)(Carrot.Props("1"))
        replaceProps(Carrot.Component, m)(Carrot.Props("2"))
      }
      withRenderedIntoDocument(Pumpkin.Component("1")) { m =>
        replaceProps(Pumpkin.Component, m)("1")
        replaceProps(Pumpkin.Component, m)("2")
      }

      assertEq(Globals.carrotMountsA, 1)
      assertEq(Globals.carrotMountsB, 1)
      assertEq(Globals.carrotRenders, expectedCarrots)
      assertEq(Globals.pumpkinRenders, expectedPumpkins)

      AsyncCallback
        .fromJsPromise(promise)
        .map(_ => s"carrots: ${Globals.carrotRenders}/3, pumpkins: ${Globals.pumpkinRenders}/3, reusabilityLog: ${Globals.reusabilityLog.length}")
        .timeoutMs(3000)
        .map(_.get)
        .unsafeToFuture()
    }

    "testWarnings" - {

      "react" - {
        val c = ScalaFnComponent[Int](i => <.p(<.td(s"i = $i")))
        val t = Try(ReactTestUtils.withRenderedIntoBody(c(123))(_ => ()))
        assertEq(t.isFailure, testWarningsReact.contains("react"))
      }

      "unlreated" - {
        val c = ScalaFnComponent[Int](i => <.p(s"i = $i"))
        val t = Try(ReactTestUtils.withRenderedIntoBody(c(123)) { _ =>
          console.info(".")
          console.log(".")
          console.warn(".")
          console.error(".")
        })
        assertEq(t.isFailure, false)
      }
    }
  }
}
