package japgolly.scalajs.react.extra.router

import scalaz.{-\/, \/-, \/}
import japgolly.scalajs.react.{ReactElement, ReactComponentC, TopNode}

/**
 * DSL for specifying a set of routing rules.
 */
trait RoutingRules {
  final type P              = this.type
  final type Renderer       = japgolly.scalajs.react.extra.router.Renderer[P]
  final type Router         = japgolly.scalajs.react.extra.router.Router[P]
  final type Loc            = Location[P]
  final type RedirectTarget = Redirect.Target[P]

  @inline final protected implicit def componentP_renderer[S,B,T<:TopNode](c: ReactComponentC.ReqProps[Router, S, B, T]): Renderer = c(_)
  @inline final protected implicit def componentU_renderer[P,S,B,T<:TopNode](c: ReactComponentC.ConstProps[P, S, B, T]): Renderer = _ => c()
  @inline final protected implicit def element_renderer[A <% ReactElement](a: A): Renderer = _ => a
  @inline final protected implicit def loc_redirectable(a: Loc): RedirectTarget = \/-(a)
  @inline final protected implicit def path_redirectable(p: Path): RedirectTarget = -\/(p)

  private[this] def totalParser: Path => RouteAction[P] =
    p => parseS(p) orElse parseD(p) getOrElse notFound(p)

  private[this] var staticRoutes = Map.empty[Path, RouteAction[P]]
  private[this] val parseS: Path => Option[RouteAction[P]] = p => staticRoutes.get(p)

  private type DRouteFn = Path => Option[RouteAction[P]]
  private[this] var dynRoutes = Vector.empty[DRouteFn]
  private[this] val parseD: DRouteFn = p => dynRoutes.foldLeft(None: Option[RouteAction[P]])(_ orElse _(p))

  // ===================================================================================================================
  // Interception

  protected case class InterceptionR(loc: Loc, router: Router, element: ReactElement)

  protected def interceptRender(i: InterceptionR): ReactElement = i.element

  private def mkloc(path: Path, render: Renderer): Loc = {
    lazy val l: Loc = Location(path, r => interceptRender(InterceptionR(l, r, render(r))))
    l
  }

  // ===================================================================================================================
  // Actions

  final type DynAction = Path => RouteAction[P]

  /**
   * The catch-all response to unmatched routes.
   */
  protected val notFound: DynAction

  final protected def render(render: Renderer): DynAction =
    path => mkloc(path, render)

  final protected def redirect(to: Loc, method: Redirect.Method): DynAction =
    _ => Redirect(to, method)

  // ===================================================================================================================
  //  Static Routes

  /** An unregistered static route. Install via `register()`. */
  protected case class StaticRoute[A <: RouteAction[P]](path: Path, action: A)

  final protected def register[A <: RouteAction[P]](u: StaticRoute[A]): A = {
    import u.{path, action}
    staticRoutes.get(path) match {
      case Some(prev) =>
        throw new ExceptionInInitializerError(s"Attempted to register two routes with the name '${path.value}' in $this.\n1) $prev\n2) $action")
      case None =>
        staticRoutes += path -> u.action
        action
    }
  }

  final protected def rootLocation(r: Renderer): StaticRoute[Loc] =
    location("", r)

  final protected def location(path: String, render: Renderer): StaticRoute[Loc] = {
    val p = Path(path)
    StaticRoute(p, mkloc(p, render))
  }

  final protected def redirection(from: String, to: RedirectTarget, method: Redirect.Method): StaticRoute[Redirect[P]] =
    StaticRoute(Path(from), Redirect(to, method))

  // ===================================================================================================================
  //  Dynamic Routes

  /** An unregistered dynamic route. Install via `register()`. */
  protected case class DynamicRoute(route: DRouteFn)

  final protected def register(u: DynamicRoute): Unit =
    dynRoutes :+= u.route

  /**
   * Parser for a dynamic path. Example: `"person/123"`
   *
   * @tparam T The value of the dynamic portion of the path. Example: `"123"` or `PersonId(123)`.
   */
  final protected def parser[T](pf: PartialFunction[String, T]): DynB[T] =
    new DynB[T](pf.lift.compose[Path](_.value))

  final protected class DynB[T](parse: Path => Option[T]) {

    private def dynamicRoute(f: (Path, T) => RouteAction[P]): DynamicRoute =
      new DynamicRoute(path => parse(path).map(t => f(path, t)))

    def thenMatch(f: T => DynAction): DynamicRoute =
      dynamicRoute((p, t) => f(t)(p))

    /**
     * Note that a `Location[P]` is not returned. In order to create links to this location,
     * use `this.dynLink()`.
     */
    def location(f: T => Renderer): DynamicRoute =
      dynamicRoute((p, t) => mkloc(p, f(t)))

    def redirection(f: T => (RedirectTarget, Redirect.Method)): DynamicRoute =
      dynamicRoute((_, t) => {
        val (l, m) = f(t)
        Redirect(l, m)
      })
  }

  /**
   * Generates paths for a dynamic route, which can then be passed to [[japgolly.scalajs.react.extra.router.Router]]
   * to be turned into clickable links.
   *
   * @param path Example: `"person/123"`
   * @tparam T The value of the dynamic portion of the route. Example: `PersonId(123)`.
   */
  final protected def dynLink[T](path: T => String): T => DynamicLocation[P] =
    t => DynamicLocation(Path(path(t)))

  // ===================================================================================================================
  // Convenience & Utility

  import Router.{Logger, nopLogger}

  final def routingEngine(base: BaseUrl, logger: Logger = nopLogger): Router =
    new Router(base, totalParser, logger)

  final def router(base: BaseUrl, logger: Logger = nopLogger): Router.Component[P] =
    Router.component(routingEngine(base, logger))

  /** `case matchNumber(num) => num.toLong` */
  final protected lazy val matchNumber = "^(\\d+)$".r

  /**
   * Registers a handle that uses a replace-state redirect to remove trailing slashes from unmatched route urls.
   */
  final protected def removeTrailingSlashes: DynamicRoute = {
    val regex = "^(.*?)/+$".r
    parser { case regex(p) => p }.redirection(p => (Path(p), Redirect.Replace))
  }
}