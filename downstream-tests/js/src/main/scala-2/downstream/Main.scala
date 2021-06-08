package downstream

import japgolly.microlibs.compiletime.CompileTimeInfo
import scala.scalajs.js.annotation._

@JSExportTopLevel("MAIN")
object Main {

  def reusabilityDev = CompileTimeInfo.sysProp("downstream_tests.reusability.dev")

  @JSExport
  def init() = {

    DownstreamMacros.mainInit

    // Init components (for RuntimeTests)
    // Reference components (for JsOutputTest)
    List[Any](
      Carrot,
      Pumpkin,
    )
  }
}
