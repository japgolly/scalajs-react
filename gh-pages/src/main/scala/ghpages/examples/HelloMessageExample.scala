package ghpages.examples

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
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
      |React.render(React.createElement(HelloMessage, {name: "John"}), mountNode);
    """.stripMargin

  val source =
    """
      |val HelloMessage = ReactComponentB[String]("HelloMessage")
      |   .render(name => <.div("Hello ", name))
      |   .build
      |
      |React.render(HelloMessage("John"), mountNode)""".stripMargin

  val HelloMessage = ReactComponentB[String]("HelloMessage")
    .render(name => <.div("Hello ", name))
    .build
}
