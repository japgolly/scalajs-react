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
    Carrot.Component,
    Pumpkin.Component,
    Routed.Component,
  )

  @JSExport
  def exp = List[Any](
    BaseUrl("http://some.url"),
    modStateFnPure,
    pxToCallback,
    Px.constByNeed(123).toCallback[SyncIO],
  )

  private def modStateFnPure: ModStateFnPure[Int] =
    ModStateFn((mod, cb) => Callback.log(mod(1).isDefined) >> cb)

  private def pxToCallback: CallbackTo[Int] = {
    val x = Px.constByNeed(123).toCallback
    x
  }
}
