package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._, vdom.html_<^._
import japgolly.scalajs.react.extra._

object ReuseExample {

  def content = SingleSide.Content(source, main())

  lazy val main = addIntro(topLevelComponent.withKey(_)(), _(
    ^.marginBottom := "2.4em",
    "The colourful overlays here are provided by ",
    <.code("ReusabilityOverlay.install"),
    " and demonstrate how many updates are prevented vs rendered in each component.",
    <.br,
    "Hover for more info. Click one to print detail to the JS console."))

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START
  // Reusable stateless components

  val showSum = ScalaComponent.builder[Long]("Show sum")
    .render_P(sum =>
      <.h1(
        "The sum of all inputs is", <.br, sum))
    .configure(ReusabilityOverlay.install)
    .build

  case class InputControl(current: Int, change: Int ~=> Callback)
  implicit val inputControlReuse = Reusability.derive[InputControl]

  val inputControl = ScalaComponent.builder[InputControl]("InputControl")
    .render_P(p =>
      <.div(^.paddingLeft := "4ex",
        <.button("-1", ^.onClick --> p.change(-1)),
        <.span(^.padding := "0 1ex", p.current),
        <.button("+1", ^.onClick --> p.change(1)))
    )
    .configure(ReusabilityOverlay.install)
    .build

  val numberRegex = "^-?\\d+$".r

  val InputEditor = ScalaComponent.builder[StateSnapshot[Long]]("Input editor")
    .render_P { v =>
      def update = (ev: ReactEventFromInput) => numberRegex.findFirstIn(ev.target.value).map(v setState _.toLong)
      <.input.text(
        ^.textAlign   := "center",
        ^.marginRight := "1ex",
        ^.width       := "12ex",
        ^.value       := v.value.toString,
        ^.onChange  ==>? update)
    }
    .configure(ReusabilityOverlay.install)
    .build

  // ---------------------------------------------------------------------------------------------------------
  // Top-level stateful component

  val topLevelComponent = ScalaComponent.builder[Unit]("Reusability example")
    .initialState(State(Vector(30, 0, 2, 0, 10)))
    .renderBackend[Backend]
    .build

  case class State(inputs: Vector[Long]) {
    def changeNumberOfInputs(delta: Int) = State(
      if (delta >= 0)
        inputs ++ Vector.fill(delta)(inputs.size % 2: Long)
      else
        inputs dropRight (-delta).min(inputs.length - 1)
    )

    def setInput(index: Int, value: Long): State =
      if (index >= 0 && index < inputs.length)
        State(inputs.updated(index, value))
      else
        this

    val sum = inputs.sum
  }

  class Backend($: BackendScope[Unit, State]) {
    val changeFn = Reusable.fn((i: Int) => $.modState(_.changeNumberOfInputs(i)))

    val setInputFn = Reusable.fn { (i: Int, setStateArgs: (Option[Long], Callback)) =>
      val (value, cb) = setStateArgs
      $.modStateOption(s => value.map(s.setInput(i, _)), cb)
    }

    def render(s: State) = {
      def inputEditor(index: Int) = {
        val value = s.inputs(index)
        val rvar = StateSnapshot.withReuse(value).tupled(setInputFn(index))
        InputEditor.withKey(index)(rvar)
      }

      <.div(
        <.h4("Number of inputs:"),
        inputControl(InputControl(s.inputs.size, changeFn)),
        <.h4("Inputs:"),
        Array.tabulate(s.inputs.length)(inputEditor).toVdomArray,
        showSum.withKey("sum")(s.sum))
    }
  }
  // EXAMPLE:END
}
