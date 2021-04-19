package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react._
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
  def refresh: Callback
  def pathFor(route: Route): Path
  def set(route: Route, via: SetRouteVia): Callback

  final def set(route: Route): Callback =
    set(route, SetRouteVia.HistoryPush)

  final def urlFor(route: Route): AbsUrl =
    pathFor(route).abs(baseUrl)

  final def setEH(route: Route): ReactEvent => Callback =
    e => set(route).asEventDefault(e).void

  final def setOnClick(route: Route): TagMod =
    ^.onClick ==> setEH(route)

  final def onLinkClick(route: Route): ReactMouseEvent => CallbackOption[Unit] =
    e =>
      CallbackOption.unless(ReactMouseEvent targetsNewTab_? e) >>
        setEH(route)(e)

  final def setOnLinkClick(route: Route): TagMod =
    ^.onClick ==> onLinkClick(route).andThen(_.toCallback)

  final def link(route: Route): VdomTagOf[html.Anchor] =
    <.a(^.href := urlFor(route).value, setOnLinkClick(route))

  @deprecated("Use .onSetRun(callback).setOnClick(route)", "forever")
  final def setOnClick(route: Route, callback: Callback): TagMod =
    onSetRun(callback).setOnClick(route)

  @deprecated("Use .onSetRun(callback).setOnLinkClick(route)", "forever")
  final def setOnLinkClick(route: Route, callback: Callback): TagMod =
    onSetRun(callback).setOnLinkClick(route)

  @deprecated("Use .onSetRun(callback).link(route)", "forever")
  final def link(route: Route, callback: Callback): VdomTagOf[html.Anchor] =
    onSetRun(callback).link(route)

  final def contramap[B](f: B => Route): RouterCtl[B] =
    new RouterCtl.Contramap(this, f)

  /**
   * Change the behaviour of [[set()]] and all derivatives.
   *
   * For example, this can be used to set a component's state immediately before setting a new route.
   */
  final def onSet(f: (Route, Callback) => Callback): RouterCtl[Route] =
    new RouterCtl.ModCB(this, f)

  /**
   * Change the behaviour of [[set()]] and all derivatives.
   *
   * For example, this can be used to set a component's state immediately before setting a new route.
   */
  final def onSet(f: Callback => Callback): RouterCtl[Route] =
    onSet((_, cb) => f(cb))

  /** Return a new version of this that executes the specified callback after setting new routes. */
  final def onSetRun(f: Callback): RouterCtl[Route] =
    onSet(_ >> f)

  final def narrow[B <: Route]: RouterCtl[B] =
    contramap(b => b)
}

object RouterCtl {
  private lazy val reuse: Reusability[RouterCtl[Any]] =
    Reusability[RouterCtl[Any]] {
      case (a, b) if a eq b                       => true // First because most common case and fastest
      case (Contramap(ac, af), Contramap(bc, bf)) => (af eq bf) && reuse.test(ac, bc)
      case _                                      => false
    }

  implicit def reusability[A]: Reusability[RouterCtl[A]] =
    reuse.asInstanceOf[Reusability[RouterCtl[A]]]

  case class Contramap[A, B](u: RouterCtl[A], f: B => A) extends RouterCtl[B] {
    override def baseUrl                   = u.baseUrl
    override def byPath                    = u.byPath
    override def refresh                   = u.refresh
    override def pathFor(b: B)             = u pathFor f(b)
    override def set(b: B, v: SetRouteVia) = u.set(f(b), v)
  }

  case class ModCB[A](u: RouterCtl[A], f: (A, Callback) => Callback) extends RouterCtl[A] {
    override def baseUrl                   = u.baseUrl
    override def byPath                    = u.byPath
    override def refresh                   = u.refresh
    override def pathFor(a: A)             = u pathFor a
    override def set(a: A, v: SetRouteVia) = f(a, u.set(a, v))
  }
}
