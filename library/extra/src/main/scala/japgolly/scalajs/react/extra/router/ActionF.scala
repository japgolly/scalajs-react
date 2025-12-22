package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.util.Effect.Sync
import japgolly.scalajs.react.vdom.VdomElement

// If we don't extend Product with Serializable here, a method that returns both a Renderer[P] and a Redirect[P] will
// be type-inferred to "Product with Serializable with Action[Page, Props]" which breaks the Renderable & Actionable implicits.
sealed trait ActionF[+F[_], Page, -Props] extends Product with Serializable {
  def map[A](f: Page => A): ActionF[F, A, Props]
  def withEffect[G[_]](implicit G: Sync[G]): ActionF[G, Page, Props]
}

final case class RendererF[F[_], Page, -Props](render: RouterCtlF[F, Page] => Props => VdomElement)
                                              (implicit val sync: Sync[F]) extends ActionF[F, Page, Props] {
  override def toString = s"Renderer($render)"
  @inline def apply(ctl: RouterCtlF[F, Page]): Props => VdomElement =
    render(ctl)

  override def map[A](g: Page => A): RendererF[F, A, Props] =
    RendererF(rc => render(rc contramap g))

  override def withEffect[G[_]](implicit G: Sync[G]): RendererF[G, Page, Props] =
    G.subst[F, ({ type L[E[_]] = RendererF[E, Page, Props] })#L](this)(
      RendererF(rc => render(rc.withEffect(sync))))
}

sealed trait Redirect[Page] extends ActionF[Nothing, Page, Any] {
  override def map[A](f: Page => A): Redirect[A]
  override final def withEffect[G[_]](implicit G: Sync[G]): this.type = this
}

final case class RedirectToPage[Page](page: Page, via: SetRouteVia) extends Redirect[Page] {
  override def map[A](f: Page => A): RedirectToPage[A] =
    RedirectToPage(f(page), via)
}

final case class RedirectToPath[Page](path: Path, via: SetRouteVia) extends Redirect[Page] {
  override def map[A](f: Page => A): RedirectToPath[A] =
    RedirectToPath(path, via)
}
