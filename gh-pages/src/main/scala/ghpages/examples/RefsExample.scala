package ghpages.examples

import ghpages.GhPagesMacros
import japgolly.scalajs.react._, vdom.html_<^._
import org.scalajs.dom.html
import ghpages.examples.util.SideBySide

object RefsExample {

  def content = SideBySide.Content(jsSource, source, main())

  lazy val main = addIntro(App.withKey(_)(), _(scalaPortOfPage("docs/more-about-refs.html")))

  val jsSource =
    """
      |var App = React.createClass({
      |  getInitialState: function() {
      |    return {userInput: ''};
      |  },
      |
      |  handleChange: function(e) {
      |    this.setState({userInput: e.target.value});
      |  },
      |
      |  clearAndFocusInput: function() {
      |    // Clear the input
      |    this.setState({userInput: ''}, function() {
      |      // This code executes after the component is re-rendered
      |      this.refs.theInput.focus();   // Boom! Focused!
      |    });
      |  },
      |
      |  render: function() {
      |    return (
      |      <div>
      |        <div onClick={this.clearAndFocusInput}>
      |          Click to Focus and Reset
      |        </div>
      |        <input
      |          ref      = "theInput"
      |          value    = {this.state.userInput}
      |          onChange = {this.handleChange}
      |        />
      |      </div>
      |    );
      |  }
      |});
      |""".stripMargin


  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START

  class Backend($: BackendScope[Unit, String]) {

    var theInput: html.Input = _

    def handleChange(e: ReactEventFromInput) =
      $.setState(e.target.value)

    def clearAndFocusInput() =
      $.setState("", Callback(theInput.focus()))

    def render(state: String) =
      <.div(
        <.div(
          ^.onClick --> clearAndFocusInput,
          "Click to Focus and Reset"),
        <.input(
          ^.value     := state,
          ^.onChange ==> handleChange)
          .ref(theInput = _)
      )
  }

  val App = ScalaComponent.build[Unit]("App")
    .initialState("")
    .renderBackend[Backend]
    .build

  // EXAMPLE:END
}
