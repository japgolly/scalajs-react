package ghpages.examples

import japgolly.scalajs.react._, vdom.ReactVDom._, all._
import org.scalajs.dom.HTMLInputElement
import ghpages.examples.util.SideBySide

/** Scala version of example on http://facebook.github.io/react/docs/more-about-refs.html */
object RefsExample {

  def content = SideBySide.Content(jsSource, source, App())

  val jsSource =
    """
      |var App = React.createClass({
      |  getInitialState: function() {
      |    return {userInput: ''};
      |  },
      |  handleChange: function(e) {
      |    this.setState({userInput: e.target.value});
      |  },
      |  clearAndFocusInput: function() {
      |    // Clear the input
      |    this.setState({userInput: ''}, function() {
      |      // This code executes after the component is re-rendered
      |      this.refs.theInput.getDOMNode().focus();   // Boom! Focused!
      |    });
      |  },
      |  render: function() {
      |    return (
      |      <div>
      |        <div onClick={this.clearAndFocusInput}>
      |          Click to Focus and Reset
      |        </div>
      |        <input
      |          ref="theInput"
      |          value={this.state.userInput}
      |          onChange={this.handleChange}
      |        />
      |      </div>
      |    );
      |  }
      |});
      |""".stripMargin


  val source =
    """
      |val theInput = Ref[HTMLInputElement]("theInput")
      |
      |class Backend(t: BackendScope[_, String]) {
      |  def handleChange(e: ReactEventI) =
      |    t.setState(e.target.value)
      |  def clearAndFocusInput() =
      |    t.setState("", () => theInput(t).tryFocus())
      |}
      |
      |val App = ReactComponentB[Unit]("App")
      |  .initialState("")
      |  .backend(new Backend(_))
      |  .render((_,S,B) =>
      |    div(
      |      div(onclick --> B.clearAndFocusInput)("Click to Focus and Reset"),
      |      input(ref := theInput, value := S, onchange ==> B.handleChange)
      |    )
      |  ).buildU
      |
      |React.render(App(), mountNode)
      |""".stripMargin

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
}