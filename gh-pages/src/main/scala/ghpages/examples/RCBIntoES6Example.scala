package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

object RCBIntoES6Example {

  def title = "ReactComponentB into ES6 Component Example"

  def content = SingleSide.Content(source, main())

  lazy val main = addIntro(RCBIntoES6App, _(scalaPortOf("An Application")))

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START

  val TodoListItem = ReactComponentB[String]("TodoListItem")
    .stateless
    .render_P{ case (text) =>
      <.li(text)
    }
    .build

  @ScalaJSDefined
  class TodoListC extends ReactComponentNoState[Seq[String], TopNode] {
    def render() = {
      def createItem(itemText: String) = TodoListItem(itemText)
      <.ul(props map createItem)
    }
  }

  val TodoList = ElementFactory.requiredProps(js.constructorOf[TodoListC], classOf[TodoListC])

  case class State(items: Seq[String], text: String)

  @ScalaJSDefined
  class RCBIntoES6AppC extends ReactComponentNoProps[State, TopNode] {

    def initialState() = State(Nil, "")

    def onChange(e: ReactEventI) =
      modState((state: State) => state.copy(text = e.target.value))

    def handleSubmit(e: ReactEventI) = e.preventDefaultCB >>
      modState((state: State) => State(state.items :+ state.text, ""))

    def render() = {
      <.div(
        <.h3("TODO"),
        TodoList(state.items),
        <.form(^.onSubmit ==> handleSubmit,
          <.input(^.onChange ==> onChange, ^.value := state.text),
          <.button("Add #", state.items.length + 1)
        )
      )
    }
  }

  val RCBIntoES6App = ElementFactory.noProps(js.constructorOf[RCBIntoES6AppC], classOf[RCBIntoES6AppC])

  // EXAMPLE:END
}
