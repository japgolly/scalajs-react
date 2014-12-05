package ghpages.examples

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.ReactVDom.all._


/**
 * Created by chandrasekharkode on 11/17/14.
 */
object HelloMessageExample {

  val source = """
                 |val Component = ReactComponentB[String]("Hello Example")
                 |   .render(name => div("Hello ", name))
                 |   .build
                 |
                 |React.render(HelloMessage("John"), mountNode)""".stripMargin

  val helloJsXCode =
    """
      |var HelloMessage = React.createClass({displayName: 'HelloMessage',
      |  render: function() {
      |    return React.DOM.div(null, "Hello ", this.props.name);
      |  }
      |});
      |
      |React.render(HelloMessage( {name:"John"} ), mountNode);
    """.stripMargin

  val Component = ReactComponentB[String]("Hello Example")
    .render(name => h1("Hello ", name))
    .build

}
