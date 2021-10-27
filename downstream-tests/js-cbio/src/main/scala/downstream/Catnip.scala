package downstream

import cats.effect.IO
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Catnip {

  final class Backend($: BackendScope[String, Int]) {
    def render(p: String, s: Int) =
      <.div(s"Hello($s) ", p)

    def onMount: Callback =
      $.props.map { p =>
        Globals.catnipMounts :+= p
      }

    def onMount2: IO[Unit] =
      $.setStateAsync(1)
  }

  val Component = ScalaComponent.builder[String]
    .initialState(0)
    .renderBackend[Backend]
    .componentDidMount(_.backend.onMount)
    .componentDidMount(_.backend.onMount2)
    .build
}
