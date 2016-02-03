package japgolly.scalajs.react.extra.router

import org.scalajs.dom
import scala.annotation.elidable
import scala.util.{Failure, Success, Try}
import japgolly.scalajs.react.{Callback, ReactElement}
import RouterConfig.{Logger, Parsed}

case class RouterConfig[Page](parse       : Path => Parsed[Page],
                              path        : Page => Path,
                              action      : (Path, Page) => Action[Page],
                              renderFn    : (RouterCtl[Page], Resolution[Page]) => ReactElement,
                              postRenderFn: (Option[Page], Page) => Callback,
                              logger      : Logger) {

  def logWith(l: Logger): RouterConfig[Page] =
    copy(logger = l)

  def logToConsole: RouterConfig[Page] =
    logWith(RouterConfig.consoleLogger)

  /**
   * Specify how to render a page once it's resolved. This function will be applied to all renderable pages.
   */
  def renderWith(f: (RouterCtl[Page], Resolution[Page]) => ReactElement): RouterConfig[Page] =
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
   * Verify that the page arguments provided, don't encounter any route config errors.
   *
   * Note: Requires that `Page#equals()` be sensible.
   */
  def verify(page1: Page, pages: Page*): RouterConfig[Page] =
    Option(_verify(page1, pages: _*)) getOrElse this

  @elidable(elidable.ASSERTION)
  private def _verify(page1: Page, pages: Page*): RouterConfig[Page] = {
    val errors = detectErrors(page1 +: pages: _*)
    if (errors.isEmpty)
      this
    else {
      import japgolly.scalajs.react.vdom.prefix_<^._
      val es = errors.sorted.map(e => s"\n  - $e") mkString ""
      val msg = s"${errors.size} RouterConfig errors detected:$es"
      dom.console.error(msg)
      val el: ReactElement =
        <.pre(^.color := "#900", ^.margin := "auto", ^.display := "block", msg)
      RouterConfig.withDefaults(_ => Right(page1), _ => Path.root, (_, _) => Renderer(_ => el))
    }
  }

  /**
   * Check specified pages for possible route config errors.
   *
   * Note: Requires that `Page#equals()` be sensible.
   */
  def detectErrors(pages: Page*): Vector[String] =
    Option(_detectErrors(pages: _*)) getOrElse Vector.empty

  @elidable(elidable.ASSERTION)
  private def _detectErrors(pages: Page*): Vector[String] = {
    var errors = Vector.empty[String]
    for (page <- pages) {
      def error(msg: String): Unit = errors :+= s"Page $page: $msg"

      // page -> path
      Try(path(page)) match {
        case Failure(f) => error(s"Path missing. ${f.getMessage}")
        case Success(path) =>

          // path -> page
          parse(path) match {
            case Left(r) => error(s"Parsing its path $path leads to a redirect. Cannot verify that this is intended and not a 404.")
            case Right(q) => if (q != page) error(s"Parsing its path $path leads to a different page: $q")
          }

          // page -> action
          Try(action(path, page)) match {
            case Failure(f) => error(s"Action missing. ${f.getMessage}")
            case Success(a) => ()
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

  def defaultRenderFn[Page]: (RouterCtl[Page], Resolution[Page]) => ReactElement =
    (_, r) => r.render()

  def defaultPostRenderFn[Page]: (Option[Page], Page) => Callback = {
    val cb = Callback(dom.window.scrollTo(0, 0))
    (_, _) => cb
  }

  def withDefaults[Page](parse : Path         => Parsed[Page],
                         path  : Page         => Path,
                         action: (Path, Page) => Action[Page]): RouterConfig[Page] =
    RouterConfig(parse, path, action, defaultRenderFn, defaultPostRenderFn, defaultLogger)
}
