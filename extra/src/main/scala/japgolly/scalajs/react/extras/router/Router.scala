package japgolly.scalajs.react.extras.router

import org.scalajs.dom._
import scala.scalajs.js
import scalaz.{\/-, -\/, \/, ~>, Free}
import scalaz.effect.IO
import scalaz.syntax.bind.ToBindOps
import japgolly.scalajs.react._, vdom.prefix_<^._, ScalazReact._
import japgolly.scalajs.react.extras._
import RouteCmd._

object Router {

  type Component[P] = ReactComponentC.ConstProps[Unit, Location[P], Any, TopNode]

  def component[P](router: Router[P]): Component[P] =
    ReactComponentB[Unit]("Router")
      .initialState(router.syncToWindowUrl.unsafePerformIO())
      .backend(_ => new OnUnmount.Backend)
      .render((_, route, _) => route.render(router))
      .componentWillMount(_ => router.init.unsafePerformIO())
      .configure(Listenable.installSF(_ => router, (_: Unit) => router.syncToWindowUrlS))
      .buildU
}


final class Router[P](val baseUrl: BaseUrl,
                      val pathAction: Path => RouteAction[P]) extends Broadcaster[Unit] {

  type Cmd[A]  = RouteCmd[P, A]
  type Prog[A] = RouteProg[P, A]
  type Loc     = Location[P]

  @inline protected implicit def impbaseurl: BaseUrl = baseUrl

  val init: IO[Unit] = {
    var need = true
    IO(
      if (need) {
        window.onpopstate = (_: PopStateEvent) => broadcast(())
        need = false
      }
    )
  }

  def parseUrl(url: AbsUrl): Option[Path] =
    if (url.value startsWith baseUrl.value)
      Some(Path(url.value.substring(baseUrl.value.length)))
    else
      None

  /**
   * @return How to synchronise the Router to a given url. Either a valid route, or a command that needs to be run.
   */
  def syncToUrl(url: AbsUrl): Prog[Loc] \/ Loc =
    parseUrl(url) match {
      case Some(path) => resolve(pathAction(path))
      case None       => -\/(wrongBase(url))
    }

  def syncToWindowUrl: IO[Loc] =
    IO(AbsUrl.fromWindow)
      .flatMap(url => syncToUrl(url).fold(interpret(_), IO(_)))

  def syncToWindowUrlS: ReactST[IO, Loc, Unit] =
    ReactS.setM(syncToWindowUrl)

  def wrongBase(url: AbsUrl): Prog[Loc] = {
    val url = AbsUrl(baseUrl.value)
    PushState[P](url) >> prog(syncToUrl(url))
  }

  def redirectCmd(p: Path, m: Redirect.Method): Cmd[Unit] = m match {
    case Redirect.Push    => PushState   [P](p.abs)
    case Redirect.Replace => ReplaceState[P](p.abs)
  }

  val resolve: RouteAction[P] => Prog[Loc] \/ Loc = {
    case l@ Location(_,_)       => \/-(l)
    case Redirect(\/-(loc),  m) => -\/( redirectCmd(loc.path, m) >> ReturnLoc(loc))
    case Redirect(-\/(path), m) => -\/( redirectCmd(path, m) >> prog(syncToUrl(path.abs)))
  }

  def prog(e: Prog[Loc] \/ Loc): Prog[Loc] =
    e.fold(identity, ReturnLoc(_))

  val interpretCmd: Cmd ~> IO = new (Cmd ~> IO) {
    @inline private def hs = js.Dynamic.literal()
    @inline private def ht = ""
    override def apply[A](m: Cmd[A]): IO[A] = m match {
      case PushState(url)     => IO(window.history.pushState   (hs, ht, url.value))
      case ReplaceState(url)  => IO(window.history.replaceState(hs, ht, url.value))
      case BroadcastLocChange => IO(broadcast(()))
      case ReturnLoc(loc)     => IO(loc)
    }
  }

  def interpret[A](r: Prog[A]): IO[A] =
    Free.runFC[Cmd, IO, A](r)(interpretCmd)

  def set(p: ApprovedPath[P]): Prog[Unit] =
    PushState(p.path.abs) >> BroadcastLocChange

  def setIO(p: ApprovedPath[P]): IO[Unit] =
    interpret(set(p))

  def setEH(p: ApprovedPath[P]): ReactEvent => IO[Unit] =
    e => preventDefaultIO(e) >> stopPropagationIO(e) >> setIO(p)

  def setOnClick(p: ApprovedPath[P]): TagMod =
    ^.onClick ~~> setEH(p)

  def link(p: ApprovedPath[P]): ReactTag =
    <.a(^.href := p.path.abs.value, setOnClick(p))
}