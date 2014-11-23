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
    example_productTable(document getElementById "eg_productTable")
  }

  // ===================================================================================================================
  // Scala version of "A Simple Component" on http://facebook.github.io/react/

  def example1(mountNode: Node) = {

    val HelloMessage = ReactComponentB[String]("HelloMessage")
      .render(name => div("Hello ", name))
      .build

    React.render(HelloMessage("John"), mountNode)
  }

  // ===================================================================================================================
  // Scala version of "A Stateful Component" on http://facebook.github.io/react/

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

    React.render(Timer(), mountNode)
  }

  // ===================================================================================================================
  // Scala version of "An Application" on http://facebook.github.io/react/

  def example3(mountNode: Node) = {

    val TodoList = ReactComponentB[List[String]]("TodoList")
      .render(P => {
        def createItem(itemText: String) = li(itemText)
        ul(P map createItem)
      })
      .build

    case class State(items: List[String], text: String)

    class Backend(t: BackendScope[Unit, State]) {
      def onChange(e: ReactEventI) =
        t.modState(_.copy(text = e.target.value))
      def handleSubmit(e: ReactEventI) = {
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

    React.render(TodoApp(), mountNode)
  }

  // ===================================================================================================================
  // Scala version of example on http://facebook.github.io/react/docs/more-about-refs.html

  def example_refs(mountNode: Node) = {

    val theInput = Ref[HTMLInputElement]("theInput")

    class Backend(t: BackendScope[_, String]) {
      def handleChange(e: ReactEventI) =
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

    React.render(App(), mountNode)
  }
  // ===================================================================================================================
  // Scala version of example on http://facebook.github.io/react/docs/thinking-in-react.html

  def example_productTable(mountNode: Node) = {

    case class Product(name: String, price: Double, category: String, stocked: Boolean)

    case class State(filterText: String, inStockOnly: Boolean)

    class Backend(t: BackendScope[_, State])  {
      def onTextChange(e: ReactEventI) =
        t.modState(_.copy(filterText = e.target.value))
      def onCheckBox(e: ReactEvent) =
        t.modState(s => s.copy(inStockOnly = !s.inStockOnly))
    }

    val ProductCategoryRow = ReactComponentB[String]("ProductCategoryRow")
      .render(category => tr(th(colspan := 2, category)))
      .build

    val ProductRow = ReactComponentB[Product]("ProductRow")
      .render(p =>
        tr(
          td(span(!p.stocked && (color := "red"), p.name)),
          td(p.price))
      )
      .build

    def productFilter(s: State)(p: Product): Boolean =
      p.name.contains(s.filterText) &&
      (!s.inStockOnly || p.stocked)

    val ProductTable = ReactComponentB[(List[Product], State)]("ProductTable")
      .render(P => {
        val (products, state) = P
        val rows = products.filter(productFilter(state))
                   .groupBy(_.category).toList
                   .flatMap{ case (cat, ps) =>
                      ProductCategoryRow.withKey(cat)(cat) :: ps.map(p => ProductRow.withKey(p.name)(p))
                    }
        table(
          thead(
            tr(
              th("Name"),
              th("Price"))),
          tbody(
            rows))
      })
      .build

    val SearchBar = ReactComponentB[(State, Backend)]("SearchBar")
      .render(P => {
        val (s, b) = P
        form()(
          input(placeholder := "Search Bar ...", value := s.filterText, onchange ==> b.onTextChange),
          p(
            input(tpe := "checkbox", onclick ==> b.onCheckBox), "Only show products in stock"))
      })
      .build


    val FilterableProductTable = ReactComponentB[List[Product]]("FilterableProductTable")
      .initialState(State("", false))
      .backend(new Backend(_))
      .render((P, S, B) =>
        div(
          SearchBar((S,B)),
          ProductTable((P,S)))
      )
      .build

      val products = List(
        Product("FootBall", 49.99, "Sporting Goods", true),
        Product("Baseball", 9.99, "Sporting Goods", true),
        Product("basketball", 29.99, "Sporting Goods", false),
        Product("ipod touch", 99.99, "Electronics", true),
        Product("iphone 5", 499.99, "Electronics", true),
        Product("Nexus 7", 199.99, "Electronics", true))

    React.render(FilterableProductTable(products), mountNode)
  }
}