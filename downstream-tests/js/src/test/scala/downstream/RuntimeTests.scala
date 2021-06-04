package downstream

import japgolly.microlibs.compiletime.CompileTimeInfo
import japgolly.microlibs.testutil.TestUtil._
import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils._
import utest._

object RuntimeTests extends TestSuite {

  val configClass  = CompileTimeInfo.sysProp(ScalaJsReactConfig.KeyConfigClass)
  val compNameAuto = CompileTimeInfo.sysProp(ScalaJsReactConfig.KeyCompNameAuto)
  val compNameAll  = CompileTimeInfo.sysProp(ScalaJsReactConfig.KeyCompNameAll)

  override def tests = Tests {
    // Force obejct initialisation
    Carrot
    Pumpkin

    "reusabilityOverride" - {
      Globals.clear()
      if (configClass.isEmpty) {

        // Expect standard Reusability
        assertEq(Globals.reusabilityLog.length, 0)
        withRenderedIntoDocument(Pumpkin.Component("1")) { m =>
          replaceProps(Pumpkin.Component, m)("1")
          replaceProps(Pumpkin.Component, m)("2")
        }
        assertEq(Globals.pumpkinRenders, 2)

      } else {

        // Expect custom Reusability
        assertEq(Globals.reusabilityLog.length, 1)
        withRenderedIntoDocument(Pumpkin.Component("1")) { m =>
          replaceProps(Pumpkin.Component, m)("1")
          replaceProps(Pumpkin.Component, m)("2")
        }
        assertEq(Globals.pumpkinRenders, 3)
      }

      s"ReusabilityLog: ${Globals.reusabilityLog.length}"
    }
  }
}
