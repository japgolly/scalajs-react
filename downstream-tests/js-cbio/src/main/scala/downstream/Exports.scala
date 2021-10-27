package downstream

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import scala.scalajs.js.annotation._

@JSExportTopLevel("EXPORTS")
object Exports {

  @JSExport
  def exp = List[Any](
    Routed.Component,
    pxToCallback,
  )

  private def pxToCallback: CallbackTo[Int] = {
    val x = Px.constByNeed(123).toCallback
    x
  }
}
