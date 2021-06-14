package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react._
import japgolly.scalajs.react.internal.EffectUtil
import japgolly.scalajs.react.util.DefaultEffects.Sync
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.html

/**
 * Router controller. A client API to the router.
 *
 * @tparam Route A data type that indicates a route that can be navigated to.
 */
abstract class RouterCtl[Route] {
  def baseUrl: BaseUrl
  def byPath: RouterCtl[Path]
  def refresh: Sync[Unit]
  def pathFor(route: Route): Path
  def set(route: Route, via: SetRouteVia): Sync[Unit]

  final def set(route: Route): Sync[Unit] =
    set(route, SetRouteVia.HistoryPush)

  final def urlFor(route: Route): AbsUrl =
    pathFor(route).abs(baseUrl)

  final def setEH(route: Route): ReactEvent => Sync[Unit] =
    e => EffectUtil.asEventDefault_(e)(set(route))

  final def setOnClick(route: Route): TagMod =
    ^.onClick ==> setEH(route)

  final def onLinkClick(route: Route): ReactMouseEvent => Option[Sync[Unit]] =
    e => Option.unless(ReactMouseEvent targetsNewTab_? e)(setEH(route)(e))

  final def setOnLinkClick(route: Route): TagMod =
    ^.onClick ==> onLinkClick(route).andThen(_.getOrElse(Sync.empty))

  final def link(route: Route): VdomTagOf[html.Anchor] =
    <.a(^.href := urlFor(route).value, setOnLinkClick(route))

  @deprecated("Use .onSetRun(callback).setOnClick(route)", "forever")
  final def setOnClick(route: Route, callback: Sync[Unit]): TagMod =
    onSetRun(callback).setOnClick(route)

  @deprecated("Use .onSetRun(callback).setOnLinkClick(route)", "forever")
  final def setOnLinkClick(route: Route, callback: Sync[Unit]): TagMod =
    onSetRun(callback).setOnLinkClick(route)

  @deprecated("Use .onSetRun(callback).link(route)", "forever")
  final def link(route: Route, callback: Sync[Unit]): VdomTagOf[html.Anchor] =
    onSetRun(callback).link(route)

  final def contramap[B](f: B => Route): RouterCtl[B] =
    new RouterCtl.Contramap(this, f)

  /**
   * Change the behaviour of [[set()]] and all derivatives.
   *
   * For example, this can be used to set a component's state immediately before setting a new route.
   */
  final def onSet(f: (Route, Sync[Unit]) => Sync[Unit]): RouterCtl[Route] =
    new RouterCtl.ModCB(this, f)

  /**
   * Change the behaviour of [[set()]] and all derivatives.
   *
   * For example, this can be used to set a component's state immediately before setting a new route.
   */
  final def onSet(f: Sync[Unit] => Sync[Unit]): RouterCtl[Route] =
    onSet((_, cb) => f(cb))

  /** Return a new version of this that executes the specified callback after setting new routes. */
  final def onSetRun(f: Sync[Unit]): RouterCtl[Route] =
    onSet(Sync.chain(_, f))

  final def narrow[B <: Route]: RouterCtl[B] =
    contramap(b => b)
}

object RouterCtl {
  private lazy val reuse: Reusability[RouterCtl[Any]] = {
    def test[A, B](x: RouterCtl[A], y: RouterCtl[B]): Boolean =
      (x eq y) || // Most common case and fastest
      ((x, y) match {
        case (Contramap(ac, af), Contramap(bc, bf)) => (af eq bf) && test(ac, bc)
        case _                                      => false
      })
    Reusability[RouterCtl[Any]](test)
  }

  implicit def reusability[A]: Reusability[RouterCtl[A]] =
    reuse.asInstanceOf[Reusability[RouterCtl[A]]]

  final case class Contramap[A, B](u: RouterCtl[A], f: B => A) extends RouterCtl[B] {
    override def baseUrl                   = u.baseUrl
    override def byPath                    = u.byPath
    override def refresh                   = u.refresh
    override def pathFor(b: B)             = u pathFor f(b)
    override def set(b: B, v: SetRouteVia) = u.set(f(b), v)
  }

  final case class ModCB[A](u: RouterCtl[A], f: (A, Sync[Unit]) => Sync[Unit]) extends RouterCtl[A] {
    override def baseUrl                   = u.baseUrl
    override def byPath                    = u.byPath
    override def refresh                   = u.refresh
    override def pathFor(a: A)             = u pathFor a
    override def set(a: A, v: SetRouteVia) = f(a, u.set(a, v))
  }
}
