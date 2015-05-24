package japgolly.scalajs.react.extra.router2

import org.scalajs.dom
import scala.scalajs.js
import scalaz.{\/-, -\/, \/, ~>, Free}
import scalaz.effect.IO
import scalaz.syntax.bind.ToBindOps
import japgolly.scalajs.react._, vdom.prefix_<^._, ScalazReact._
import japgolly.scalajs.react.extra._

object Router {

  def apply[Page](baseUrl: BaseUrl, cfg: RouterConfig[Page]): Router[Page] =
    componentUnbuilt(baseUrl, cfg).buildU

  def componentUnbuilt[Page](baseUrl: BaseUrl, cfg: RouterConfig[Page]) =
    componentUnbuiltC(baseUrl, cfg, new RouterLogic(baseUrl, cfg))

  def componentUnbuiltC[Page](baseUrl: BaseUrl, cfg: RouterConfig[Page], lgc: RouterLogic[Page]) =
    ReactComponentB[Unit]("Router")
      .initialState(                      lgc.syncToWindowUrl.unsafePerformIO())
      .backend           (_            => new OnUnmount.Backend)
      .render            ((_, s, _)    => lgc.render(s))
      .componentWillMount(_            => lgc.init.unsafePerformIO())
      .componentDidMount ($            => cfg.postRenderFn(None, $.state.page).unsafePerformIO())
      .componentDidUpdate(($, _, prev) => cfg.postRenderFn(Some(prev.page), $.state.page).unsafePerformIO())
      .configure(Listenable.installS(_ => lgc, (_: Unit) => lgc.syncToWindowUrlS))

  def componentAndLogic[Page](baseUrl: BaseUrl, cfg: RouterConfig[Page]): (Router[Page], RouterLogic[Page]) = {
    val l = new RouterLogic(baseUrl, cfg)
    val r = componentUnbuiltC(baseUrl, cfg, l).buildU
    (r, l)
  }

  def componentAndCtl[Page](baseUrl: BaseUrl, cfg: RouterConfig[Page]): (Router[Page], RouterCtl[Page]) = {
    val (r, l) = componentAndLogic(baseUrl, cfg)
    (r, l.ctl)
  }
}

/**
 * Performs all routing logic.
 *
 * @param baseUrl The prefix of all routes in a set.
 * @tparam Page Routing rules context. Prevents different routing rule sets being mixed up.
 */
final class RouterLogic[Page](val baseUrl: BaseUrl, cfg: RouterConfig[Page]) extends Broadcaster[Unit] {

  type Action     = router2.Action[Page]
  type Renderer   = router2.Renderer[Page]
  type Redirect   = router2.Redirect[Page]
  type Resolution = router2.Resolution[Page]

  import RouteCmd._
  import dom.window
  import cfg.logger

  @inline protected implicit def impbaseurl: BaseUrl = baseUrl

  @inline protected def log(msg: => String) = Log(() => msg)

  val init: IO[Unit] = {
    var initPending = true
    IO(
      if (initPending) {
        window.onpopstate = (_: dom.PopStateEvent) => broadcast(())
        initPending = false
        logger(s"Installed onpopstate event handler.").unsafePerformIO()
      }
    )
  }

  def syncToWindowUrl: IO[Resolution] =
   for {
     url <- IO(AbsUrl.fromWindow)
     _   <- logger(s"Syncing to [${url.value}].")
     res <- interpret(syncToUrl(url))
     _   <- logger(s"Resolved to page: [${res.page}].")
   } yield res

  def syncToWindowUrlS: ReactST[IO, Resolution, Unit] =
    ReactS.setM(syncToWindowUrl) //addCallbackS onSync

  def syncToUrl(url: AbsUrl): RouteProg[Resolution] =
    parseUrl(url) match {
      case Some(path) => syncToPath(path)
      case None       => wrongBase(url)
    }

  def wrongBase(wrongUrl: AbsUrl): RouteProg[Resolution] = {
    val root = Path.root
    log(s"Wrong base: [${wrongUrl.value}] is outside of [${root.abs}].") >>
      redirectToPath(root, Redirect.Push)
  }

  def parseUrl(url: AbsUrl): Option[Path] =
    if (url.value startsWith baseUrl.value)
      Some(Path(url.value.substring(baseUrl.value.length)))
    else
      None

  def syncToPath(path: Path): RouteProg[Resolution] =
    cfg.parse(path) match {
      case \/-(page) => resolve(page, cfg action page)
      case -\/(r)    => redirect(r)
    }

  def redirectCmd(p: Path, m: Redirect.Method): RouteCmd[Unit] = m match {
    case Redirect.Push    => PushState   (p.abs)
    case Redirect.Replace => ReplaceState(p.abs)
  }

  def resolve(page: Page, action: Action): RouteProg[Resolution] =
    prog(resolveAction(action).map(r => Resolution(page, () => r(ctl))))

  def resolveAction(a: Action): RouteProg[Resolution] \/ Renderer = a match {
    case r: Renderer => \/-(r)
    case r: Redirect => -\/(redirect(r))
  }

  def redirect(r: Redirect): RouteProg[Resolution] = r match {
    case RedirectToPage(page, m) => redirectToPath(cfg path page, m)
    case RedirectToPath(path, m) => redirectToPath(path, m)
  }

  def redirectToPath(path: Path, method: Redirect.Method): RouteProg[Resolution] =
    //log(s"Redirecting to [${path.value}], method=$method.") >>
    redirectCmd(path, method) >> syncToUrl(path.abs)

  private def prog[A](e: RouteProg[A] \/ A): RouteProg[A] =
    e.fold(identity, Return(_))

  val interpretCmd: RouteCmd ~> IO = new (RouteCmd ~> IO) {
    @inline private def hs = js.Dynamic.literal()
    @inline private def ht = ""
    @inline private def h = window.history
    override def apply[A](m: RouteCmd[A]): IO[A] = m match {
      case PushState(url)    => IO(h.pushState   (hs, ht, url.value)) << logger(s"PushState: [${url.value}]")
      case ReplaceState(url) => IO(h.replaceState(hs, ht, url.value)) << logger(s"ReplaceState: [${url.value}]")
      case BroadcastSync     => IO(broadcast(()))                     << logger("Broadcasting sync request.")
      case Return(a)         => IO(a)
      case Log(msg)          => logger(msg())
    }
  }

  def interpret[A](r: RouteProg[A]): IO[A] =
    Free.runFC[RouteCmd, IO, A](r)(interpretCmd)

  def render(r: Resolution): ReactElement =
    cfg.renderFn(ctl, r)

  def setPath(path: Path): RouteProg[Unit] =
    log(s"Set route to path [${path.value}].") >>
      PushState(path.abs) >> BroadcastSync

  val ctlByPath: RouterCtl[Path] =
    new RouterCtl[Path] {
      override def baseUrl             = impbaseurl
      override def byPath              = this
      override def refreshIO           = interpret(BroadcastSync)
      override def pathFor(path: Path) = path
      override def setIO(path: Path)   = interpret(setPath(path))
    }

  val ctl: RouterCtl[Page] =
    ctlByPath contramap cfg.path
}

/**
 * Router controller. A client API to the router.
 *
 * @tparam A A data type that indicates a route that can be navigated to.
 */
abstract class RouterCtl[A] {
  def baseUrl: BaseUrl
  def byPath: RouterCtl[Path]
  def refreshIO: IO[Unit]
  def pathFor(target: A): Path
  def setIO(target: A): IO[Unit]

  final def urlFor(target: A): AbsUrl =
    pathFor(target).abs(baseUrl)

  final def setEH(target: A): ReactEvent => IO[Unit] =
    e => preventDefaultIO(e) >> stopPropagationIO(e) >> setIO(target)

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
  implicit def reusability[A]: Reusability[RouterCtl[A]] =
    Reusability.byRef

  class Contramap[A, B](u: RouterCtl[A], f: B => A) extends RouterCtl[B] {
    override def baseUrl       = u.baseUrl
    override def byPath        = u.byPath
    override def refreshIO     = u.refreshIO
    override def pathFor(b: B) = u pathFor f(b)
    override def setIO(b: B)   = u setIO f(b)
  }
}
