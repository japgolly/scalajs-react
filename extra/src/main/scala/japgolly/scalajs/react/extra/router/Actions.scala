package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.vdom.VdomElement
import scala.annotation.nowarn

// If we don't extend Product with Serializable here, a method that returns both a Renderer[P] and a Redirect[P] will
// be type-inferred to "Product with Serializable with Action[P]" which breaks the Renderable & Actionable implicits.
sealed trait Action[P] extends Product with Serializable {
  def map[A](f: P => A): Action[A]
}

final case class Renderer[P, Props](f: RouterCtl[P] => Props => VdomElement) extends Action[P] {
  def apply(ctl: RouterCtl[P]): Props => VdomElement = f(ctl)

  override def map[A](g: P => A): Renderer[A, Props] =
    Renderer(r => f(r contramap g))
}

sealed trait Redirect[P] extends Action[P] {
  override def map[A](f: P => A): Redirect[A]
}

object Redirect {

  @deprecated("Use SetRouteVia", "1.5.0")
  sealed trait Method

  /** The current URL will not be recorded in history. User can't hit ''Back'' button to reach it. */
  @deprecated("Use SetRouteVia.HistoryReplace", "1.5.0")
  case object Replace extends Method

  /** The current URL will be recorded in history. User can hit ''Back'' button to reach it. */
  @deprecated("Use SetRouteVia.HistoryPush", "1.5.0")
  case object Push extends Method

  /** `window.location.href` will be programmatically set to the new URL.
    * If the new URL is part of the current SPA, the entire SPA will be reloaded.
    *
    * The current URL will be recorded in history. User can hit ''Back'' button to reach it.
    */
  @deprecated("Use SetRouteVia.WindowLocation", "1.5.0")
  case object Force extends Method

  @nowarn
  implicit def autoMigrateMethodToSetRouteVia(m: Method): SetRouteVia =
    m match {
      case Replace => SetRouteVia.HistoryReplace
      case Push    => SetRouteVia.HistoryPush
      case Force   => SetRouteVia.WindowLocation
    }
}

final case class RedirectToPage[P](page: P, via: SetRouteVia) extends Redirect[P] {
  override def map[A](f: P => A): RedirectToPage[A] =
    RedirectToPage(f(page), via)
}

final case class RedirectToPath[P](path: Path, via: SetRouteVia) extends Redirect[P] {
  override def map[A](f: P => A): RedirectToPath[A] =
    RedirectToPath(path, via)
}
