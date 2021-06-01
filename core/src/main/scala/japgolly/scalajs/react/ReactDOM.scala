package japgolly.scalajs.react

import japgolly.scalajs.react.internal.NotAllowed
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{raw => Raw}
import org.scalajs.dom
import scala.scalajs.js.|

object ReactDOM {
  def raw = Raw.ReactDOM
  def version = Raw.ReactDOM.version

  /** For mounted components, use .getDOMNode */
  def findDOMNode(componentOrElement: dom.Element | Raw.React.ComponentUntyped): Option[ComponentDom.Mounted] =
    ComponentDom.findDOMNode(componentOrElement).mounted

  def hydrate(element  : VdomNode,
              container: Raw.ReactDOM.Container,
              callback : Callback = Callback.empty): Raw.React.ComponentUntyped =
    Raw.ReactDOM.hydrate(element.rawNode, container, callback.toJsFn)

  /** Hydrate the container if is has children, else render into that container.
    */
  def hydrateOrRender(element  : VdomNode,
                      container: dom.Element,
                      callback : Callback = Callback.empty): Raw.React.ComponentUntyped =
    if (container.hasChildNodes())
      hydrate(element, container, callback)
    else
      Raw.ReactDOM.render(element.rawNode, container, callback.toJsFn)

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
             callback : Any = null) = element.result

  @deprecated("Import vdom and use ReactPortal()", "")
  def createPortal(child: NotAllowed, container: Any) = child.result

  def flushSync[A](a: CallbackTo[A]): CallbackTo[A] =
    CallbackTo(Raw.ReactDOM.flushSync(a.toJsFn))

}