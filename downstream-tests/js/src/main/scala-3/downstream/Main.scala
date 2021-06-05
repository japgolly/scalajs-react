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
    enableReusabilityOverlay()

    // Init components (for RuntimeTests)
    // Reference components (for JsOutputTest)
    List[Any](
      Carrot,
      Pumpkin,
    )
  }

  private inline def disableReusability(): Unit =
    inline reusabilityDev match {
      case Some("disable") => Reusability.disableGloballyInDev()
      case _               => ()
    }

  private inline def enableReusabilityOverlay(): Unit =
    inline reusabilityDev match {
      case Some("overlay") => ReusabilityOverlay.overrideGloballyInDev()
      case _               => ()
    }
}
