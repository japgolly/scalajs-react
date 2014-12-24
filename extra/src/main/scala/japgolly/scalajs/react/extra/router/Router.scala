package japgolly.scalajs.react.extra.router

import org.scalajs.dom._
import scala.scalajs.js
import scalaz.{\/-, -\/, \/, ~>, Free}
import scalaz.effect.IO
import scalaz.syntax.bind.ToBindOps
import japgolly.scalajs.react._, vdom.prefix_<^._, ScalazReact._
import japgolly.scalajs.react.extra._
import RouteCmd._

object Router {

  type Component[P] = ReactComponentC.ConstProps[Unit, Location[P], Any, TopNode]

  def componentUnbuilt[P](router: Router[P]) =
    ReactComponentB[Unit]("Router")
      .initialState(router.syncToWindowUrl.unsafePerformIO())
      .backend(_ => new OnUnmount.Backend)
      .render((_, route, _) => route.render(router))
      .componentWillMount(_ => router.init.unsafePerformIO())
      .configure(Listenable.installSF(_ => router, (_: Unit) => router.syncToWindowUrlS))

  def component[P](router: Router[P]): Component[P] =
    componentUnbuilt(router).buildU

  type Logger = String => IO[Unit]

  def consoleLogger: Logger =
    s => IO(console.log(s"[Router] $s"))

  val nopLogger: Logger =
    Function const IO(())
}

/**
 * Performs all routing logic.
 *
 * @param baseUrl The prefix of all routes in a set.
 * @param pathAction Determines the appropriate response to a route path.
 * @tparam P Routing rules context. Prevents different routing rule sets being mixed up.
 */
final class Router[P](val baseUrl: BaseUrl,
                      val pathAction: Path => RouteAction[P],
                      logger: Router.Logger) extends Broadcaster[Unit] {

  type Cmd[A]  = RouteCmd[P, A]
  type Prog[A] = RouteProg[P, A]
  type Loc     = Location[P]

  @inline protected implicit def impbaseurl: BaseUrl = baseUrl

  @inline protected def log(msg: => String) = Log(() => msg)

  val init: IO[Unit] = {
    var need = true
    IO(
      if (need) {
        window.onpopstate = (_: PopStateEvent) => broadcast(())
        need = false
        logger(s"Installed onpopstate event handler.").unsafePerformIO()
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
    IO(AbsUrl.fromWindow).flatMap(url =>
      logger(s"Syncing to [${url.value}].") >>
        syncToUrl(url).fold(
          p => interpret(p),
          l => IO(l) << logger(s"Location found for path [${l.path.value}]."))
    )

  def syncToWindowUrlS: ReactST[IO, Loc, Unit] =
    ReactS.setM(syncToWindowUrl)

  def wrongBase(wrongUrl: AbsUrl): Prog[Loc] = {
    val url = AbsUrl(baseUrl.value)
    log(s"Wrong base: [${wrongUrl.value}] is outside of [${baseUrl.value}].") >>
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
      case PushState(url)     => IO(window.history.pushState   (hs, ht, url.value)) << logger(s"PushState: [${url.value}]")
      case ReplaceState(url)  => IO(window.history.replaceState(hs, ht, url.value)) << logger(s"ReplaceState: [${url.value}]")
      case BroadcastLocChange => IO(broadcast(())) << logger("Broadcasting location change.")
      case ReturnLoc(loc)     => IO(loc)
      case Log(msg)           => logger(msg())
    }
  }

  def interpret[A](r: Prog[A]): IO[A] =
    Free.runFC[Cmd, IO, A](r)(interpretCmd)

  def set(p: ApprovedPath[P]): Prog[Unit] =
    log(s"Set route to path [${p.path.value}].") >>
      PushState[P](p.path.abs) >> BroadcastLocChange

  def setIO(p: ApprovedPath[P]): IO[Unit] =
    interpret(set(p))

  def setEH(p: ApprovedPath[P]): ReactEvent => IO[Unit] =
    e => preventDefaultIO(e) >> stopPropagationIO(e) >> setIO(p)

  def setOnClick(p: ApprovedPath[P]): TagMod =
    ^.onClick ~~> setEH(p)

  def link(p: ApprovedPath[P]): ReactTag =
    <.a(^.href := p.path.abs.value, setOnClick(p))
}