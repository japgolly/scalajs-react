package japgolly.scalajs.react

import org.scalajs.dom

object ReactDOMClient {
  val raw = facade.ReactDOMClient

  /** Create a React root for the supplied container and return the root. The root can be used to render a React element
    * into the DOM with `.render`.
    *
    * @since v3.0.0 / React v18
    */
  def createRoot(container: raw.RootContainer): ReactRoot =
    ReactRoot(raw.createRoot(container))

  /** Create a React root for the supplied container and return the root. The root can be used to render a React element
    * into the DOM with `.render`.
    *
    * @since v3.0.0 / React v18
    */
  def createRoot(container: raw.RootContainer, options: ReactOptions.CreateRoot): ReactRoot =
    ReactRoot(raw.createRoot(container, options.raw()))

  /** Same as [[createRoot()]], but is used to hydrate a container whose HTML contents were rendered by
    * [[ReactDOMServer]]. React will attempt to attach event listeners to the existing markup.
    *
    * @since v3.0.0 / React v18
    */
  def hydrateRoot[A](container: raw.HydrationContainer, element: A)(implicit r: Renderable[A]): ReactRoot =
    ReactRoot(raw.hydrateRoot(container, r(element)))

  /** Same as [[createRoot()]], but is used to hydrate a container whose HTML contents were rendered by
    * [[ReactDOMServer]]. React will attempt to attach event listeners to the existing markup.
    *
    * @since v3.0.0 / React v18
    */
  def hydrateRoot[A](container: raw.HydrationContainer, element: A, options: ReactOptions.HydrateRoot)
                    (implicit r: Renderable[A]): ReactRoot =
    ReactRoot(raw.hydrateRoot(container, r(element), options.raw()))

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
}
