package ghpages.examples

import ghpages.GhPagesMacros
import japgolly.scalajs.react._, vdom.html_<^._
import org.scalajs.dom.window
import ghpages.examples.util.SideBySide
import Addons.ReactCssTransitionGroup

object AnimationExample {

  def content = SideBySide.Content(jsSource, source, main())

  lazy val main = addIntro(TodoList, _(scalaPortOfPage("docs/animation.html")))

  val jsSource =
    """
      |var ReactCSSTransitionGroup = React.addons.CSSTransitionGroup;
      |
      |var TodoList = React.createClass({
      |  getInitialState: function() {
      |    return {items: ['hello', 'world', 'click', 'me']};
      |  },
      |
      |  handleAdd: function() {
      |    var newItems =
      |      this.state.items.concat([prompt('Enter some text')]);
      |    this.setState({items: newItems});
      |  },
      |
      |  handleRemove: function(i) {
      |    var newItems = this.state.items;
      |    newItems.splice(i, 1);
      |    this.setState({items: newItems});
      |  },
      |
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

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START

  class Backend($: BackendScope[Unit, Vector[String]]) {
    def handleAdd =
      $.modState(_ :+ window.prompt("Enter some text"))

    def handleRemove(i: Int) =
      $.modState(_.zipWithIndex.filterNot(_._2 == i).map(_._1))

    def render(state: Vector[String]) =
      <.div(
        <.button(^.onClick --> handleAdd, "Add Item"),
        ReactCssTransitionGroup("example", component = "h1")(
          state.zipWithIndex.map { case (s, i) =>
            <.div(^.key := s, ^.onClick --> handleRemove(i), s)
          }: _*
        )
      )
  }

  val TodoList = ScalaComponent.build[Unit]("TodoList")
    .initialState(Vector("hello", "world", "click", "me"))
    .renderBackend[Backend]
    .build

  // EXAMPLE:END
}
