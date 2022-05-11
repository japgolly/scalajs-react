package japgolly.scalajs.react

import japgolly.scalajs.react.facade.ReactDOM.Container
import japgolly.scalajs.react.util.Effect._
import japgolly.scalajs.react.util.NotAllowed
import japgolly.scalajs.react.vdom.VdomNode
import org.scalajs.dom
import scala.scalajs.js.|

object ReactDOM {
  @inline def raw = facade.ReactDOM
  @inline def version = facade.ReactDOM.version

  /** Create a React root for the supplied container and return the root. The root can be used to render a React element
    * into the DOM with `.render`.
    *
    * @since v2.2.0 / React v18
    */
  def createRoot(container: Container): ReactRoot =
    ReactRoot(raw.createRoot(container))

  /** Create a React root for the supplied container and return the root. The root can be used to render a React element
    * into the DOM with `.render`.
    *
    * @since v2.2.0 / React v18
    */
  def createRoot(container: Container, options: ReactOptions.CreateRoot): ReactRoot =
    ReactRoot(raw.createRoot(container, options.raw()))

  /** For mounted components, use .getDOMNode */
  def findDOMNode(componentOrElement: dom.Element | facade.React.ComponentUntyped): Option[ComponentDom.Mounted] =
    ComponentDom.findDOMNode(componentOrElement).mounted

  def flushSync[F[_], A](fa: F[A])(implicit F: Sync[F]): F[A] =
    F.delay(facade.ReactDOM.flushSync(F.toJsFn(fa)))

  def hydrate[G[_]](element  : VdomNode,
                    container: Container,
                    callback : => G[Unit])(implicit G: Dispatch[G]): facade.React.ComponentUntyped =
    facade.ReactDOM.hydrate(element.rawNode, container, G.dispatchFn(callback))

  /** Same as [[createRoot()]], but is used to hydrate a container whose HTML contents were rendered by
    * [[ReactDOMServer]]. React will attempt to attach event listeners to the existing markup.
    *
    * @since v2.2.0 / React v18
    */
  def hydrateRoot(container: Container, element: VdomNode): ReactRoot =
    ReactRoot(raw.hydrateRoot(container, element.rawNode))

  /** Same as [[createRoot()]], but is used to hydrate a container whose HTML contents were rendered by
    * [[ReactDOMServer]]. React will attempt to attach event listeners to the existing markup.
    *
    * @since v2.2.0 / React v18
    */
  def hydrateRoot(container: Container, element: VdomNode, options: ReactOptions.HydrateRoot): ReactRoot =
    ReactRoot(raw.hydrateRoot(container, element.rawNode, options.raw()))

  /** Hydrate the container if is has children, else render into that container. */
  def hydrateOrRender[G[_]](element  : VdomNode,
                            container: dom.Element,
                            callback : => G[Unit])(implicit G: Dispatch[G]): facade.React.ComponentUntyped =
    if (container.hasChildNodes())
      hydrate(element, container, callback)
    else
      element.renderIntoDOM(container, callback)

  /** Hydrate the container if is has children, else render into that container. */
  def hydrateOrRenderIntoNewRoot(container       : dom.Element,
                                 element         : VdomNode,
                                 creationOptions : ReactOptions.CreateRoot = ReactOptions.CreateRoot(),
                                 hydrationOptions: ReactOptions.HydrateRoot = ReactOptions.HydrateRoot(),
                                ): ReactRoot =
    if (container.hasChildNodes())
      hydrateRoot(container, element, hydrationOptions)
    else {
      val root = createRoot(container, creationOptions)
      root.render(element)
      root
    }

  // ===================================================================================================================
  // Deprecated stuff

  @deprecated("Import vdom and use ReactPortal()", "")
  def createPortal(child: NotAllowed, container: Any) = child.result

  @deprecated("Use hydrateRoot instead", "2.2.0 / React v18")
  def hydrate(element: VdomNode, container: Container): facade.React.ComponentUntyped =
    facade.ReactDOM.hydrate(element.rawNode, container)

  /** Hydrate the container if is has children, else render into that container. */
  @deprecated("Use hydrateOrRenderIntoNewRoot instead", "2.2.0 / React v18")
  def hydrateOrRender(element: VdomNode, container: dom.Element): facade.React.ComponentUntyped =
    if (container.hasChildNodes())
      hydrate(element, container)
    else
      element.renderIntoDOM(container)

  @deprecated("Use createRoot and root.render instead", "2.2.0 / React v18")
  def render(element  : NotAllowed,
             container: Any,
             callback : Any = null) = element.result

  @deprecated("Use root.unmount() instead", "2.2.0 / React v18")
  def unmountComponentAtNode(container: dom.Node): Boolean =
    raw.unmountComponentAtNode(container)
}
