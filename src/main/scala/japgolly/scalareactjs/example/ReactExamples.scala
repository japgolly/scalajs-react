package japgolly.scalareactjs.example

import scala.collection.immutable.SortedSet
import scala.scalajs.js
import org.scalajs.dom.{HTMLInputElement, console, document, window}

import japgolly.scalareactjs._
import vdom.ReactVDom._
import all._

object ReactExamples extends js.JSApp {

  override def main(): Unit = {
    Sample4()
  }

  object Sample1 {

    case class HelloProps(name: String, age: Int)

    val component = ReactComponentB[HelloProps]("sample1")
      .render(P =>
        div(backgroundColor := "#fdd", color := "#c00")(
          h1("THIS IS COOL."),
          p(textDecoration := "underline")("Hello there, ", "Hello, ", P.name, " of age ", P.age)
        ).render
      ).build

    def apply(): Unit = {
      React.renderComponent(component.create(HelloProps("Johnhy", 100)), document getElementById "target")
    }
  }
  
  // ===================================================================================================================

  object Sample2 {

    case class MyProps(title: String, startTime: Long)

    case class MyState(secondsElapsed: Long) {
      def inc = MyState(secondsElapsed + 1)
    }

    class MyBackend {
      var interval: js.UndefOr[Int] = js.undefined
      def start(tick: js.Function): Unit = interval = window.setInterval(tick, 1000)
      def stop(): Unit = interval foreach window.clearInterval
    }

    val component = ReactComponentB[MyProps]("sample2")
      .getInitialState(p => MyState(p.startTime))
      .backend(_ => new MyBackend)
      .render((P,S,_) =>
        div(backgroundColor := "#fdd", color := "#c00")(
          h1("THIS IS AWESOME (", P.title, ")"),
          p(textDecoration := "underline")("Seconds elapsed: ", S.secondsElapsed)
        ).render
      )
      .componentDidMount(ctx => {
        val tick: js.Function = (_: js.Any) => ctx.modState(_.inc)
        console log "Installing timer..."
        ctx.backend.start(tick)
      })
      .componentWillUnmount(_.backend.stop)
      .build

    def apply(): Unit = {
      React.renderComponent(component.create(MyProps("Great", 0)), document getElementById "target")
      React.renderComponent(component.create(MyProps("Again", 1000)), document getElementById "target2")
    }
  }

  // ===================================================================================================================

  object Sample3 {

    case class State(items: List[String], text: String)

    val inputRef = Ref[HTMLInputElement]("i")

    val TodoList = ReactComponentB[List[String]]("TodoList")
      .render(P =>
        ul(P.map(itemText => li(itemText))).render
      ).build

    val TodoApp = ReactComponentB[Unit]("TodoApp")
      .initialState(State(List("Sample todo #1", "Sample todo #2"), "Sample todo #3"))
      .backend(new Backend(_))
      .render((_,S,B) =>
        div(
          h3("TODO"),
          TodoList.create(S.items),
          form(onsubmit ==> B.handleSubmit)(
            input(onchange ==> B.onChange, value := S.text, ref := inputRef)(),
            button("Add #", S.items.length + 1)
          )
        ).render
      )
      .build

    class Backend(t: ComponentScopeB[Unit, State]) {
      val handleSubmit: SyntheticEvent[HTMLInputElement] => Unit = e => {
        e.preventDefault()
        val nextItems = t.state.items :+ t.state.text
        t.setState(State(nextItems, ""))
        inputRef(t).tryFocus()
      }

      val onChange: SyntheticEvent[HTMLInputElement] => Unit = e =>
        t.modState(_.copy(text = e.target.value))
    }

    def apply(): Unit = {
      React.renderComponent(TodoApp.create(()), document getElementById "target")
    }

  }

  // ===================================================================================================================

  def textChangeRecv(f: String => Unit): SyntheticEvent[HTMLInputElement] => Unit = e => f(e.target.value)

  object Sample4 {

    case class State(people: SortedSet[String], text: String, focusPerson: Option[String]) {
      def updateText(t: String) = copy(text = t, focusPerson = None)
    }

    class PeopleListBackend(t: ComponentScopeB[Unit, State]) {
      def delete(name: String): Unit = {
        val p = t.state.people
        if (p.contains(name))
          t.setState(State(p - name, name, None))
      }

      val onChange = textChangeRecv(s => t.modState(_ updateText s))

      val onKP: SyntheticEvent[HTMLInputElement] => Unit =
        e => if (e.keyboardEvent.keyCode == 13) {
            e.preventDefault()
            add()
          }

      def add(): Unit = t.setState(State(t.state.people + t.state.text, "", Some(t.state.text)))
    }

    case class PeopleListProps(people: SortedSet[String], latest: Option[String], deleteFn: String => Unit)

    val PeopleList = {
      val focusNext = Ref[HTMLInputElement]("latest")

      ReactComponentB[PeopleListProps]("PeopleList")
        .render(P =>
          if (P.people.isEmpty)
            div(color := "#800")("No people in your list!!").render
          else
            ol(P.people.toList.map(p =>
              li(
                input(value := p, (P.latest contains p) && (ref := focusNext))(),
                button(marginLeft := 1.em, onclick runs P.deleteFn(p))("Delete"))
            )).render
          )
          .componentDidUpdate((t,_,_) => focusNext(t).tryFocus())
          .componentDidMount(t => focusNext(t).tryFocus())
          .build
    }

    val PeopleEditor = ReactComponentB[Unit]("PeopleEditor")
      .getInitialState(_ => State(SortedSet("First","Second", "x"), "Middle", Some("Second")))
      .backend(new PeopleListBackend(_))
      .render((_,S,B) =>
          div(
            h3("People List")
            ,div(PeopleList.create(PeopleListProps(S.people, S.focusPerson, B.delete)))
            ,h3("Add")
            ,input(onchange ==> B.onChange, onkeypress ==> B.onKP, value := S.text)()
            ,button(onclick runs B.add())("+")
          ).render
      )
      .build

    def apply(): Unit =
      React.renderComponent(PeopleEditor.create(()), document getElementById "target2")
  }
}
