package ghpages.examples

import org.scalajs.dom.window
import japgolly.scalajs.react._, vdom.html_<^._
import ghpages.GhPagesMacros
import ghpages.examples.util.SideBySide

object AnimationExample {

  def content = SideBySide.Content(jsSource, source, main())

  lazy val main = addIntro(App.withKey(_)(), _(scalaPortOfPage("docs/animation.html")))

  val jsSource =
    """
      |var ReactCSSTransitionGroup = React.addons.CSSTransitionGroup;
      |
      |class TodoList extends React.Component {
      |  constructor(props) {
      |    super(props);
      |    this.state = {items: ['hello', 'world', 'click', 'me']};
      |    this.handleAdd = this.handleAdd.bind(this);
      |  }
      |
      |  handleAdd() {
      |    const newItems = this.state.items.concat([
      |      prompt('Enter some text')
      |    ]);
      |    this.setState({items: newItems});
      |  }
      |
      |  handleRemove(i) {
      |    let newItems = this.state.items.slice();
      |    newItems.splice(i, 1);
      |    this.setState({items: newItems});
      |  }
      |
      |  render() {
      |    const items = this.state.items.map((item, i) => (
      |      <div key={item} onClick={() => this.handleRemove(i)}>
      |        {item}
      |      </div>
      |    ));
      |
      |    return (
      |      <div>
      |        <button onClick={this.handleAdd}>Add Item</button>
      |        <ReactCSSTransitionGroup
      |          transitionName="example"
      |          transitionEnterTimeout={500}
      |          transitionLeaveTimeout={300}>
      |          {items}
      |        </ReactCSSTransitionGroup>
      |      </div>
      |    );
      |  }
      |}
    """.stripMargin

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START

  import ReactAddons._

  class Backend($: BackendScope[Unit, Vector[String]]) {
    def handleAdd =
      $.modState(_ :+ window.prompt("Enter some text"))

    def handleRemove(i: Int) =
      $.modState(_.zipWithIndex.filterNot(_._2 == i).map(_._1))

    def render(state: Vector[String]) = {
      val items = state.zipWithIndex.map { case (item, i) =>
        <.div(^.key := item, ^.onClick --> handleRemove(i),
          item)
      }

      val p = CSSTransitionGroupProps()
      p.transitionName = "example"
      p.transitionEnterTimeout = 500
      p.transitionLeaveTimeout = 300

      <.div(
        <.button(^.onClick --> handleAdd, "Add Item"),
        CSSTransitionGroup(p)(items.toReactArray))
    }
  }

  val App = ScalaComponent.build[Unit]("TodoList")
    .initialState(Vector("hello", "world", "click", "me"))
    .renderBackend[Backend]
    .build

  // EXAMPLE:END
}
