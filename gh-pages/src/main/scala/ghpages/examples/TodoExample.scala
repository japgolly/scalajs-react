package ghpages.examples

import ghpages.GhPagesMacros
import japgolly.scalajs.react._, vdom.prefix_<^._
import ghpages.examples.util.SideBySide

object TodoExample {

  def title = "Todo List"

  def content = SideBySide.Content(jsSource, source, main())

  lazy val main = addIntro(TodoApp, _(scalaPortOf("An Application")))

  val jsSource =
    """
      |var TodoList = React.createClass({displayName: 'TodoList',
      |  render: function() {
      |    var createItem = function(itemText) {
      |      return React.createElement("li", null, itemText);
      |    };
      |    return React.createElement("ul", null, this.props.items.map(createItem));
      |  }
      |});
      |var TodoApp = React.createClass({displayName: 'TodoApp',
      |  getInitialState: function() {
      |    return {items: [], text: ''};
      |  },
      |  onChange: function(e) {
      |    this.setState({text: e.target.value});
      |  },
      |  handleSubmit: function(e) {
      |    e.preventDefault();
      |    var nextItems = this.state.items.concat([this.state.text]);
      |    var nextText = '';
      |    this.setState({items: nextItems, text: nextText});
      |  },
      |  render: function() {
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
      |});
      |
      |ReactDOM.render(React.createElement(TodoApp, null), mountNode);
      |""".stripMargin

  val source =
    s"""
      |${GhPagesMacros.exampleSource}
      |
      |ReactDOM.render(TodoApp(), mountNode)
      |""".stripMargin

  // EXAMPLE:START

  val TodoList = ReactComponentB[List[String]]("TodoList")
    .render_P { props =>
      def createItem(itemText: String) = <.li(itemText)
      <.ul(props map createItem)
    }
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

  val TodoApp = ReactComponentB[Unit]("TodoApp")
    .initialState(State(Nil, ""))
    .renderBackend[Backend]
    .buildU

  // EXAMPLE:END
}
