package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import scalaz.effect.IO
import japgolly.scalajs.react._, vdom.prefix_<^._, ScalazReact._
import japgolly.scalajs.react.extra._

object ReuseExample {

  def content = SingleSide.Content(source, main())

  lazy val main = addIntro(topLevelComponent, _(
    ^.marginBottom := "2.4em",
    "The colourful overlays here are provided by ",
    <.code("Reusability.shouldComponentUpdateWithOverlay"),
    " and demonstrate how many updates are prevented vs rendered in each component.",
    <.br,
    "Hover for more info. Click one to print detail to the JS console."))

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START
  // Reusable stateless components

  val showSum = ReactComponentB[Long]("Show sum")
    .render(sum =>
      <.h1(
        "The sum of all inputs is", <.br, sum))
    .configure(Reusability.shouldComponentUpdateWithOverlay)
    .build

  case class InputControl(current: Int, change: Int ~=> IO[Unit])
  implicit val inputControlReuse = Reusability.caseClass[InputControl]

  val inputControl = ReactComponentB[InputControl]("InputControl")
    .render(p =>
      <.div(^.paddingLeft := "4ex",
        <.button("-1", ^.onClick ~~> p.change(-1)),
        <.span(^.padding := "0 1ex", p.current),
        <.button("+1", ^.onClick ~~> p.change(1)))
    )
    .configure(Reusability.shouldComponentUpdateWithOverlay)
    .build

  val numberRegex = "^-?\\d+$".r

  val InputEditor = ReactComponentB[ReusableVar[Long]]("Input editor")
    .render { v =>
      def update = (ev: ReactEventI) => numberRegex.findFirstIn(ev.target.value).map(v set _.toLong)
      <.input(
        ^.textAlign   := "center",
        ^.marginRight := "1ex",
        ^.width       := "12ex",
        ^.`type`      := "text",
        ^.value       := v.value.toString,
        ^.onChange  ~~>? update)
    }
    .configure(Reusability.shouldComponentUpdateWithOverlay)
    .build

  // ---------------------------------------------------------------------------------------------------------
  // Top-level stateful component

  val topLevelComponent = ReactComponentB[Unit]("Reusability example")
    .initialState(State(Vector(30, 0, 2, 0, 10)))
    .backend(new Backend(_))
    .render(_.backend.render)
    .buildU

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
    val changeFn   = ReusableFn($).modStateIO.endoCall(_.changeNumberOfInputs)
    val setInputFn = ReusableFn($).modStateIO.endoCall2(_.setInput)

    def render = {
      val s = $.state

      def inputEditor(index: Int) = {
        val value = s.inputs(index)
        val rvar = ReusableVar(value)(setInputFn(index))
        InputEditor.withKey(index)(rvar)
      }

      <.div(
        <.h4("Number of inputs:"),
        inputControl(InputControl(s.inputs.size, changeFn)),
        <.h4("Inputs:"),
        Array.tabulate(s.inputs.length)(inputEditor),
        showSum.withKey("sum")(s.sum))
    }
  }
  // EXAMPLE:END
}
