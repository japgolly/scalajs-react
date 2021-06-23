package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.util.DefaultEffects.Sync
import japgolly.scalajs.react.util.Util.identityFn
import japgolly.scalajs.react.vdom.VdomElement
import org.scalajs.dom
import scala.scalajs.js

object Router {

  def apply[Page](baseUrl: BaseUrl, cfg: RouterWithPropsConfig[Page, Unit]): Router[Page] =
    componentUnbuilt[Page](baseUrl, cfg).build

  def componentUnbuilt[Page](baseUrl: BaseUrl, cfg: RouterWithPropsConfig[Page, Unit]) =
    componentUnbuiltC[Page](cfg, new RouterLogic(baseUrl, cfg))

  def componentUnbuiltC[Page](cfg: RouterWithPropsConfig[Page, Unit], lgc: RouterLogic[Page, Unit]) =
    RouterWithProps.componentUnbuiltC(cfg, lgc)

  def componentAndLogic[Page](baseUrl: BaseUrl, cfg: RouterWithPropsConfig[Page, Unit]): (Router[Page], RouterLogic[Page, Unit]) = {
    val l = new RouterLogic[Page, Unit](baseUrl, cfg)
    val r = componentUnbuiltC[Page](cfg, l).build
    (r, l)
  }

  def componentAndCtl[Page](baseUrl: BaseUrl, cfg: RouterWithPropsConfig[Page, Unit]): (Router[Page], RouterCtl[Page]) = {
    val (r, l) = componentAndLogic[Page](baseUrl, cfg)
    (r, l.ctl)
  }
}

object RouterWithProps {
  def apply[Page, Props](baseUrl: BaseUrl, cfg: RouterWithPropsConfig[Page, Props]): RouterWithProps[Page, Props] =
    componentUnbuilt[Page, Props](baseUrl, cfg).build

  def componentUnbuilt[Page, Props](baseUrl: BaseUrl, cfg: RouterWithPropsConfig[Page, Props]) =
    componentUnbuiltC[Page, Props](cfg, new RouterLogic(baseUrl, cfg))

  def componentUnbuiltC[Page, Props](cfg: RouterWithPropsConfig[Page, Props], lgc: RouterLogic[Page, Props]) =
    ScalaComponent.builder[Props]("Router")
      .initialStateCallback   (lgc.syncToWindowUrl)
      .backend                (_ => new OnUnmount.Backend)
      .render                 ($ => lgc.render($.state, $.props))
      .componentDidMount      ($ => cfg.postRenderFn(None, $.state.page, $.props))
      .componentDidUpdate     (i => cfg.postRenderFn(Some(i.prevState.page), i.currentState.page, i.currentProps))
      .configure              (Listenable.listenToUnit(_ => lgc, $ => Sync.flatMap(lgc.syncToWindowUrl)($.setState(_))))
      .configure              (EventListener.install("popstate", _ => lgc.ctl.refresh, _ => dom.window))
      .configureWhen(isIE11())(EventListener.install("hashchange", _ => lgc.ctl.refresh, _ => dom.window))

  private def isIE11(): Boolean =
    dom.window.navigator.userAgent.indexOf("Trident") != -1

  def componentAndLogic[Page, Props](baseUrl: BaseUrl, cfg: RouterWithPropsConfig[Page, Props]): (RouterWithProps[Page, Props], RouterLogic[Page, Props]) = {
    val l = new RouterLogic[Page, Props](baseUrl, cfg)
    val r = componentUnbuiltC[Page, Props](cfg, l).build
    (r, l)
  }

  def componentAndCtl[Page, Props](baseUrl: BaseUrl, cfg: RouterWithPropsConfig[Page, Props]): (RouterWithProps[Page, Props], RouterCtl[Page]) = {
    val (r, l) = componentAndLogic[Page, Props](baseUrl, cfg)
    (r, l.ctl)
  }
}

/**
 * Performs all routing logic.
 *
 * @param baseUrl The prefix of all routes in a set.
 * @tparam Page Routing rules context. Prevents different routing rule sets being mixed up.
 */
final class RouterLogic[Page, Props](val baseUrl: BaseUrl, cfg: RouterWithPropsConfig[Page, Props]) extends Broadcaster[Unit] {

  type Action     = router.Action[Page, Props]
  type Renderer   = router.Renderer[Page, Props]
  type Redirect   = router.Redirect[Page]
  type Resolution = router.ResolutionWithProps[Page, Props]

  import RouteCmd._
  import dom.window
  import cfg.logger

  @inline protected implicit def impbaseurl: BaseUrl = baseUrl

  @inline protected def log(msg: => String) = Log(() => msg)

  val syncToWindowUrl: Sync[Resolution] =
    Sync.flatMap(Sync.delay(AbsUrl.fromWindow)           )(url =>
    Sync.flatMap(logger(s"Syncing to $url.")             )(_   =>
    Sync.flatMap(syncToUrl(url)                          )(cmd =>
    Sync.flatMap(interpret(cmd)                          )(res =>
    Sync.flatMap(logger(s"Resolved to page ${res.page}."))(_   =>
    Sync.map    (logger("")                              )(_   =>
      res
    ))))))

  def syncToUrl(url: AbsUrl): Sync[RouteCmd[Resolution]] =
    parseUrl(url) match {
      case Some(path) => syncToPath(path)
      case None       => wrongBase(url)
    }

  def wrongBase(wrongUrl: AbsUrl): Sync[RouteCmd[Resolution]] = {
    val root = Path.root
    Sync.map(redirectToPath(root, SetRouteVia.HistoryPush))(x =>
      log(s"Wrong base: $wrongUrl is outside of ${root.abs}.") >> x
    )
  }

  def parseUrl(url: AbsUrl): Option[Path] =
    if (url.value startsWith baseUrl.value)
      Some(Path(url.value.substring(baseUrl.value.length)))
    else
      None

  def syncToPath(path: Path): Sync[RouteCmd[Resolution]] =
    Sync.flatMap(cfg.rules.parse(path)) { parsed =>
      Sync.map(
        parsed match {
          case Right(page) => resolveActionForPage(path, page)
          case Left(r)     => redirect(r)
        }
      )(cmd =>
        log(s"Parsed $path to $parsed.") >> cmd
      )
    }

  def resolveActionForPage(path: Path, page: Page): Sync[RouteCmd[Resolution]] =
    Sync.flatMap(cfg.rules.action(path, page))(action =>
    Sync.map    (resolveAction(page, action) )(cmd =>
      log(s"Action for page $page at $path is $action.") >> cmd
    ))

  def resolveAction(page: Page, action: Action): Sync[RouteCmd[Resolution]] =
    Sync.map(resolveAction(action))(a =>
      cmdOrPure(a.map(r => ResolutionWithProps(page, r(ctl))))
    )

  def resolveAction(a: Action): Sync[Either[RouteCmd[Resolution], Renderer]] =
    a match {
      case r: Renderer => Sync.pure(Right(r))
      case r: Redirect => Sync.map(redirect(r))(Left(_))
    }

  def redirect(r: Redirect): Sync[RouteCmd[Resolution]] =
    r match {
      case RedirectToPage(page, m) => redirectToPath(cfg.rules.path(page), m)
      case RedirectToPath(path, m) => redirectToPath(path, m)
    }

  def redirectToPath(path: Path, via: SetRouteVia): Sync[RouteCmd[Resolution]] =
    Sync.map(syncToUrl(path.abs))(syncCmd =>
      log(s"Redirecting to ${path.abs} via $via.") >>
        RouteCmd.setRoute(path.abs, via) >> syncCmd
    )

  private def cmdOrPure[A](e: Either[RouteCmd[A], A]): RouteCmd[A] =
    e.fold(identityFn, Return(_))

  def interpret[A](r: RouteCmd[A]): Sync[A] = {
    @inline def hs = js.Dynamic.literal()
    @inline def ht = ""
    @inline def h = window.history
    r match {

      case PushState(url) =>
        Sync.chain(logger(s"PushState: [${url.value}]"), Sync.delay(h.pushState(hs, ht, url.value)))

      case ReplaceState(url) =>
        Sync.chain(logger(s"ReplaceState: [${url.value}]"), Sync.delay(h.replaceState(hs, ht, url.value)))

      case SetWindowLocation(url) =>
        Sync.chain(logger(s"SetWindowLocation: [${url.value}]"), Sync.delay(window.location.href = url.value))

      case BroadcastSync =>
        Sync.chain(logger("Broadcasting sync request."), broadcast(()))

      case Return(a) =>
        Sync.pure(a)

      case Log(msg) =>
        logger(msg())

      case Sequence(a, b) =>
        Sync.chain(a.foldLeft[Sync[_]](Sync.empty)((x, y) => Sync.chain(x, interpret(y))), interpret(b))
    }
  }

  def render(r: Resolution, props: Props): VdomElement =
    cfg.renderFn(ctl, r)(props)

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
