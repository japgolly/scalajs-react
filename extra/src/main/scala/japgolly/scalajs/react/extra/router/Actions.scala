package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.vdom.VdomElement

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

final case class RedirectToPage[P](page: P, via: SetRouteVia) extends Redirect[P] {
  override def map[A](f: P => A): RedirectToPage[A] =
    RedirectToPage(f(page), via)
}

final case class RedirectToPath[P](path: Path, via: SetRouteVia) extends Redirect[P] {
  override def map[A](f: P => A): RedirectToPath[A] =
    RedirectToPath(path, via)
}
