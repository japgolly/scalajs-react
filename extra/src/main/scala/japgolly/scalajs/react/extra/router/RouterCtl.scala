package japgolly.scalajs.react.extra.router

import scalaz.effect.IO
import scalaz.syntax.bind.ToBindOps
import japgolly.scalajs.react._
import japgolly.scalajs.react.ScalazReact._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.prefix_<^._

/**
 * Router controller. A client API to the router.
 *
 * @tparam A A data type that indicates a route that can be navigated to.
 */
abstract class RouterCtl[A] {
  def baseUrl: BaseUrl
  def byPath: RouterCtl[Path]
  def refresh: IO[Unit]
  def pathFor(target: A): Path
  def set(target: A): IO[Unit]

  final def urlFor(target: A): AbsUrl =
    pathFor(target).abs(baseUrl)

  final def setEH(target: A): ReactEvent => IO[Unit] =
    e => preventDefaultIO(e) >> stopPropagationIO(e) >> set(target)

  final def setOnClick(target: A): TagMod =
    ^.onClick ~~> setEH(target)

  final def link(target: A): ReactTag =
    <.a(^.href := urlFor(target).value, setOnClick(target))

  final def contramap[B](f: B => A): RouterCtl[B] =
    new RouterCtl.Contramap(this, f)

  final def narrow[B <: A]: RouterCtl[B] =
    contramap(b => b)
}

object RouterCtl {
  private lazy val reuse: Reusability[RouterCtl[Any]] =
    Reusability.fn[RouterCtl[Any]]{
      case (a, b) if a eq b => true // First because most common case and fastest
      case (Contramap(ac, af), Contramap(bc, bf)) => (af eq bf) && reuse.test(ac, bc)
      case _ => false
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
}
