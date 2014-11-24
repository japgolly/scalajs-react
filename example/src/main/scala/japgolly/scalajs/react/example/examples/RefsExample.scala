package japgolly.scalajs.react.example.examples

import japgolly.scalajs.react.vdom.ReactVDom.ReactVExt_Attr
import japgolly.scalajs.react.vdom.ReactVDom.all._
import japgolly.scalajs.react.{ReactComponentB, SyntheticEvent, BackendScope, Ref}
import org.scalajs.dom.HTMLInputElement

/**
 * Created by chandrasekharkode on 11/18/14.
 */
object RefsExample {


  val refsJsxCode = """
                      |var App = React.createClass({
                      |    getInitialState: function() {
                      |      return {userInput: ''};
                      |    },
                      |    handleChange: function(e) {
                      |      this.setState({userInput: e.target.value});
                      |    },
                      |    clearAndFocusInput: function() {
                      |      this.setState({userInput: ''}); // Clear the input
                      |      // We wish to focus the <input /> now!
                      |    },
                      |    render: function() {
                      |      return (
                      |        <div>
                      |          <div onClick={this.clearAndFocusInput}>
                      |            Click to Focus and Reset
                      |          </div>
                      |          <input
                      |            value={this.state.userInput}
                      |            onChange={this.handleChange}
                      |          />
                      |        </div>
                      |      );
                      |    }
                      |  });""".stripMargin


  val refsScalaCode = """
                        |   val theInput = Ref[HTMLInputElement]("theInput")
                        |
                        |    class Backend(t: BackendScope[_, String]) {
                        |      def handleChange(e: SyntheticEvent[HTMLInputElement]) =
                        |        t.setState(e.target.value)
                        |      def clearAndFocusInput() =
                        |        t.setState("", () => theInput(t).tryFocus())
                        |    }
                        |
                        |    val App = ReactComponentB[Unit]("App")
                        |      .initialState("")
                        |      .backend(new Backend(_))
                        |      .render((_,S,B) =>
                        |        div(
                        |          div(onclick --> B.clearAndFocusInput)("Click to Focus and Reset"),
                        |          input(ref := theInput, value := S, onchange ==> B.handleChange)
                        |        )
                        |      ).buildU
                        |
                        |    React.renderComponent(App(), mountNode)""".stripMargin




  val theInput = Ref[HTMLInputElement]("theInput")

  class Backend(t: BackendScope[_, String]) {
    def handleChange(e: SyntheticEvent[HTMLInputElement]) =
      t.setState(e.target.value)
    def clearAndFocusInput() =
      t.setState("", () => theInput(t).tryFocus())
  }

  val App = ReactComponentB[Unit]("App")
    .initialState("")
    .backend(new Backend(_))
    .render((_,S,B) =>
    div(
      div(onclick -->  B.clearAndFocusInput)("Click to Focus and Reset"),
      input(ref := theInput, value := S, onchange ==> B.handleChange)
    )
    ).buildU

}
