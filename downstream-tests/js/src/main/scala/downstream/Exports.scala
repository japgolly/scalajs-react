package downstream

import japgolly.scalajs.react.extra.router.BaseUrl
import scala.scalajs.js.annotation._

@JSExportTopLevel("EXPORTS")
object Exports {
  @JSExport
  def exp = List[Any](
    BaseUrl("http://some.url"),
  )
}
