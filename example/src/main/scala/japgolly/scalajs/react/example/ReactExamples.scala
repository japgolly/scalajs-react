package japgolly.scalajs.react.example

import scala.scalajs.js
import org.scalajs.dom.{HTMLInputElement, console, document, window, Node}

import japgolly.scalajs.react._
import vdom.ReactVDom._
import all._

object ReactExamples extends js.JSApp {

  override def main(): Unit = {
    example1(document getElementById "eg1")
    example2(document getElementById "eg2")
    example3(document getElementById "eg3")
    example_refs(document getElementById "eg_refs")
  }

  // ===================================================================================================================

  def example1(mountNode: Node) = {

    val HelloMessage = ReactComponentB[String]("HelloMessage")
      .render(name => div("Hello ", name))
      .build

    React.renderComponent(HelloMessage("John"), mountNode)
  }

  // ===================================================================================================================

  def example2(mountNode: Node) = {

    case class State(secondsElapsed: Long)

    class Backend {
      var interval: js.UndefOr[Int] = js.undefined
      def tick(scope: ComponentScopeM[_, State, _]): js.Function =
        () => scope.modState(s => State(s.secondsElapsed + 1))
    }

    val Timer = ReactComponentB[Unit]("Timer")
      .initialState(State(0))
      .backend(_ => new Backend)
      .render((_,S,_) => div("Seconds elapsed: ", S.secondsElapsed))
      .componentDidMount(scope =>
        scope.backend.interval = window.setInterval(scope.backend.tick(scope), 1000))
      .componentWillUnmount(_.backend.interval foreach window.clearInterval)
      .buildU

    React.renderComponent(Timer(), mountNode)
  }

  // ===================================================================================================================

  def example3(mountNode: Node) = {

    val TodoList = ReactComponentB[List[String]]("TodoList")
      .render(P => {
        def createItem(itemText: String) = li(itemText)
        ul(P map createItem)
      })
      .build

    case class State(items: List[String], text: String)

    class Backend(t: BackendScope[Unit, State]) {
      def onChange(e: SyntheticEvent[HTMLInputElement]) =
        t.modState(_.copy(text = e.target.value))
      def handleSubmit(e: SyntheticEvent[HTMLInputElement]) = {
        e.preventDefault()
        t.modState(s => State(s.items :+ s.text, ""))
      }
    }

    val TodoApp = ReactComponentB[Unit]("TodoApp")
      .initialState(State(Nil, ""))
      .backend(new Backend(_))
      .render((_,S,B) =>
        div(
          h3("TODO"),
          TodoList(S.items),
          form(onsubmit ==> B.handleSubmit)(
            input(onchange ==> B.onChange, value := S.text),
            button("Add #", S.items.length + 1)
          )
        )
      ).buildU

    React.renderComponent(TodoApp(), mountNode)
  }

  // ===================================================================================================================

  def example_refs(mountNode: Node) = {

    val theInput = Ref[HTMLInputElement]("theInput")

    class Backend(t: BackendScope[_, String]) {
      def handleChange(e: SyntheticEvent[HTMLInputElement]) =
        t.setState(e.target.value)
      def clearAndFocusInput() =
        t.setState("", () => theInput(t).tryFocus())
    }

    val App = ReactComponentB[Unit]("App")
      .initialState("")
      .backend(new Backend(_))
      .render((_,S,B) =>
        div(
          div(onclick --> B.clearAndFocusInput)("Click to Focus and Reset"),
          input(ref := theInput, value := S, onchange ==> B.handleChange)
        )
      ).buildU

    React.renderComponent(App(), mountNode)
  }
}
