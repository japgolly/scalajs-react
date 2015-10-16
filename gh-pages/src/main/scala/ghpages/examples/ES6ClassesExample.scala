package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import scala.scalajs.js

import scala.scalajs.js.annotation.ScalaJSDefined

/**
 * Example of using Touch events.
 *
 * TouchList is JavaScript collection, so it is converted to Scala IndexedSeq.
 * Showing only top 10 events, so mobile phone will not crash.
 * Preventing default events, so move and zoom events could also be tested
 */
object ES6ClassesExample {

  def content = SingleSide.Content(source, ES6ClassesExampleApp)

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START

  @ScalaJSDefined
  class Component extends ReactComponent[Unit, Unit] {
    def render() = {
      <.div(
        <.p("test1"),
        <.p("test2")
      )
    }
  }

  val ctor = ElementFactory.getComponentConstructor[Unit, Unit, Component](js.constructorOf[Component])

  val ES6ClassesExampleApp = React.createElement(ctor, js.Dynamic.literal())

  // EXAMPLE:END
}
