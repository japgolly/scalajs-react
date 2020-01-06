package japgolly.scalajs.react.extra.router

import org.scalajs.dom
import scala.annotation.elidable
import scala.util.{Failure, Success, Try}
import japgolly.scalajs.react.{Callback, CallbackTo}
import japgolly.scalajs.react.vdom.VdomElement
import RouterConfig.Logger

case class RouterConfig[Page](rules       : RoutingRules[Page],
                              renderFn    : (RouterCtl[Page], Resolution[Page]) => VdomElement,
                              postRenderFn: (Option[Page], Page) => Callback,
                              logger      : Logger) {

  def logWith(l: Logger): RouterConfig[Page] =
    copy(logger = l)

  def logToConsole: RouterConfig[Page] =
    logWith(RouterConfig.consoleLogger)

  /**
   * Specify how to render a page once it's resolved. This function will be applied to all renderable pages.
   */
  def renderWith(f: (RouterCtl[Page], Resolution[Page]) => VdomElement): RouterConfig[Page] =
    copy(renderFn = f)

  /**
   * Specify (entirely) what to do after the router renders.
   *
   * @param f Given the previous page and the current page that just rendered, return a callback.
   */
  def setPostRender(f: (Option[Page], Page) => Callback): RouterConfig[Page] =
    copy(postRenderFn = f)

  /**
   * Add an procedure to be performed after the router renders.
   *
   * @param f Given the previous page and the current page that just rendered, return a callback.
   */
  def onPostRender(f: (Option[Page], Page) => Callback): RouterConfig[Page] =
    setPostRender((a, b) => this.postRenderFn(a, b) >> f(a, b))

  /**
   * Change the document title after the router renders.
   *
   * @param f Given the current page that just rendered, return a new title.
   */
  def setTitle(f: Page => String): RouterConfig[Page] =
    setTitleOption(p => Some(f(p)))

  /**
   * Change the document title after the router renders.
   *
   * @param f Given the current page that just rendered, return potential new title.
   */
  def setTitleOption(f: Page => Option[String]): RouterConfig[Page] =
    onPostRender((_, page) =>
      f(page).fold(Callback.empty)(title => Callback(dom.document.title = title)))

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
  def verify(page1: Page, pages: Page*): RouterConfig[Page] =
    Option(_verify(page1, pages: _*)) getOrElse this

  @elidable(elidable.ASSERTION)
  private def _verify(page1: Page, pages: Page*): RouterConfig[Page] = {
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

      val newRules = RoutingRules[Page](
        parseMulti     = _ => static[Option[RouterConfig.Parsed[Page]]](Some(Right(page1))) :: Nil,
        path           = _ => Path.root,
        actionMulti    = (_, _) => Nil,
        fallbackAction = (_, _) => Renderer(_ => el),
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
    import RoutingRules.SharedLogic._

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


object RouterConfig {
  /** Either a redirect or a value representing the page to render. */
  type Parsed[Page] = Either[Redirect[Page], Page]

  type Logger = (=> String) => Callback

  def consoleLogger: Logger =
    s => Callback.log("[Router] " + s)

  val nopLogger: Logger =
    Function const Callback.empty

  def defaultLogger: Logger =
    nopLogger

  def defaultRenderFn[Page]: (RouterCtl[Page], Resolution[Page]) => VdomElement =
    (_, r) => r.render()

  def defaultPostRenderFn[Page]: (Option[Page], Page) => Callback = {
    val cb = Callback(dom.window.scrollTo(0, 0))
    (_, _) => cb
  }

  def withDefaults[Page](rules: RoutingRules[Page]): RouterConfig[Page] =
    RouterConfig(rules, defaultRenderFn, defaultPostRenderFn, defaultLogger)
}
