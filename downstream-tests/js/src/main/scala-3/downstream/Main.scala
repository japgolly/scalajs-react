package downstream

import japgolly.microlibs.compiletime.CompileTimeInfo
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import scala.scalajs.js.annotation._

@JSExportTopLevel("MAIN")
object Main {

  transparent inline def reusabilityDev = CompileTimeInfo.sysProp("downstream_tests.reusability.dev")

  @JSExport
  def init() = {
    disableReusability()
    Exports.components
  }

  private inline def disableReusability(): Unit =
    inline reusabilityDev match {
      case Some("disable") => Reusability.disableGloballyInDev()
      case _               => ()
    }
}
