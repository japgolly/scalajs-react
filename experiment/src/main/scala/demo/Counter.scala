package demo

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Counter {
  type Props = Unit
  type State = Int

  final class Backend($: BackendScope[Props, State]) {
    def render(s: State): VdomNode =
      <.button(
        "Count: ", s,
        ^.onClick --> $.modState(_ + 1),
      )
  }

  val Component = ScalaComponent.builder[Props]
    .initialState(0)
    .renderBackend[Backend]
    .configure(Reusability.shouldComponentUpdate)
    .build
}