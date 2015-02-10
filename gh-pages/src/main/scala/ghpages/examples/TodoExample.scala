package ghpages.examples

import japgolly.scalajs.react._, vdom.prefix_<^._
import ghpages.examples.util.SideBySide

/** Scala version of "An Application" on http://facebook.github.io/react/ */
object TodoExample {

  def content = SideBySide.Content(jsSource, source, TodoApp())

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
      |React.render(React.createElement(TodoApp, null), mountNode);
      |""".stripMargin


  val source =
    """
      |val TodoList = ReactComponentB[List[String]]("TodoList")
      |  .render(props => {
      |    def createItem(itemText: String) = <.li(itemText)
      |    <.ul(props map createItem)
      |  })
      |  .build
      |
      |case class State(items: List[String], text: String)
      |
      |class Backend($: BackendScope[Unit, State]) {
      |  def onChange(e: ReactEventI) =
      |    $.modState(_.copy(text = e.target.value))
      |  def handleSubmit(e: ReactEventI) = {
      |    e.preventDefault()
      |    $.modState(s => State(s.items :+ s.text, ""))
      |  }
      |}
      |
      |val TodoApp = ReactComponentB[Unit]("TodoApp")
      |  .initialState(State(Nil, ""))
      |  .backend(new Backend(_))
      |  .render((_,S,B) =>
      |    <.div(
      |      <.h3("TODO"),
      |      TodoList(S.items),
      |      <.form(^.onSubmit ==> B.handleSubmit,
      |        <.input(^.onChange ==> B.onChange, ^.value := S.text),
      |        <.button("Add #", S.items.length + 1)
      |      )
      |    )
      |  ).buildU
      |
      |React.render(TodoApp(), mountNode)
      |""".stripMargin


  val TodoList = ReactComponentB[List[String]]("TodoList")
    .render(props => {
      def createItem(itemText: String) = <.li(itemText)
      <.ul(props map createItem)
    })
    .build

  case class State(items: List[String], text: String)

  class Backend($: BackendScope[Unit, State]) {
    def onChange(e: ReactEventI) =
      $.modState(_.copy(text = e.target.value))
    def handleSubmit(e: ReactEventI) = {
      e.preventDefault()
      $.modState(s => State(s.items :+ s.text, ""))
    }
  }

  val TodoApp = ReactComponentB[Unit]("TodoApp")
    .initialState(State(Nil, ""))
    .backend(new Backend(_))
    .render((_,S,B) =>
      <.div(
        <.h3("TODO"),
        TodoList(S.items),
        <.form(^.onSubmit ==> B.handleSubmit,
          <.input(^.onChange ==> B.onChange, ^.value := S.text),
          <.button("Add #", S.items.length + 1)
        )
      )
    ).buildU
}
