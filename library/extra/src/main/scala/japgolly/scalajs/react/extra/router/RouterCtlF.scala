package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react._
import japgolly.scalajs.react.internal.CoreGeneral._
import japgolly.scalajs.react.internal.EffectUtil
import japgolly.scalajs.react.util.Effect.Sync
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.html

/** Router controller. A client API to the router.
  *
  * @tparam Route A data type that indicates a route that can be navigated to.
  */
abstract class RouterCtlF[F[_], Route] {

  protected implicit def F: Sync[F]

  def baseUrl: BaseUrl
  def byPath: RouterCtlF[F, Path]
  def refresh: F[Unit]
  def pathFor(route: Route): Path
  def set(route: Route, via: SetRouteVia): F[Unit]

  def withEffect[G[_]](implicit G: Sync[G]): RouterCtlF[G, Route] =
    G.subst[F, ({type L[E[_]] = RouterCtlF[E, Route]})#L](this)(
      new RouterCtlF.AltEffect(this))

  final def set(route: Route): F[Unit] =
    set(route, SetRouteVia.HistoryPush)

  final def urlFor(route: Route): AbsUrl =
    pathFor(route).abs(baseUrl)

  final def setEH(route: Route): ReactEvent => F[Unit] =
    e => EffectUtil.asEventDefault_(e)(set(route))

  final def setOnClick(route: Route): TagMod =
    ^.onClick ==> setEH(route)

  final def onLinkClick(route: Route): ReactMouseEvent => Option[F[Unit]] =
    e => Option.unless(ReactMouseEvent targetsNewTab_? e)(setEH(route)(e))

  final def setOnLinkClick(route: Route): TagMod =
    ^.onClick ==> onLinkClick(route).andThen(_.getOrElse(F.empty))

  final def link(route: Route): VdomTagOf[html.Anchor] =
    <.a(^.href := urlFor(route).value, setOnLinkClick(route))

  @deprecated("Use .onSetRun(callback).setOnClick(route)", "forever")
  final def setOnClick(route: Route, callback: F[Unit]): TagMod =
    onSetRun(callback).setOnClick(route)

  @deprecated("Use .onSetRun(callback).setOnLinkClick(route)", "forever")
  final def setOnLinkClick(route: Route, callback: F[Unit]): TagMod =
    onSetRun(callback).setOnLinkClick(route)

  @deprecated("Use .onSetRun(callback).link(route)", "forever")
  final def link(route: Route, callback: F[Unit]): VdomTagOf[html.Anchor] =
    onSetRun(callback).link(route)

  final def contramap[B](f: B => Route): RouterCtlF[F, B] =
    new RouterCtlF.Contramap(this, f)

  /**
   * Change the behaviour of [[set()]] and all derivatives.
   *
   * For example, this can be used to set a component's state immediately before setting a new route.
   */
  final def onSet(f: (Route, F[Unit]) => F[Unit]): RouterCtlF[F, Route] =
    new RouterCtlF.ModCB(this, f)

  /**
   * Change the behaviour of [[set()]] and all derivatives.
   *
   * For example, this can be used to set a component's state immediately before setting a new route.
   */
  final def onSet(f: F[Unit] => F[Unit]): RouterCtlF[F, Route] =
    onSet((_, cb) => f(cb))

  /** Return a new version of this that executes the specified callback after setting new routes. */
  final def onSetRun(f: F[Unit]): RouterCtlF[F, Route] =
    onSet(F.chain(_, f))

  final def narrow[B <: Route]: RouterCtlF[F, B] =
    contramap(b => b)
}

object RouterCtlF {

  private def reuseF[F[_]]: Reusability[RouterCtlF[F, Any]] = {
    def test[A, B](x: RouterCtlF[F, A], y: RouterCtlF[F, B]): Boolean =
      (x eq y) || // Most common case and fastest
      ((x, y) match {
        case (Contramap(ac, af), Contramap(bc, bf)) => (af eq bf) && test(ac, bc)
        case _                                      => false
      })
    Reusability[RouterCtlF[F, Any]](test)
  }

  private[this] lazy val reuse = reuseF[List]

  implicit def reusability[F[_], A]: Reusability[RouterCtlF[F, A]] =
    reuse.asInstanceOf[Reusability[RouterCtlF[F, A]]]

  final case class Contramap[F[_], A, B](u: RouterCtlF[F, A], f: B => A)(implicit x: Sync[F]) extends RouterCtlF[F, B] {
    override protected implicit def F: Sync[F] = x
    override def baseUrl                       = u.baseUrl
    override def byPath                        = u.byPath
    override def refresh                       = u.refresh
    override def pathFor(b: B)                 = u pathFor f(b)
    override def set(b: B, v: SetRouteVia)     = u.set(f(b), v)
  }

  final case class ModCB[F[_], A](u: RouterCtlF[F, A], f: (A, F[Unit]) => F[Unit])(implicit x: Sync[F]) extends RouterCtlF[F, A] {
    override protected implicit def F: Sync[F] = x
    override def baseUrl                       = u.baseUrl
    override def byPath                        = u.byPath
    override def refresh                       = u.refresh
    override def pathFor(a: A)                 = u pathFor a
    override def set(a: A, v: SetRouteVia)     = f(a, u.set(a, v))
  }

  final case class AltEffect[F[_], G[_], A](u: RouterCtlF[F, A])(implicit G: Sync[G]) extends RouterCtlF[G, A] {
    import u.{F => FF}
    override protected implicit def F: Sync[G] = G
    override def baseUrl                       = u.baseUrl
    override def byPath                        = u.byPath.withEffect(G)
    override def refresh                       = G.transSync(u.refresh)
    override def pathFor(a: A)                 = u pathFor a
    override def set(a: A, v: SetRouteVia)     = G.transSync(u.set(a, v))

    override def withEffect[H[_]](implicit H: Sync[H]): RouterCtlF[H, A] =
      H.subst[G, ({type L[E[_]] = RouterCtlF[E, A]})#L](this)(
        H.subst[F, ({type L[E[_]] = RouterCtlF[E, A]})#L](u)(
          new RouterCtlF.AltEffect(u)(H)
        )
      )(G)
  }
}
