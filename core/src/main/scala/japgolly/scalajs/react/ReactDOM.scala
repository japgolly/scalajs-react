package japgolly.scalajs.react

import japgolly.scalajs.react.internal.NotAllowed
import japgolly.scalajs.react.vdom.VdomNode
import org.scalajs.dom
import scala.scalajs.js.|

object ReactDOM {
  def raw = facade.ReactDOM
  def version = facade.ReactDOM.version

  /** For mounted components, use .getDOMNode */
  def findDOMNode(componentOrElement: dom.Element | facade.React.ComponentUntyped): Option[ComponentDom.Mounted] =
    ComponentDom.findDOMNode(componentOrElement).mounted

  def hydrate(element  : VdomNode,
              container: facade.ReactDOM.Container,
              callback : Callback = Callback.empty): facade.React.ComponentUntyped =
    facade.ReactDOM.hydrate(element.rawNode, container, callback.toJsFn)

  /** Hydrate the container if is has children, else render into that container.
    */
  def hydrateOrRender(element  : VdomNode,
                      container: dom.Element,
                      callback : Callback = Callback.empty): facade.React.ComponentUntyped =
    if (container.hasChildNodes())
      hydrate(element, container, callback)
    else
      facade.ReactDOM.render(element.rawNode, container, callback.toJsFn)

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
    CallbackTo(facade.ReactDOM.flushSync(a.toJsFn))

}