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
      |class Refs extends React.Component {
      |  constructor(props) {
      |    super(props);
      |    this.state = {
      |      userInput: ''
      |    };
      |    this.handleChange = this.handleChange.bind(this);
      |    this.clearAndFocusInput = this.clearAndFocusInput.bind(this);
      |  }
      |
      |  handleChange(e) {
      |    this.setState({userInput: e.target.value});
      |  }
      |
      |  clearAndFocusInput() {
      |    // Clear the input
      |    this.setState({userInput: ''}, function() {
      |      // This code executes after the component is re-rendered
      |      this.refs.theInput.focus();   // Boom! Focused!
      |    });
      |  }
      |
      |  render() {
      |    return (
      |      React.createElement("div", null,
      |        React.createElement("div", {onClick: this.clearAndFocusInput},
      |         'Click to Focus and Reset'
      |       ),
      |        React.createElement("input",
      |          {ref: 'theInput', value: this.state.userInput, onChange: this.handleChange}
      |        )
      |      )
      |    );
      |  }
      |}
      |
      |ReactDOM.render(React.createElement(Refs), mountNode);
      |""".stripMargin


  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START

  class Backend($: BackendScope[Unit, String]) {

    val inputRef = Ref[html.Input]

    def handleChange(e: ReactEventFromInput) =
      $.setState(e.target.value)

    def clearAndFocusInput() =
      $.setState("", inputRef.foreach(_.focus()))

    def render(state: String) =
      <.div(
        <.div(
          ^.onClick --> clearAndFocusInput,
          "Click to Focus and Reset"),
        <.input(
          ^.value     := state,
          ^.onChange ==> handleChange)
          .withRef(inputRef)
      )
  }

  val App = ScalaComponent.builder[Unit]("App")
    .initialState("")
    .renderBackend[Backend]
    .build

  // EXAMPLE:END
}
