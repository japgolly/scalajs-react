package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.extra.router.RouterConfig.Logger
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.{Callback, CallbackTo}
import org.scalajs.dom
import scala.annotation.elidable
import scala.util.{Failure, Success, Try}

case class RouterWithPropsConfig[Page, Props](
    rules       : RoutingRules[Page, Props],
    renderFn    : (RouterCtl[Page], ResolutionWithProps[Page, Props]) => Props => VdomElement,
    postRenderFn: (Option[Page], Page, Props) => Callback,
    logger      : Logger) {

  def logWith(l: Logger): RouterWithPropsConfig[Page, Props] =
    copy(logger = l)

  def logToConsole: RouterWithPropsConfig[Page, Props] =
    logWith(RouterConfig.consoleLogger)

  /**
   * Specify how to render a page once it's resolved. This function will be applied to all renderable pages.
   */
  def renderWithP(f: (RouterCtl[Page], ResolutionWithProps[Page, Props]) => Props => VdomElement): RouterWithPropsConfig[Page, Props] =
    copy(renderFn = f)

  /**
   * Specify how to render a page once it's resolved. This function will be applied to all renderable pages.
   */
  def renderWith(f: (RouterCtl[Page], ResolutionWithProps[Page, Props]) => VdomElement): RouterWithPropsConfig[Page, Props] =
    copy(renderFn = (ctl, ctx) => _ => f(ctl, ctx))

  /**
   * Specify (entirely) what to do after the router renders.
   *
   * @param f Given the previous page, the current page that just rendered and props, return a callback.
   */
  def setPostRenderP(f: (Option[Page], Page, Props) => Callback): RouterWithPropsConfig[Page, Props] =
    copy(postRenderFn = f)

  /**
   * Specify (entirely) what to do after the router renders.
   *
   * @param f Given the previous page and the current page that just rendered, return a callback.
   */
  def setPostRender(f: (Option[Page], Page) => Callback): RouterWithPropsConfig[Page, Props] =
    setPostRenderP((previous, current, _) => f(previous, current))

  /**
   * Add an procedure to be performed after the router renders.
   *
   * @param f Given the previous page, the current page that just rendered and props, return a callback.
   */
  def onPostRenderP(f: (Option[Page], Page, Props) => Callback): RouterWithPropsConfig[Page, Props] =
    setPostRenderP((a, b, c) => this.postRenderFn(a, b, c) >> f(a, b, c))

  /**
   * Add an procedure to be performed after the router renders.
   *
   * @param f Given the previous page and the current page that just rendered, return a callback.
   */
  def onPostRender(f: (Option[Page], Page) => Callback): RouterWithPropsConfig[Page, Props] =
    onPostRenderP((previous, current, _) => f(previous, current))

  /**
   * Change the document title after the router renders.
   *
   * @param f Given the current page that just rendered and props, return a new title.
   */
  def setTitleP(f: (Page, Props) => String): RouterWithPropsConfig[Page, Props] =
    setTitleOptionP((p, c) => Some(f(p, c)))

  /**
   * Change the document title after the router renders.
   *
   * @param f Given the current page that just rendered, return a new title.
   */
  def setTitle(f: Page => String): RouterWithPropsConfig[Page, Props] =
    setTitleOption(p => Some(f(p)))

  /**
   * Change the document title after the router renders.
   *
   * @param f Given the current page that just rendered and props, return potential new title.
   */
  def setTitleOptionP(f: (Page, Props) => Option[String]): RouterWithPropsConfig[Page, Props] =
    onPostRenderP((_, page, c) =>
      f(page, c).fold(Callback.empty)(title => Callback(dom.document.title = title)))

  /**
   * Change the document title after the router renders.
   *
   * @param f Given the current page that just rendered, return potential new title.
   */
  def setTitleOption(f: Page => Option[String]): RouterWithPropsConfig[Page, Props] =
    setTitleOptionP((page, _) => f(page))

  /** Asserts that the page arguments provided, don't encounter any route config errors.
    *
    * If any errors are detected, the Router will be replaced with a new dummy router that displays the error messages.
    *
    * If you want direct, programmatic access to the errors themselves, use [[detectErrors()]] instead.
    *
    * Note: Requires that `Page#equals()` be sensible.
    * Note: If `elidable.ASSERTION` is elided, this always returns `this`.
    *
    * @return In the event that errors are detected, a new [[RouterConfig]] that displays them; else this unmodified.
    */
  def verify(page1: Page, pages: Page*): RouterWithPropsConfig[Page, Props] =
    Option(_verify(page1, pages: _*)) getOrElse this

  @elidable(elidable.ASSERTION)
  private def _verify(page1: Page, pages: Page*): RouterWithPropsConfig[Page, Props] = {
    val errors = detectErrors(page1 +: pages: _*).runNow()
    if (errors.isEmpty)
      this
    else {
      import japgolly.scalajs.react.vdom.html_<^._
      import StaticOrDynamic.Helpers.static
      val es = errors.sorted.map(e => s"\n  - $e") mkString ""
      val msg = s"${errors.size} RouterConfig errors detected:$es"
      dom.console.error(msg)

      val el: VdomElement =
        <.pre(^.color := "#900", ^.margin := "auto", ^.display := "block", msg)

      val newRules = RoutingRules[Page, Props](
        parseMulti     = _ => static[Option[RouterConfig.Parsed[Page]]](Some(Right(page1))) :: Nil,
        path           = _ => Path.root,
        actionMulti    = (_, _) => Nil,
        fallbackAction = (_, _) => Renderer(_ => (_: Props) => el),
        whenNotFound   = _ => CallbackTo.pure(Right(page1)),
      )

      RouterConfig.withDefaults(newRules)
    }
  }

  /** Check specified pages for possible route config errors, and returns any detected.
    *
    * Note: Requires that `Page#equals()` be sensible.
    * Note: If `elidable.ASSERTION` is elided, this always returns an empty collection.
    *
    * @return Error messages (or an empty collection if no errors are detected).
    */
  def detectErrors(pages: Page*): CallbackTo[Vector[String]] =
    Option(_detectErrors(pages: _*)) getOrElse CallbackTo.pure(Vector.empty)

  @elidable(elidable.ASSERTION)
  private def _detectErrors(pages: Page*): CallbackTo[Vector[String]] = CallbackTo {

    var errors = Vector.empty[String]

    for (page <- pages) {
      def fail(msg: String): Unit =
        errors :+= s"Routing config failure at page $page: $msg"

      // page -> path
      Try(rules.path(page)) match {
        case Failure(f) =>
          fail(s"Runtime exception occurred generating path: ${f.getMessage}")

        case Success(path) =>

          // path -> page
          rules.parse(path).attemptTry.runNow() match {
            case Success(Right(q)) =>
              if (q != page) errors :+= s"Parsing its path /${path.value} leads to a different page: $q"

            case Success(Left(r)) =>
              val to: String = r match {
                case RedirectToPage(page, _) => s"page $page"
                case RedirectToPath(path, _) => s"path /${path.value}"
              }
              fail(s"Parsing its path /${path.value} leads to a redirect to $to. Cannot verify that this is intended and not a 404.")

            case Failure(f: RoutingRules.Exception) =>
              fail(f.getMessage)

            case Failure(f) =>
              fail(s"Runtime exception occurred parsing path /${path.value}: ${f.getMessage}")
          }

          // page -> action
          rules.action(path, page).attemptTry.runNow() match {
            case Success(_) => ()

            case Failure(f: RoutingRules.Exception) =>
              fail(f.getMessage)

            case Failure(f) =>
              fail(s"Runtime exception occurred generating action for path /${path.value}: ${f.getMessage}")
          }
      }
    }

    errors
  }
}
