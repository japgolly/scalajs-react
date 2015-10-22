package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react
import japgolly.scalajs.react.vdom.prefix_<^._
import scala.scalajs.js
import japgolly.scalajs.react._
import scala.scalajs.js.annotation.ScalaJSDefined

/**
 * Example of using Touch events.
 *
 * TouchList is JavaScript collection, so it is converted to Scala IndexedSeq.
 * Showing only top 10 events, so mobile phone will not crash.
 * Preventing default events, so move and zoom events could also be tested
 */
object ES6ClassesExample {

  def content = SingleSide.Content(source, ES6ClassesExampleApp())

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START

  case class Props(text: String, onChange: () => Callback)

  @ScalaJSDefined
  class CustomInputC extends ReactComponent[Props, String, react.TopNode] {
    def initialState(props: Props) = props.text

    def onChange(event: ReactEventI) =
      setState(event.target.value, props.onChange())

    def render() = {
      <.input(^.onChange ==> onChange, ^.placeholder:="Headline", ^.value:=state)
    }
  }

  val CustomInput = ElementFactory.requiredProps(js.constructorOf[CustomInputC], classOf[CustomInputC])
  val inputRef = Ref.to[Props, String, Unit, react.TopNode](CustomInput, "inputRef")

  @ScalaJSDefined
  class Component extends ReactComponentNoProps[String, react.TopNode] {
    def initialState() = "Headline"

    def onChange() = getRef(inputRef).map { r =>
      CallbackTo(r.state).flatMap(s => setState(s))
    }.getOrElse(Callback.empty)

    def render() = {
      <.div(
        <.h1(state),
        CustomInput.set(ref = inputRef)(Props(state, onChange))
      )
    }
  }

  val ES6ClassesExampleApp = ElementFactory.noProps(js.constructorOf[Component], classOf[Component])

  // EXAMPLE:END
}
