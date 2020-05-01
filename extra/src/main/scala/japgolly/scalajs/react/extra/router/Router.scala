package japgolly.scalajs.react.extra.router

import org.scalajs.dom
import scala.scalajs.js
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.internal.identityFn
import japgolly.scalajs.react.vdom.VdomElement

object Router {

  def apply[Page](baseUrl: BaseUrl, cfg: RouterConfig[Page]): Router[Page] =
    componentUnbuilt(baseUrl, cfg).build

  def componentUnbuilt[Page](baseUrl: BaseUrl, cfg: RouterConfig[Page]) =
    componentUnbuiltC(cfg, new RouterLogic(baseUrl, cfg))

  def componentUnbuiltC[Page](cfg: RouterConfig[Page], lgc: RouterLogic[Page]) =
    ScalaComponent.builder[Unit]("Router")
      .initialStateCallback   (lgc.syncToWindowUrl)
      .backend                (_ => new OnUnmount.Backend)
      .render_S               (lgc.render)
      .componentDidMount      ($ => cfg.postRenderFn(None, $.state.page))
      .componentDidUpdate     (i => cfg.postRenderFn(Some(i.prevState.page), i.currentState.page))
      .configure              (Listenable.listenToUnit(_ => lgc, $ => lgc.syncToWindowUrl.flatMap($.setState)))
      .configure              (EventListener.install("popstate", _ => lgc.ctl.refresh, _ => dom.window))
      .configureWhen(isIE11())(EventListener.install("hashchange", _ => lgc.ctl.refresh, _ => dom.window))

  private def isIE11(): Boolean =
    dom.window.navigator.userAgent.indexOf("Trident") != -1

  def componentAndLogic[Page](baseUrl: BaseUrl, cfg: RouterConfig[Page]): (Router[Page], RouterLogic[Page]) = {
    val l = new RouterLogic(baseUrl, cfg)
    val r = componentUnbuiltC(cfg, l).build
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

  type Action     = router.Action[Page]
  type Renderer   = router.Renderer[Page]
  type Redirect   = router.Redirect[Page]
  type Resolution = router.Resolution[Page]

  import RouteCmd._
  import dom.window
  import cfg.logger

  @inline protected implicit def impbaseurl: BaseUrl = baseUrl

  @inline protected def log(msg: => String) = Log(() => msg)

  val syncToWindowUrl: CallbackTo[Resolution] =
   for {
     url <- CallbackTo(AbsUrl.fromWindow)
     _   <- logger(s"Syncing to $url.")
     cmd <- syncToUrl(url)
     res <- interpret(cmd)
     _   <- logger(s"Resolved to page ${res.page}.")
     _   <- logger("")
   } yield res

//  val syncToWindowUrlS: ReactST[IO, Resolution, Unit] =
//    ReactS.setM(syncToWindowUrl) //addCallbackS onSync

  def syncToUrl(url: AbsUrl): CallbackTo[RouteCmd[Resolution]] =
    parseUrl(url) match {
      case Some(path) => syncToPath(path)
      case None       => wrongBase(url)
    }

  def wrongBase(wrongUrl: AbsUrl): CallbackTo[RouteCmd[Resolution]] = {
    val root = Path.root
    redirectToPath(root, SetRouteVia.HistoryPush).map(
      log(s"Wrong base: $wrongUrl is outside of ${root.abs}.") >> _)
  }

  def parseUrl(url: AbsUrl): Option[Path] =
    if (url.value startsWith baseUrl.value)
      Some(Path(url.value.substring(baseUrl.value.length)))
    else
      None

  def syncToPath(path: Path): CallbackTo[RouteCmd[Resolution]] =
    for {
      parsed <- cfg.rules.parse(path)
      cmd    <- parsed match {
                  case Right(page) => resolveActionForPage(path, page)
                  case Left(r)     => redirect(r)
                }
    } yield log(s"Parsed $path to $parsed.") >> cmd

  def resolveActionForPage(path: Path, page: Page): CallbackTo[RouteCmd[Resolution]] =
    for {
      action <- cfg.rules.action(path, page)
      cmd    <- resolveAction(page, action)
    } yield log(s"Action for page $page at $path is $action.") >> cmd

  def resolveAction(page: Page, action: Action): CallbackTo[RouteCmd[Resolution]] =
    for {
      a <- resolveAction(action)
    } yield cmdOrPure(a.map(r => Resolution(page, () => r(ctl))))

  def resolveAction(a: Action): CallbackTo[Either[RouteCmd[Resolution], Renderer]] =
    a match {
      case r: Renderer => CallbackTo pure Right(r)
      case r: Redirect => redirect(r).map(Left(_))
    }

  def redirect(r: Redirect): CallbackTo[RouteCmd[Resolution]] =
    r match {
      case RedirectToPage(page, m) => redirectToPath(cfg.rules.path(page), m)
      case RedirectToPath(path, m) => redirectToPath(path, m)
    }

  def redirectToPath(path: Path, via: SetRouteVia): CallbackTo[RouteCmd[Resolution]] =
    for {
      syncCmd <- syncToUrl(path.abs)
    } yield
      log(s"Redirecting to ${path.abs} via $via.") >>
        RouteCmd.setRoute(path.abs, via) >> syncCmd

  private def cmdOrPure[A](e: Either[RouteCmd[A], A]): RouteCmd[A] =
    e.fold(identityFn, Return(_))

  def interpret[A](r: RouteCmd[A]): CallbackTo[A] = {
    @inline def hs = js.Dynamic.literal()
    @inline def ht = ""
    @inline def h = window.history
    r match {

      case PushState(url) =>
        CallbackTo(h.pushState(hs, ht, url.value)) << logger(s"PushState: [${url.value}]")

      case ReplaceState(url) =>
        CallbackTo(h.replaceState(hs, ht, url.value)) << logger(s"ReplaceState: [${url.value}]")

      case SetWindowLocation(url) =>
        CallbackTo(window.location.href = url.value) << logger(s"SetWindowLocation: [${url.value}]")

      case BroadcastSync =>
        broadcast(()) << logger("Broadcasting sync request.")

      case Return(a) =>
        CallbackTo.pure(a)

      case Log(msg) =>
        logger(msg())

      case Sequence(a, b) =>
        a.foldLeft[CallbackTo[_]](Callback.empty)(_ >> interpret(_)) >> interpret(b)
    }
  }

  def render(r: Resolution): VdomElement =
    cfg.renderFn(ctl, r)

  def setPath(path: Path, via: SetRouteVia): RouteCmd[Unit] =
    log(s"Set route to $path via $via") >>
      RouteCmd.setRoute(path.abs, via) >> BroadcastSync

  val ctlByPath: RouterCtl[Path] =
    new RouterCtl[Path] {
      override def baseUrl                      = impbaseurl
      override def byPath                       = this
      override val refresh                      = interpret(BroadcastSync)
      override def pathFor(path: Path)          = path
      override def set(p: Path, v: SetRouteVia) = interpret(setPath(p, v))
    }

  val ctl: RouterCtl[Page] =
    ctlByPath contramap cfg.rules.path
}
