package japgolly.scalajs.react

import org.scalajs.dom

object ReactDOM {
  def raw = japgolly.scalajs.react.raw.ReactDOM

  def unmountComponentAtNode(container: dom.Node): Boolean =
    raw.unmountComponentAtNode(container)

  @deprecated("Use .renderIntoDOM on unmounted components.", "")
  def render(element  : Nothing,
             container: Nothing,
             callback : Any = null): Null = null

  @deprecated("Use .getDOMNode on mounted components.", "")
  def findDOMNode(comonent: Nothing): Null = null
}