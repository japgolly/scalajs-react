package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.vdom.VdomElement

// If we don't extend Product with Serializable here, a method that returns both a Renderer[P] and a Redirect[P] will
// be type-inferred to "Product with Serializable with Action[P]" which breaks the Renderable & Actionable implicits.
sealed trait Action[P] extends Product with Serializable {
  def map[A](f: P => A): Action[A]
}

final case class Renderer[P](f: RouterCtl[P] => VdomElement) extends Action[P] {
  def apply(ctl: RouterCtl[P]) = f(ctl)

  override def map[A](g: P => A): Renderer[A] =
    Renderer(r => f(r contramap g))
}

sealed trait Redirect[P] extends Action[P] {
  override def map[A](f: P => A): Redirect[A]
}

object Redirect {
  sealed trait Method

  /** The current URL will not be recorded in history. User can't hit ''Back'' button to reach it. */
  case object Replace extends Method

  /** The current URL will be recorded in history. User can hit ''Back'' button to reach it. */
  case object Push extends Method

  /** `window.location.href` will be programmatically set to the new URL.
    * If the new URL is part of the current SPA, the entire SPA will be reloaded.
    *
    * The current URL will be recorded in history. User can hit ''Back'' button to reach it.
    */
  case object Force extends Method
}

final case class RedirectToPage[P](page: P, method: Redirect.Method) extends Redirect[P] {
  override def map[A](f: P => A): RedirectToPage[A] =
    RedirectToPage(f(page), method)
}

final case class RedirectToPath[P](path: Path, method: Redirect.Method) extends Redirect[P] {
  override def map[A](f: P => A): RedirectToPath[A] =
    RedirectToPath(path, method)
}
