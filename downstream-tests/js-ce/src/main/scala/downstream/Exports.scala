package downstream

import cats.effect.SyncIO
import japgolly.scalajs.react.extra._
import scala.scalajs.js.annotation._

@JSExportTopLevel("EXPORTS")
object Exports {

  @JSExport
  def exp = List[Any](
    pxToCallback,
  )

  private def pxToCallback: SyncIO[Int] = {
    val x = Px.constByNeed(123).toCallback
    x
  }
}
