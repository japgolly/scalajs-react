package japgolly.scalajs.react

import org.scalajs.dom

object ReactDOM {
  def raw = japgolly.scalajs.react.raw.ReactDOM

  def unmountComponentAtNode(container: dom.Node): Boolean =
    raw.unmountComponentAtNode(container)

  sealed trait NotAllowed

  @deprecated("Use .renderIntoDOM on unmounted components.", "")
  def render(element  : NotAllowed,
             container: NotAllowed,
             callback : NotAllowed = null): Null = null

  @deprecated("Use .getDOMNode on mounted components.", "")
  def findDOMNode(comonent: NotAllowed): Null = null

  @deprecated("Import vdom and use ReactPortal()", "")
  def createPortal(child: NotAllowed, container: NotAllowed): Null = null

}