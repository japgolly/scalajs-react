package downstream

import cats.effect.SyncIO
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router.BaseUrl
import scala.scalajs.js.annotation._

@JSExportTopLevel("EXPORTS")
object Exports {

  // Init components (for RuntimeTests)
  // Reference components (for JsOutputTest)
  def components = List[Any](
    Carrot,
    Pumpkin,
  )

  @JSExport
  def exp = List[Any](
    BaseUrl("http://some.url"),
    pxToCallback,
    Px.constByNeed(123).toCallback[SyncIO],
  )

  private def pxToCallback: CallbackTo[Int] = {
    val x = Px.constByNeed(123).toCallback
    x
  }
}
