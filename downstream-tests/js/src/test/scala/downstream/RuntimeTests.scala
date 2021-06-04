package downstream

import japgolly.microlibs.compiletime.CompileTimeInfo
import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils._
import scala.scalajs.LinkingInfo.developmentMode
import utest._

object RuntimeTests extends TestSuite {

  val compNameAuto = CompileTimeInfo.sysProp(ScalaJsReactConfig.KeyCompNameAuto)
  val compNameAll  = CompileTimeInfo.sysProp(ScalaJsReactConfig.KeyCompNameAll)
  val configClass  = CompileTimeInfo.sysProp(ScalaJsReactConfig.KeyConfigClass)

  val dsCfg1 = configClass.contains("downstream.DownstreamConfig1")
  val dsCfg2 = configClass.contains("downstream.DownstreamConfig2")
  val dsCfg3 = configClass.contains("downstream.DownstreamConfig3")

  val reusabilityDevDisable = CompileTimeInfo.sysProp("downstream_tests.reusability.dev.disable").isDefined

  // Pre-test initialisation
  locally {
    assert(!Globals.componentInitStarted())

    if (reusabilityDevDisable)
      Reusability.disableGloballyInDev()

    // Init components
    Carrot
    Pumpkin
  }

  override def tests = Tests {

    "reusabilityOverride" - {
      Globals.clear()

      val expectedReusabilityLog = if (configClass.isEmpty) 0 else 2
      val reusabilityAllowed     = !(developmentMode && reusabilityDevDisable)

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

      withRenderedIntoDocument(Carrot.Component("1")) { m =>
        replaceProps(Carrot.Component, m)("1")
        replaceProps(Carrot.Component, m)("2")
      }
      withRenderedIntoDocument(Pumpkin.Component("1")) { m =>
        replaceProps(Pumpkin.Component, m)("1")
        replaceProps(Pumpkin.Component, m)("2")
      }

      assertEq(Globals.carrotRenders, expectedCarrots)
      assertEq(Globals.pumpkinRenders, expectedPumpkins)

      s"carrots: ${Globals.carrotRenders}/3, pumpkins: ${Globals.pumpkinRenders}/3, reusabilityLog: ${Globals.reusabilityLog.length}"
    }
  }
}
