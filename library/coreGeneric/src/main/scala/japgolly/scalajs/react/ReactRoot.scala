package japgolly.scalajs.react

/** A location in the DOM into which React has initialised itself, and now manages.
  *
  * Can be used to render a React element into the DOM with `.render`.
  *
  * @since v2.2.0 / React 18
  */
@inline final case class ReactRoot(raw: facade.RootType) {

  @inline def render[A](node: A)(implicit r: Renderable[A]): Unit =
    raw.render(r(node))

  @inline def unmount(): Unit =
    raw.unmount()
}
