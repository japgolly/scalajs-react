package downstream

import cats.effect.SyncIO
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Catnip {

  final class Backend($: BackendScope[String, Unit]) {
    def render(p: String) =
      <.div("Hello ", p)

    def onMount: SyncIO[Unit] =
      $.props.map { p =>
        Globals.catnipMounts :+= p
      }
  }

  val Component = ScalaComponent.builder[String]
    .backend(new Backend(_))
    .renderP(_.backend.render(_))
    .componentDidMount(_.backend.onMount)
    .build
}
