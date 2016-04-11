package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._, vdom.prefix_<^._

object CallbackOptionExample {

  def content = SingleSide.Content(source, Main2())

  val source = GhPagesMacros.exampleSource

  def Main2 = ReactComponentB[Unit]("CallbackOption example")
    .render(_ =>
      <.div(
        <.p(
          <.code("CallbackOption"), " is a ", <.code("Callback"), " that you can compose so that steps can abort the rest of the process.",
          <.br,
          "It makes it easy to work with conditions."),
        <.br,
        <.p(
          "Press ←↑↓→ to move the box. Hold ctrl to move to the edges.",
          <.br,
          "Notice that PageDown still scrolls the page but ↓ doesn't? That's because", <.code("preventDefault"), "is only called when a key is matched."),
        Main()))
    .build

  // EXAMPLE:START
  import org.scalajs.dom.ext.KeyCode

  val OuterX    = 600
  val OuterY    = 240
  val InnerSize =  24
  val MoveDist  =  24

  case class State(x: Int, y: Int)

  def initState = State((OuterX - InnerSize) / 2, (OuterY - InnerSize) / 2)

  val OuterRef = Ref("o")

  val OuterDiv =
    <.div(
      ^.ref        := OuterRef,
      ^.tabIndex   := 0,
      ^.width      := OuterX.px,
      ^.height     := OuterY.px,
      ^.border     := "solid 1px #333",
      ^.background := "#ddd")

  val InnerDiv =
    <.div(
      ^.position.relative,
      ^.width      := InnerSize.px,
      ^.height     := InnerSize.px,
      ^.background := "#800")

  def moveOneAxis(pos: Int, steps: Int, max: Int): Int =
    (pos + steps * MoveDist) min (max - InnerSize) max 0

  class Backend($: BackendScope[Unit, State]) {
    def init: Callback =
      OuterRef($).tryFocus

    def move(dx: Int, dy: Int): Callback =
      $.modState(s => s.copy(
        x = moveOneAxis(s.x, dx, OuterX),
        y = moveOneAxis(s.y, dy, OuterY)))

    def handleKey(e: ReactKeyboardEvent): Callback = {

      def plainKey: CallbackOption[Unit] =             // CallbackOption will stop if a key isn't matched
        CallbackOption.keyCodeSwitch(e) {
          case KeyCode.Up    => move(0, -1)
          case KeyCode.Down  => move(0,  1)
          case KeyCode.Left  => move(-1, 0)
          case KeyCode.Right => move( 1, 0)
        }

      def ctrlKey: CallbackOption[Unit] =              // Like above but if ctrlKey is pressed
        CallbackOption.keyCodeSwitch(e, ctrlKey = true) {
          case KeyCode.Up    => move(0, -OuterY)
          case KeyCode.Down  => move(0,  OuterY)
          case KeyCode.Left  => move(-OuterX, 0)
          case KeyCode.Right => move( OuterX, 0)
        }

      (plainKey orElse ctrlKey) >> e.preventDefaultCB  // This is the interesting part.
                                                       //
                                                       // orElse joins CallbackOptions so if one fails, it tries the other.
                                                       //
                                                       // The >> means "and then run" but only if the left side passes.
                                                       // This means preventDefault only runs if a valid key is pressed.
    }

    def render(s: State) =
      OuterDiv(
        ^.onKeyDown ==> handleKey,
        InnerDiv(
          ^.left := s.x.px,
          ^.top  := s.y.px))
  }

  val Main = ReactComponentB[Unit]("CallbackOption example")
    .initialState(initState)
    .renderBackend[Backend]
    .componentDidMount(_.backend.init)
    .build

  // EXAMPLE:END
}
