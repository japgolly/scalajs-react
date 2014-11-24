package japgolly.scalajs.react.example.examples

import japgolly.scalajs.react.vdom.ReactVDom.ReactVExt_Attr
import japgolly.scalajs.react.{SyntheticEvent, BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.ReactVDom.all._
import org.scalajs.dom.HTMLInputElement

/**
 * Created by chandrasekharkode on 11/18/14.
 */
object TodoExample {

  val todoJsxCode = """
                      |var TodoList = React.createClass({displayName: 'TodoList',
                      |  render: function() {
                      |    var createItem = function(itemText) {
                      |      return React.DOM.li(null, itemText);
                      |    };
                      |    return React.DOM.ul(null, this.props.items.map(createItem));
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
                      |      React.DOM.div(null,
                      |        React.DOM.h3(null, "TODO"),
                      |        TodoList( {items:this.state.items} ),
                      |        React.DOM.form( {onSubmit:this.handleSubmit},
                      |          React.DOM.input( {onChange:this.onChange, value:this.state.text} ),
                      |          React.DOM.button(null, 'Add #' + (this.state.items.length + 1))
                      |        )
                      |      )
                      |    );
                      |  }
                      |});
                      |React.renderComponent(TodoApp(null ), mountNode);""".stripMargin


  val todoScalaCode = """
                        | val TodoList = ReactComponentB[List[String]]("TodoList")
                        |    .render(P => {
                        |    def createItem(itemText: String) = li(itemText)
                        |    ul(P map createItem)
                        |  })
                        |    .build
                        |
                        |  case class State(items: List[String], text: String)
                        |
                        |  class Backend(t: BackendScope[Unit, State]) {
                        |    def onChange(e: SyntheticEvent[HTMLInputElement]) =
                        |      t.modState(_.copy(text = e.target.value))
                        |    def handleSubmit(e: SyntheticEvent[HTMLInputElement]) = {
                        |      e.preventDefault()
                        |      t.modState(s => State(s.items :+ s.text, ""))
                        |    }
                        |  }
                        |
                        |  val TodoApp = ReactComponentB[Unit]("TodoApp")
                        |    .initialState(State(Nil, ""))
                        |    .backend(new Backend(_))
                        |    .render((_,S,B) =>
                        |    div(
                        |      h3("TODO"),
                        |      TodoList(S.items),
                        |      form(onsubmit ==>  B.handleSubmit)(
                        |        input(onchange ==> B.onChange, value := S.text),
                        |        button("Add #", S.items.length + 1)
                        |      )
                        |    )
                        |    ).buildU
                        |   React.renderComponent(App(), mountNode)
                        |    """.stripMargin


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
      form(onsubmit ==>  B.handleSubmit)(
        input(onchange ==> B.onChange, value := S.text),
        button("Add #", S.items.length + 1)
      )
    )
    ).buildU
}
