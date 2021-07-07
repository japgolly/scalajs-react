package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.util.Effect.Sync
import japgolly.scalajs.react.vdom.VdomElement
import org.scalajs.dom
import scala.scalajs.js

// Note: The type `RouterConfig` is defined in the package object.

object RouterConfig {

  /** Either a redirect or a value representing the page to render. */
  type Parsed[Page] = Either[Redirect[Page], Page]

  type Logger = (=> String) => js.Function0[Unit]

  def consoleLogger: Logger =
    s => () => println("[Router] " + s)

  val nopLogger: Logger = {
    val f: js.Function0[Unit] = () => ()
    _ => f
  }

  def defaultLogger: Logger =
    nopLogger

  def defaultRenderFn[F[_], Page, C]: (RouterCtlF[F, Page], ResolutionWithProps[Page, C]) => C => VdomElement =
    (_, r) => r.renderP

  def defaultPostRenderFn[F[_], Page, C](implicit F: Sync[F]): (Option[Page], Page, C) => F[Unit] = {
    val cb = F.delay(dom.window.scrollTo(0, 0))
    (_, _, _) => cb
  }

  def withDefaults[F[_]: Sync, Page, C](rules: RoutingRulesF[F, Page, C]): RouterWithPropsConfigF[F, Page, C] =
    RouterWithPropsConfigF(rules, defaultRenderFn, defaultPostRenderFn, defaultLogger)
}
