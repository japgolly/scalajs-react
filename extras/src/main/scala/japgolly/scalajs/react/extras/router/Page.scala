package japgolly.scalajs.react.extras.router

import scalaz.{-\/, \/-, \/}
import japgolly.scalajs.react.{ReactElement, ReactComponentC, TopNode}

/**
 * DSL for specifying a set of routing rules.
 */
trait Page {
  final type P              = this.type
  final type Renderer       = japgolly.scalajs.react.extras.router.Renderer[P]
  final type Router         = japgolly.scalajs.react.extras.router.Router[P]
  final type Loc            = Location[P]
  final type RedirectTarget = Redirect.Target[P]

  @inline final protected implicit def componentP_renderer[S,B,T<:TopNode](c: ReactComponentC.ReqProps[Router, S, B, T]): Renderer = c(_)
  @inline final protected implicit def componentU_renderer[P,S,B,T<:TopNode](c: ReactComponentC.ConstProps[P, S, B, T]): Renderer = _ => c()
  @inline final protected implicit def element_renderer[A <% ReactElement](a: A): Renderer = _ => a
  @inline final protected implicit def loc_redirectable(a: Loc): RedirectTarget = \/-(a)
  @inline final protected implicit def path_redirectable(p: Path): RedirectTarget = -\/(p)

  private[this] def parser: Path => RouteAction[P] =
    p => parseS(p) orElse parseD(p) getOrElse notFound(p)

  private[this] var staticRoutes = Map.empty[Path, RouteAction[P]]
  private[this] val parseS: Path => Option[RouteAction[P]] = p => staticRoutes.get(p)

  private type DynRoute = Path => Option[RouteAction[P]]
  private[this] var dynRoutes = Vector.empty[DynRoute]
  private[this] val parseD: DynRoute = p => dynRoutes.foldLeft(None: Option[RouteAction[P]])(_ orElse _(p))

  // ===================================================================================================================
  // Actions

  final type DynAction = Path => RouteAction[P]

  /**
   * The catch-all response to unmatched routes.
   */
  protected val notFound: DynAction

  final protected def render(render: Renderer): DynAction =
    path => Location[P](path, render)

  final protected def redirect(to: Loc, method: Redirect.Method): DynAction =
    _ => Redirect(to, method)

  // ===================================================================================================================
  //  Static Routes

  final private def staticRoute[A <: RouteAction[P]](path: Path, action: A): A =
    staticRoutes.get(path) match {
      case Some(prev) =>
        throw new ExceptionInInitializerError(s"Attempted to register two routes with the name '${path.value}' in $this.\n1) $prev\n2) $action")
      case None =>
        staticRoutes += path -> action
        action
    }

  final protected def rootLocation(r: Renderer): Loc =
    location("", r)

  final protected def location(path: String, render: Renderer): Loc = {
    val p = Path(path)
    staticRoute(p, Location[P](p, render))
  }

  final protected def redirection(from: String, to: RedirectTarget, method: Redirect.Method): Redirect[P] =
    staticRoute(Path(from), Redirect(to, method))

  // ===================================================================================================================
  //  Dynamic Routes

  /** Evidence that a dynamic route was registered. */
  sealed trait Registered

  /**
   * Parse a dynamic path. Example: `"person/123"`
   *
   * @tparam T The value of the dynamic portion of the path. Example: `"123"` or `PersonId(123)`.
   */
  final protected def parse[T](pf: PartialFunction[String, T]): DynB[T] =
    new DynB[T](pf.lift.compose[Path](_.value))

  final protected class DynB[T](parse: Path => Option[T]) {

    private def register(f: (Path, T) => RouteAction[P]): Registered = {
      val r: DynRoute = path => parse(path).map(t => f(path, t))
      dynRoutes :+= r
      null.asInstanceOf[Registered]
    }

    def thenMatch(f: T => DynAction): Registered =
      register((p, t) => f(t)(p))

    /**
     * Registers a dynamic location.
     *
     * Note that a `Location[P]` is not returned. In order to create links to this location,
     * use `this.dynLink()`.
     */
    def location(f: T => Renderer): Registered =
      register((p, t) => Location(p, f(t)))

    def redirection(f: T => (RedirectTarget, Redirect.Method)): Registered =
      register((_, t) => {
        val (l, m) = f(t)
        Redirect(l, m)
      })
  }

  /**
   * Generates paths for a dynamic route, which can then be passed to [[japgolly.scalajs.react.extras.router.Router]]
   * to be turned into clickable links.
   *
   * @param path Example: `"person/123"`
   * @tparam T The value of the dynamic portion of the route. Example: `PersonId(123)`.
   */
  final protected def dynLink[T](path: T => String): T => DynamicLocation[P] =
    t => DynamicLocation(Path(path(t)))

  // ===================================================================================================================
  // Convenience & Utility

  final def routingEngine(base: BaseUrl): Router =
    new Router(base, parser)

  final def router(base: BaseUrl): Router.Component[P] =
    Router.component(routingEngine(base))

  /** `case matchNumber(num) => num.toLong` */
  protected final lazy val matchNumber = "^(\\d+)$".r

  /**
   * Registers a handle that uses a replace-state redirect to remove trailing slashes from unmatched route urls.
   */
  def removeTrailingSlashes(): Registered = {
    val regex = "^(.*?)/+$".r
    parse { case regex(p) => p }.redirection(p => (Path(p), Redirect.Replace))
  }
}