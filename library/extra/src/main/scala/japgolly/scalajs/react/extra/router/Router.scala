package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.React.startTransition
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.util.DefaultEffects
import japgolly.scalajs.react.util.Effect.Sync
import japgolly.scalajs.react.util.Util.identityFn
import japgolly.scalajs.react.vdom.VdomElement
import org.scalajs.dom
import scala.scalajs.js

object Router {

  def apply[F[_], Page](baseUrl: BaseUrl, cfg: RouterWithPropsConfigF[F, Page, Unit]): RouterF[F, Page] =
    componentUnbuilt[F, Page](baseUrl, cfg).build

  def componentUnbuilt[F[_], Page](baseUrl: BaseUrl, cfg: RouterWithPropsConfigF[F, Page, Unit]) =
    componentUnbuiltC[F, Page](cfg, new RouterLogicF(baseUrl, cfg))

  def componentUnbuiltC[F[_], Page](cfg: RouterWithPropsConfigF[F, Page, Unit], lgc: RouterLogicF[F, Page, Unit]) =
    RouterWithProps.componentUnbuiltC(cfg, lgc)

  def componentAndLogic[F[_], Page](baseUrl: BaseUrl, cfg: RouterWithPropsConfigF[F, Page, Unit]): (RouterF[F, Page], RouterLogicF[F, Page, Unit]) = {
    val l = new RouterLogicF[F, Page, Unit](baseUrl, cfg)
    val r = componentUnbuiltC[F, Page](cfg, l).build
    (r, l)
  }

  def componentAndCtl[F[_], Page](baseUrl: BaseUrl, cfg: RouterWithPropsConfigF[F, Page, Unit]): (RouterF[F, Page], RouterCtlF[F, Page]) = {
    val (r, l) = componentAndLogic[F, Page](baseUrl, cfg)
    (r, l.ctl)
  }
}

object RouterWithProps {
  def apply[F[_], Page, Props](baseUrl: BaseUrl, cfg: RouterWithPropsConfigF[F, Page, Props]): RouterWithPropsF[F, Page, Props] =
    componentUnbuilt[F, Page, Props](baseUrl, cfg).build

  def componentUnbuilt[F[_], Page, Props](baseUrl: BaseUrl, cfg: RouterWithPropsConfigF[F, Page, Props]) =
    componentUnbuiltC[F, Page, Props](cfg, new RouterLogicF(baseUrl, cfg))

  def componentUnbuiltC[F[_], Page, Props](cfg: RouterWithPropsConfigF[F, Page, Props], lgc: RouterLogicF[F, Page, Props]) = {
    import cfg.{effect => F}
    import DefaultEffects.Sync
    val EL = EventListenerF[F]

    ScalaComponent.builder[Props]("Router")
      .initialStateCallback   (lgc.syncToWindowUrl)
      .backend                (_ => OnUnmountF[F]())
      .render                 ($ => lgc.render($.state, $.props))
      .componentDidMount      ($ => cfg.postRenderFn(None, $.state.page, $.props))
      .componentDidUpdate     (i => cfg.postRenderFn(Some(i.prevState.page), i.currentState.page, i.currentProps))
      .configure              (ListenableF.listenToUnit(_ => lgc, $ => F.flatMap(lgc.syncToWindowUrl)(s => F.transSync(startTransition($.setState(s))))))
      .configure              (EL.install_("popstate", lgc.ctl.refresh, dom.window))
      .configureWhen(isIE11())(EL.install_("hashchange", lgc.ctl.refresh, dom.window))
  }

  private def isIE11(): Boolean =
    dom.window.navigator.userAgent.indexOf("Trident") != -1

  def componentAndLogic[F[_], Page, Props](baseUrl: BaseUrl, cfg: RouterWithPropsConfigF[F, Page, Props]): (RouterWithPropsF[F, Page, Props], RouterLogicF[F, Page, Props]) = {
    val l = new RouterLogicF[F, Page, Props](baseUrl, cfg)
    val r = componentUnbuiltC[F, Page, Props](cfg, l).build
    (r, l)
  }

  def componentAndCtl[F[_], Page, Props](baseUrl: BaseUrl, cfg: RouterWithPropsConfigF[F, Page, Props]): (RouterWithPropsF[F, Page, Props], RouterCtlF[F, Page]) = {
    val (r, l) = componentAndLogic[F, Page, Props](baseUrl, cfg)
    (r, l.ctl)
  }
}

/**
 * Performs all routing logic.
 *
 * @param baseUrl The prefix of all routes in a set.
 * @tparam Page Routing rules context. Prevents different routing rule sets being mixed up.
 */
final class RouterLogicF[F[_], Page, Props](val baseUrl: BaseUrl, cfg: RouterWithPropsConfigF[F, Page, Props]) extends BroadcasterF[F, Unit] {
  import cfg.{effect => F}

  type Action     = router.ActionF[F, Page, Props]
  type Renderer   = router.RendererF[F, Page, Props]
  type Redirect   = router.Redirect[Page]
  type Resolution = router.ResolutionWithProps[Page, Props]

  import RouteCmd._
  import dom.window

  override protected def listenableEffect = F

  @inline protected implicit def impbaseurl: BaseUrl = baseUrl

  @inline protected def log(msg: => String) = Log(() => msg)

  private def logger(s: => String): F[Unit] =
    F.fromJsFn0(cfg.logger(s))

  def withEffect[G[_]](implicit G: Sync[G]): RouterLogicF[G, Page, Props] =
    G.subst[F, ({type L[E[_]] = RouterLogicF[E, Page, Props]})#L](this)(
      new RouterLogicF(baseUrl, cfg.withEffect[G])
    )

  val syncToWindowUrl: F[Resolution] =
    F.flatMap(F.delay(AbsUrl.fromWindow)              )(url =>
    F.flatMap(logger(s"Syncing to $url.")             )(_   =>
    F.flatMap(syncToUrl(url)                          )(cmd =>
    F.flatMap(interpret(cmd)                          )(res =>
    F.flatMap(logger(s"Resolved to page ${res.page}."))(_   =>
    F.map    (logger("")                              )(_   =>
      res
    ))))))

  def syncToUrl(url: AbsUrl): F[RouteCmd[Resolution]] =
    parseUrl(url) match {
      case Some(path) => syncToPath(path)
      case None       => wrongBase(url)
    }

  def wrongBase(wrongUrl: AbsUrl): F[RouteCmd[Resolution]] = {
    val root = Path.root
    F.map(redirectToPath(root, SetRouteVia.HistoryPush))(x =>
      log(s"Wrong base: $wrongUrl is outside of ${root.abs}.") >> x
    )
  }

  def parseUrl(url: AbsUrl): Option[Path] =
    if (url.value startsWith baseUrl.value)
      Some(Path(url.value.substring(baseUrl.value.length)))
    else
      None

  def syncToPath(path: Path): F[RouteCmd[Resolution]] =
    F.flatMap(cfg.rules.parse(path)) { parsed =>
      F.map(
        parsed match {
          case Right(page) => resolveActionForPage(path, page)
          case Left(r)     => redirect(r)
        }
      )(cmd =>
        log(s"Parsed $path to $parsed.") >> cmd
      )
    }

  def resolveActionForPage(path: Path, page: Page): F[RouteCmd[Resolution]] =
    F.flatMap(cfg.rules.action(path, page))(action =>
    F.map    (resolveAction(page, action) )(cmd =>
      log(s"Action for page $page at $path is $action.") >> cmd
    ))

  def resolveAction(page: Page, action: Action): F[RouteCmd[Resolution]] =
    F.map(resolveAction(action))(a =>
      cmdOrPure(a.map(r => ResolutionWithProps(page, r(ctl))))
    )

  def resolveAction(a: Action): F[Either[RouteCmd[Resolution], Renderer]] =
    a match {
      case r@ RendererF(_) => F.pure(Right(r.withEffect[F]))
      case r: Redirect     => F.map(redirect(r))(Left(_))
    }

  def redirect(r: Redirect): F[RouteCmd[Resolution]] =
    r match {
      case RedirectToPage(page, m) => redirectToPath(cfg.rules.path(page), m)
      case RedirectToPath(path, m) => redirectToPath(path, m)
    }

  def redirectToPath(path: Path, via: SetRouteVia): F[RouteCmd[Resolution]] =
    F.map(syncToUrl(path.abs))(syncCmd =>
      log(s"Redirecting to ${path.abs} via $via.") >>
        RouteCmd.setRoute(path.abs, via) >> syncCmd
    )

  private def cmdOrPure[A](e: Either[RouteCmd[A], A]): RouteCmd[A] =
    e.fold(identityFn, Return(_))

  def interpret[A](r: RouteCmd[A]): F[A] = {
    @inline def hs = js.Dynamic.literal()
    @inline def ht = ""
    @inline def h = window.history
    r match {

      case PushState(url) =>
        F.chain(logger(s"PushState: [${url.value}]"), F.delay(h.pushState(hs, ht, url.value)))

      case ReplaceState(url) =>
        F.chain(logger(s"ReplaceState: [${url.value}]"), F.delay(h.replaceState(hs, ht, url.value)))

      case SetWindowLocation(url) =>
        F.chain(logger(s"SetWindowLocation: [${url.value}]"), F.delay(window.location.href = url.value))

      case BroadcastSync =>
        F.chain(logger("Broadcasting sync request."), broadcast(()))

      case Return(a) =>
        F.pure(a)

      case Log(msg) =>
        logger(msg())

      case Sequence(a, b) =>
        F.chain(a.foldLeft[F[Unit]](F.empty)((x, y) => F.chain(x, F.map(interpret(y))(_ => ()))), interpret(b))
    }
  }

  def render(r: Resolution, props: Props): VdomElement =
    cfg.renderFn(ctl, r)(props)

  def setPath(path: Path, via: SetRouteVia): RouteCmd[Unit] =
    log(s"Set route to $path via $via") >>
      RouteCmd.setRoute(path.abs, via) >> BroadcastSync

  val ctlByPath: RouterCtlF[F, Path] =
    new RouterCtlF[F, Path] {
      override protected implicit def F: Sync[F] = cfg.effect
      override def baseUrl                       = impbaseurl
      override def byPath                        = this
      override val refresh                       = interpret(BroadcastSync)
      override def pathFor(path: Path)           = path
      override def set(p: Path, v: SetRouteVia)  = interpret(setPath(p, v))
    }

  val ctl: RouterCtlF[F, Page] =
    ctlByPath contramap cfg.rules.path
}
