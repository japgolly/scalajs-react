package japgolly.scalajs.react

import japgolly.scalajs.react.util.Effect._
import japgolly.scalajs.react.util.NotAllowed
import org.scalajs.dom
import scala.scalajs.js.|

object ReactDOM {
  val raw = facade.ReactDOM
  @inline def version = facade.ReactDOM.version

  /** Create a React root for the supplied container and return the root. The root can be used to render a React element
    * into the DOM with `.render`.
    *
    * @since v2.2.0 / React v18
    */
  def createRoot(container: raw.RootContainer): ReactRoot =
    ReactRoot(raw.createRoot(container))

  /** Create a React root for the supplied container and return the root. The root can be used to render a React element
    * into the DOM with `.render`.
    *
    * @since v2.2.0 / React v18
    */
  def createRoot(container: raw.RootContainer, options: ReactOptions.CreateRoot): ReactRoot =
    ReactRoot(raw.createRoot(container, options.raw()))

  /** For mounted components, use .getDOMNode */
  def findDOMNode(componentOrElement: dom.Element | facade.React.ComponentUntyped): Option[ComponentDom.Mounted] =
    ComponentDom.findDOMNode(componentOrElement).mounted

  def flushSync[F[_], A](fa: F[A])(implicit F: Sync[F]): F[A] =
    F.delay(facade.ReactDOM.flushSync(F.toJsFn(fa)))

  def hydrate[G[_], A](element: A, container: raw.Container, callback : => G[Unit])
                      (implicit G: Dispatch[G], r: Renderable[A]): facade.React.ComponentUntyped =
    facade.ReactDOM.hydrate(r(element), container, G.dispatchFn(callback))

  /** Same as [[createRoot()]], but is used to hydrate a container whose HTML contents were rendered by
    * [[ReactDOMServer]]. React will attempt to attach event listeners to the existing markup.
    *
    * @since v2.2.0 / React v18
    */
  def hydrateRoot[A](container: raw.HydrationContainer, element: A)(implicit r: Renderable[A]): ReactRoot =
    ReactRoot(raw.hydrateRoot(container, r(element)))

  /** Same as [[createRoot()]], but is used to hydrate a container whose HTML contents were rendered by
    * [[ReactDOMServer]]. React will attempt to attach event listeners to the existing markup.
    *
    * @since v2.2.0 / React v18
    */
  def hydrateRoot[A](container: raw.HydrationContainer, element: A, options: ReactOptions.HydrateRoot)
                    (implicit r: Renderable[A]): ReactRoot =
    ReactRoot(raw.hydrateRoot(container, r(element), options.raw()))

  /** Hydrate the container if is has children, else render into that container. */
  def hydrateOrRender[G[_], A](element: A, container: dom.Element, callback: => G[Unit])
                              (implicit G: Dispatch[G], r: Renderable[A]): facade.React.ComponentUntyped =
    if (container.hasChildNodes())
      hydrate(element, container, callback)
    else
      raw.render(r(element), container, G.dispatchFn(callback))

  /** Hydrate the container if is has children, else render into that container. */
  def hydrateOrRenderIntoNewRoot[A](container       : dom.Element,
                                    element         : A,
                                    creationOptions : ReactOptions.CreateRoot = ReactOptions.CreateRoot(),
                                    hydrationOptions: ReactOptions.HydrateRoot = ReactOptions.HydrateRoot(),
                                   )(implicit r     : Renderable[A]): ReactRoot =
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
  def hydrate[A](element: A, container: raw.Container)(implicit r: Renderable[A]): facade.React.ComponentUntyped =
    facade.ReactDOM.hydrate(r(element), container)

  /** Hydrate the container if is has children, else render into that container. */
  @deprecated("Use hydrateOrRenderIntoNewRoot instead", "2.2.0 / React v18")
  def hydrateOrRender[A](element: A, container: dom.Element)(implicit r: Renderable[A]): facade.React.ComponentUntyped =
    if (container.hasChildNodes())
      hydrate(element, container)
    else
      raw.render(r(element), container)

  @deprecated("Use createRoot and root.render instead", "2.2.0 / React v18")
  def render(element  : NotAllowed,
             container: Any,
             callback : Any = null) = element.result

  @deprecated("Use root.unmount() instead", "2.2.0 / React v18")
  def unmountComponentAtNode(container: dom.Node): Boolean =
    raw.unmountComponentAtNode(container)
}
