package japgolly.scalajs.react

import japgolly.scalajs.react.vdom.html_<^._

object Wtf {
  type M = Map[Int, String]

  val outerComponent = ScalaComponent.builder[M]("Demo")
    .initialStateFromProps(identity)
    .renderBackend[Backend]
    .build

  class Backend($: BackendScope[M, M]) {
    val updateUser = Reusable.fn((id: Int, data: String) =>
      $.modState(_.updated(id, data)))
    def render(s: M) = <.div
  }
}