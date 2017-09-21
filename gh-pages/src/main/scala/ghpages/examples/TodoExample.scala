package ghpages.examples

import ghpages.GhPagesMacros
import japgolly.scalajs.react._, vdom.html_<^._
import ghpages.examples.util.SideBySide

object TodoExample {

  def title = "Todo List"

  def content = SideBySide.Content(jsSource, source, main())

  lazy val main = addIntro(TodoApp.withKey(_)(), _(scalaPortOf("An Application")))

  val jsSource =
    """
      |class TodoList extends React.Component {
      |  render() {
      |    var createItem = (itemText,index) => React.createElement('li', {key: index}, itemText);
      |    return React.createElement('ul', null, this.props.items.map(createItem));
      |  }
      |}
      |
      |class TodoApp extends React.Component {
      |  constructor(props) {
      |    super(props);
      |    this.state = {items: [], text: ''};
      |    this.onChange = this.onChange.bind(this);
      |    this.handleSubmit = this.handleSubmit.bind(this);
      |  }
      |
      |  onChange(e) {
      |    this.setState({text: e.target.value});
      |  }
      |
      |  handleSubmit(e) {
      |    e.preventDefault();
      |    var nextItems = this.state.items.concat([this.state.text]);
      |    var nextText = '';
      |    this.setState({items: nextItems, text: nextText});
      |  }
      |
      |  render() {
      |    return (
      |      React.createElement("div", null,
      |        React.createElement("h3", null, "TODO"),
      |        React.createElement(TodoList, {items: this.state.items}),
      |        React.createElement("form", {onSubmit: this.handleSubmit},
      |          React.createElement("input", {onChange: this.onChange, value: this.state.text}),
      |          React.createElement("button", null, 'Add #' + (this.state.items.length + 1))
      |        )
      |      )
      |    );
      |  }
      |}
      |
      |ReactDOM.render(React.createElement(TodoApp), mountNode);
      |""".stripMargin

  val source =
    s"""
      |${GhPagesMacros.exampleSource}
      |
      |TodoApp().renderIntoDOM(mountNode)
      |""".stripMargin

  // EXAMPLE:START

  val TodoList = ScalaFnComponent[List[String]]{ props =>
      def createItem(itemText: String) = <.li(itemText)
      <.ul(props map createItem: _*)
    }

  case class State(items: List[String], text: String)

  class Backend($: BackendScope[Unit, State]) {
    def onChange(e: ReactEventFromInput) = {
      val newValue = e.target.value
      $.modState(_.copy(text = newValue))
    }

    def handleSubmit(e: ReactEventFromInput) =
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

  val TodoApp = ScalaComponent.builder[Unit]("TodoApp")
    .initialState(State(Nil, ""))
    .renderBackend[Backend]
    .build

  // EXAMPLE:END
}
