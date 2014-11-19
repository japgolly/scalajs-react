package japgolly.scalajs.react.example.examples

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.ReactVDom.all._


/**
 * Created by chandrasekharkode on 11/17/14.
 */
object HelloMessageExample {

  val helloScalaCode = """
                      |val HelloMessage = ReactComponentB[String]("HelloMessage")
                      |   .render(name => div("Hello ", name)).build
                      |
                      |React.renderComponent(HelloMessage("John"), mountNode)""".stripMargin

  val helloJsXCode =
    """
      |var HelloMessage = React.createClass({displayName: 'HelloMessage',
      |  render: function() {
      |    return React.DOM.div(null, "Hello ", this.props.name);
      |  }
      |});
      |
      |React.renderComponent(HelloMessage( {name:"John"} ), mountNode);
    """.stripMargin

  val helloComponent = ReactComponentB[String]("HelloMessage")
    .render(name => h1("Hello ", name))
    .build

}
