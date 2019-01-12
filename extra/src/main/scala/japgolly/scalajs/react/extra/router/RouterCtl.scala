package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.html

/**
 * Router controller. A client API to the router.
 *
 * @tparam A A data type that indicates a route that can be navigated to.
 */
abstract class RouterCtl[A] {
  def baseUrl: BaseUrl
  def byPath: RouterCtl[Path]
  def refresh: Callback
  def pathFor(target: A): Path
  def set(target: A): Callback

  final def urlFor(target: A): AbsUrl =
    pathFor(target).abs(baseUrl)

  final def setEH(target: A): ReactEvent => Callback =
    e => set(target).asEventDefault(e).void

  final def setOnClick(target: A): TagMod =
    ^.onClick ==> setEH(target)

  final def setOnLinkClick(target: A): TagMod = {
    def go(e: ReactMouseEvent): Callback =
      CallbackOption.unless(ReactMouseEvent targetsNewTab_? e) >>
        setEH(target)(e)
    ^.onClick ==> go
  }

  final def link(target: A): VdomTagOf[html.Anchor] =
    <.a(^.href := urlFor(target).value, setOnLinkClick(target))

  final def contramap[B](f: B => A): RouterCtl[B] =
    new RouterCtl.Contramap(this, f)

  /**
   * Change the behaviour of [[set()]] and all derivatives.
   *
   * For example, this can be used to set a component's state immediately before setting a new route.
   */
  final def onSet(f: (A, Callback) => Callback): RouterCtl[A] =
    new RouterCtl.ModCB(this, f)

  /**
   * Change the behaviour of [[set()]] and all derivatives.
   *
   * For example, this can be used to set a component's state immediately before setting a new route.
   */
  final def onSet(f: Callback => Callback): RouterCtl[A] =
    onSet((_, cb) => f(cb))

  final def narrow[B <: A]: RouterCtl[B] =
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
    override def baseUrl       = u.baseUrl
    override def byPath        = u.byPath
    override def refresh       = u.refresh
    override def pathFor(b: B) = u pathFor f(b)
    override def set(b: B)     = u set f(b)
  }

  case class ModCB[A](u: RouterCtl[A], f: (A, Callback) => Callback) extends RouterCtl[A] {
    override def baseUrl       = u.baseUrl
    override def byPath        = u.byPath
    override def refresh       = u.refresh
    override def pathFor(a: A) = u pathFor a
    override def set(a: A)     = f(a, u set a)
  }
}
