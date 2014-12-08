package japgolly.scalajs.react.extras.router

import japgolly.scalajs.react._
import japgolly.scalajs.react.extras._
import japgolly.scalajs.react.ScalazReact._
import org.scalajs.dom._
import scala.scalajs.js
import scalaz.{\/, ~>, Free}
import scalaz.effect.IO
import scalaz.syntax.bind._
import scalaz.syntax.std.option._

final case class BaseUrl(value: String)

object Router {

  @inline def component[P](base: BaseUrl, p: Page[P]) =
    componentR(new Router(base, p.root, p.paths))

  def componentR[P](router: Router[P]) = {
    val rootDom = router.root renderer router
    val doms    = router.paths.map(p => (p: Route[P], p renderer router)).toMap + (router.root -> rootDom)
    ReactComponentB[Unit]("Router")
      .initialState(router.readIO.unsafePerformIO())
      .backend(_ => new OnUnmount.Backend)
      .render((_, route, _) => doms.getOrElse(route, rootDom))
      .componentWillMount(_ => router.init.unsafePerformIO())
      .configure(Listenable.installF(_ => router, (_: Unit) => router.readS))
      .buildU
  }
}

class Router[P](val base: BaseUrl, val root: Root[P], val paths: Seq[Path[P]]) extends Broadcaster[Unit] {

  final type RouteCmdP[A] = RouteCmd[P, A]
  final type RouteProgP[A] = RouteProg[P, A]

  @inline final private def regexEscape(s: String) =
    s.replaceAll("""([-()\[\]{}+?*.$\^|,:#<!\\])""", """\\$1""").replaceAll("""\u0008""", """\\u0008""")

  private val urlParser =
    s"""^(?://|[^/]+?)+?${regexEscape(base.value)}(.*)$$""".r

  val routes: Seq[Route[P]] =
    root +: paths

  def route(url: String): Option[Route[P]] =
    url match {
      case urlParser(path) => routes.find(_.path == path)
      case _               => None
    }

  def read(url: String): RouteCmdP[Route[P]] \/ Route[P] =
    route(url) toRightDisjunction ReplaceState(base, root)

  def readIO: IO[Route[P]] =
    IO(window.location.href)
      .flatMap(url => read(url).fold(interpret(_), IO(_)))

  def readS =
    ReactS.setM(readIO)

  def set(r: Route[P]): RouteProgP[Unit] =
    PushState(base, r) >> BroadcastRouteChange

  def setIO(r: Route[P]): IO[Unit] =
    interpret(set(r))

  def setEH(r: Route[P]): ReactEvent => IO[Unit] =
    e => preventDefaultIO(e) >> stopPropagationIO(e) >> setIO(r)

  @inline private def eo = js.Dynamic.literal()

  @inline private def mkurl(r: Route[P]) = base.value + r.path

  private[this] val cmdinterp: RouteCmdP ~> IO = new (RouteCmdP ~> IO) {
    @inline private def hs = js.Dynamic.literal()
    @inline private def ht = ""
    override def apply[A](m: RouteCmdP[A]): IO[A] = m match {
      case PushState(b, r)      => IO{ window.history.pushState   (hs, ht, mkurl(r)); r }
      case ReplaceState(b, r)   => IO{ window.history.replaceState(hs, ht, mkurl(r)); r }
      case BroadcastRouteChange => IO{ broadcast(()) }
    }
  }

  def interpret[A](r: RouteProgP[A]): IO[A] =
    Free.runFC[RouteCmdP, IO, A](r)(cmdinterp)

  val init: IO[Unit] = {
    var need = true
    IO(
      if (need) {
        window.onpopstate = (_: PopStateEvent) => broadcast(())
        need = false
      }
    )
  }

  import vdom.prefix_<^._
  def setonclick(r: Route[P]) = ^.onclick ~~> setEH(r)
  def link(r: Route[P]) = <.a(^.href := mkurl(r), setonclick(r))
}

sealed trait RouteCmd[+P, A]
case class PushState[P](b: BaseUrl, r: Route[P]) extends RouteCmd[P, Route[P]]
case class ReplaceState[P](b: BaseUrl, r: Route[P]) extends RouteCmd[P, Route[P]]
case object BroadcastRouteChange extends RouteCmd[Nothing, Unit]
