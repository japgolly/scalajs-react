package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.util.DefaultEffects.Sync
import japgolly.scalajs.react.vdom.VdomElement
import org.scalajs.dom

// Note: The type `RouterConfig` is defined in the package object.

object RouterConfig {

  /** Either a redirect or a value representing the page to render. */
  type Parsed[Page] = Either[Redirect[Page], Page]

  type Logger = (=> String) => Sync[Unit]

  def consoleLogger: Logger =
    s => Sync.delay(println("[Router] " + s))

  val nopLogger: Logger =
    Function const Sync.empty

  def defaultLogger: Logger =
    nopLogger

  def defaultRenderFn[Page, C]: (RouterCtl[Page], ResolutionWithProps[Page, C]) => C => VdomElement =
    (_, r) => r.renderP

  def defaultPostRenderFn[Page, C]: (Option[Page], Page, C) => Sync[Unit] = {
    val cb = Sync.delay(dom.window.scrollTo(0, 0))
    (_, _, _) => cb
  }

  def withDefaults[Page, C](rules: RoutingRules[Page, C]): RouterWithPropsConfig[Page, C] =
    RouterWithPropsConfig(rules, defaultRenderFn, defaultPostRenderFn, defaultLogger)
}
