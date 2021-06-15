package japgolly.scalajs.react

import japgolly.scalajs.react.util.Effect._
import japgolly.scalajs.react.util.NotAllowed
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
              container: facade.ReactDOM.Container): facade.React.ComponentUntyped =
    facade.ReactDOM.hydrate(element.rawNode, container)

  def hydrate[F[_], A](element  : VdomNode,
                       container: facade.ReactDOM.Container,
                       callback : => F[A])(implicit F: Dispatch[F]): facade.React.ComponentUntyped =
    facade.ReactDOM.hydrate(element.rawNode, container, F.dispatchFn(callback))

  /** Hydrate the container if is has children, else render into that container. */
  def hydrateOrRender(element  : VdomNode,
                      container: dom.Element): facade.React.ComponentUntyped =
    if (container.hasChildNodes())
      hydrate(element, container)
    else
      element.renderIntoDOM(container)

  /** Hydrate the container if is has children, else render into that container. */
  def hydrateOrRender[F[_], A](element  : VdomNode,
                               container: dom.Element,
                               callback : => F[A])(implicit F: Dispatch[F]): facade.React.ComponentUntyped =
    if (container.hasChildNodes())
      hydrate(element, container, callback)
    else
      element.renderIntoDOM(container, callback)

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

  def flushSync[F[_], A](fa: F[A])(implicit F: Sync[F]): F[A] =
    F.delay(facade.ReactDOM.flushSync(F.toJsFn(fa)))

}