package japgolly.scalajs.react

import japgolly.scalajs.react.internal.NotAllowed
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.{raw => Raw}
import org.scalajs.dom
import scala.scalajs.js.|

object ReactDOM {
  def raw = Raw.ReactDOM

  /** For mounted components, use .getDOMNode */
  def findDOMNode(componentOrElement: dom.Element | Raw.React.ComponentUntyped): Option[ComponentDom.Mounted] =
    ComponentDom.findDOMNode(componentOrElement).mounted

  def hydrate(element  : VdomElement,
              container: Raw.ReactDOM.Container,
              callback: Callback = Callback.empty): Raw.React.ComponentUntyped =
    Raw.ReactDOM.hydrate(element.rawElement, container, callback.toJsFn)

  def unmountComponentAtNode(container: dom.Node): Boolean =
    raw.unmountComponentAtNode(container)

  // .hydrate is not here because currently, SSR with scalajs-react isn't directly supported.
  // .raw.hydrate can be used if needed.

  // There are three ways of providing this functionality:
  // 1. ReactDOM.render here (problem: lose return type precision)
  // 2. ReactDOM.render{Js,Scala,Vdom,etc}
  // 3. Add .renderIntoDOM to mountable types (current solution)
  @deprecated("Use .renderIntoDOM on unmounted components.", "")
  def render(element  : NotAllowed,
             container: Any,
             callback : Any = null) = NotAllowed.body

  @deprecated("Import vdom and use ReactPortal()", "")
  def createPortal(child: NotAllowed, container: Any) = NotAllowed.body

}