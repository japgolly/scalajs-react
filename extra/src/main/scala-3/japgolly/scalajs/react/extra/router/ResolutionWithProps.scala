package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.vdom.VdomElement

/**
 * Result of the router resolving a URL and reaching a conclusion about what to render.
 *
 * @param page Data representation (or command) of what will be drawn.
 * @param render The render function provided by the rules and logic in [[RouterConfig]].
 */
final case class ResolutionWithProps[P, Props](page: P, renderP: Props => VdomElement) {
  inline def render()(implicit inline ev: Unit =:= Props): VdomElement =
    renderP(ev(()))
}