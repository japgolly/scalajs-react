package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

object ES6IntoRCBExample {

  def title = "ES6 Component into ReactComponentB Example"

  def content = SingleSide.Content(source, main())

  lazy val main = addIntro(ES6IntoRCBApp, _(scalaPortOf("An Application")))

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START

  case class Props(text: String)

  @ScalaJSDefined
  class TodoListItemC extends ReactComponentNoState[Props, TopNode] {
    def render() = {
      <.li(props.text)
    }
  }

  val TodoListItem = ElementFactory.requiredProps(js.constructorOf[TodoListItemC], classOf[TodoListItemC])

  val TodoList = ReactComponentB[List[String]]("TodoList")
    .render_P(props => {
      def createItem(itemText: String) = TodoListItem(Props(itemText))
      <.ul(props map createItem)
    })
    .build

  case class State(items: List[String], text: String)

  class Backend($: BackendScope[Unit, State]) {
    def onChange(e: ReactEventI) =
      $.modState(_.copy(text = e.target.value))

    def handleSubmit(e: ReactEventI) =
      e.preventDefaultCB >>
      $.modState(s => State(s.items :+ s.text, ""))

    def render(state: State) =
      <.div(
        <.h3("TODO"),
        TodoList(state.items),
        <.form(^.onSubmit ==> handleSubmit,
          <.input(^.onChange ==> onChange, ^.value := state.text),
          <.button("Add #", state.items.length + 1)
        )
      )
  }

  val ES6IntoRCBApp = ReactComponentB[Unit]("ES6IntoRCB")
    .initialState(State(Nil, ""))
    .renderBackend[Backend]
    .buildU

  // EXAMPLE:END
}
