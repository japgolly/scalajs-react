package ghpages.examples

import ghpages.GhPagesMacros
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.vdom.html_<^._
import ghpages.examples.util.SideBySide

object HelloMessageExample {

  def content = SideBySide.Content(jsSource, source, main())

  lazy val main = addIntro(HelloMessage withProps "John", _(scalaPortOf("A Simple Component")))

  val jsSource =
    """
      |var HelloMessage = React.createClass({displayName: 'HelloMessage',
      |  render: function() {
      |    return React.createElement("div", null, "Hello ", this.props.name);
      |  }
      |});
      |
      |ReactDOM.render(React.createElement(HelloMessage, {name: "John"}), mountNode);
    """.stripMargin

  val source =
    s"""
      |${GhPagesMacros.exampleSource}
      |
      |ReactDOM.render(HelloMessage("John"), mountNode)""".stripMargin

  // EXAMPLE:START

  val HelloMessage = ScalaComponent.build[String]("HelloMessage")
    .render($ => <.div("Hello ", $.props))
    .build

  // EXAMPLE:END
}
