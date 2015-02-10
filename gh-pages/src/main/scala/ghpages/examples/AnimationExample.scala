package ghpages.examples

import japgolly.scalajs.react._, vdom.prefix_<^._
import org.scalajs.dom.window
import ghpages.examples.util.SideBySide
import Addons.ReactCssTransitionGroup

/** http://facebook.github.io/react/docs/animation.html */
object AnimationExample {

  def content = SideBySide.Content(jsSource, source, TodoList())

  val jsSource =
    """
      |var ReactCSSTransitionGroup = React.addons.CSSTransitionGroup;
      |
      |var TodoList = React.createClass({
      |  getInitialState: function() {
      |    return {items: ['hello', 'world', 'click', 'me']};
      |  },
      |  handleAdd: function() {
      |    var newItems =
      |      this.state.items.concat([prompt('Enter some text')]);
      |    this.setState({items: newItems});
      |  },
      |  handleRemove: function(i) {
      |    var newItems = this.state.items;
      |    newItems.splice(i, 1);
      |    this.setState({items: newItems});
      |  },
      |  render: function() {
      |    var items = this.state.items.map(function(item, i) {
      |      return (
      |        <div key={item} onClick={this.handleRemove.bind(this, i)}>
      |          {item}
      |        </div>
      |      );
      |    }.bind(this));
      |    return (
      |      <div>
      |        <button onClick={this.handleAdd}>Add Item</button>
      |        <ReactCSSTransitionGroup transitionName="example">
      |          {items}
      |        </ReactCSSTransitionGroup>
      |      </div>
      |    );
      |  }
      |});
    """.stripMargin

  val source =
    """
      |class Backend($: BackendScope[_, Vector[String]]) {
      |  def handleAdd(): Unit =
      |    $.modState(_ :+ window.prompt("Enter some text"))
      |  def handleRemove(i: Int): Unit =
      |    $.modState(_.zipWithIndex.filterNot(_._2 == i).map(_._1))
      |}
      |
      |val TodoList = ReactComponentB[Unit]("TodoList")
      |  .initialState(Vector("hello", "world", "click", "me"))
      |  .backend(new Backend(_))
      |  .render((_,S,B) =>
      |    <.div(
      |      <.button(^.onClick --> B.handleAdd())("Add Item"),
      |      ReactCssTransitionGroup("example", component = "h1")(
      |        S.zipWithIndex.map{case (s,i) =>
      |          <.div(^.key := s, ^.onClick --> B.handleRemove(i))(s)
      |        }: _*
      |      )
      |    )
      |  ).buildU
      | """.stripMargin

  class Backend($: BackendScope[_, Vector[String]]) {
    def handleAdd(): Unit =
      $.modState(_ :+ window.prompt("Enter some text"))
    def handleRemove(i: Int): Unit =
      $.modState(_.zipWithIndex.filterNot(_._2 == i).map(_._1))
  }

  val TodoList = ReactComponentB[Unit]("TodoList")
    .initialState(Vector("hello", "world", "click", "me"))
    .backend(new Backend(_))
    .render((_,S,B) =>
      <.div(
        <.button(^.onClick --> B.handleAdd())("Add Item"),
        ReactCssTransitionGroup("example", component = "h1")(
          S.zipWithIndex.map{case (s,i) =>
            <.div(^.key := s, ^.onClick --> B.handleRemove(i))(s)
          }: _*
        )
      )
    ).buildU
}
