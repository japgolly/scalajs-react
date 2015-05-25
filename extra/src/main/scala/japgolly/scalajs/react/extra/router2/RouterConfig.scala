package japgolly.scalajs.react.extra.router2

import org.scalajs.dom
import scala.annotation.elidable
import scala.util.{Failure, Success, Try}
import scalaz.{Equal, -\/, \/-, \/}
import scalaz.effect.IO
import scalaz.syntax.bind.ToBindOps
import scalaz.syntax.equal.ToEqualOps
import japgolly.scalajs.react.ReactElement
import RouterConfig.{Logger, Parsed}

case class RouterConfig[Page](parse       : Path => Parsed[Page],
                              path        : Page => Path,
                              action      : Page => Action[Page],
                              renderFn    : (RouterCtl[Page], Resolution[Page]) => ReactElement,
                              postRenderFn: (Option[Page], Page) => IO[Unit],
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
  def setPostRender(f: (Option[Page], Page) => IO[Unit]): RouterConfig[Page] =
    copy(postRenderFn = f)

  /**
   * Add an procedure to be performed after the router renders.
   *
   * @param f Given the previous page and the current page that just rendered, return a callback.
   */
  def onPostRender(f: (Option[Page], Page) => IO[Unit]): RouterConfig[Page] =
    setPostRender((a, b) => this.postRenderFn(a, b) >> f(a, b))

  /**
   * Verify that the page arguments provided, don't encounter any route config errors.
   */
  def verify(page1: Page, pages: Page*): RouterConfig[Page] =
    verifyE(page1, pages: _*)(Equal.equalA)

  /**
   * Verify that the page arguments provided, don't encounter any route config errors.
   */
  def verifyE(page1: Page, pages: Page*)(implicit eq: Equal[Page]): RouterConfig[Page] =
    Option(_verifyE(page1, pages: _*)) getOrElse this

  @elidable(elidable.ASSERTION)
  private def _verifyE(page1: Page, pages: Page*)(implicit eq: Equal[Page]): RouterConfig[Page] = {
    val errors = detectErrorsE(page1 +: pages: _*)
    if (errors.isEmpty)
      this
    else {
      import japgolly.scalajs.react.vdom.prefix_<^._
      val es = errors.sorted.map(e => s"\n  - $e") mkString ""
      val msg = s"${errors.size} RouterConfig errors detected:$es"
      dom.console.error(msg)
      val el: ReactElement =
        <.pre(^.color := "#900", ^.margin := "auto", ^.display := "block", msg)
      RouterConfig.withDefaults(_ => \/-(page1), _ => Path.root, _ => Renderer(_ => el))
    }
  }

  /**
   * Check specified pages for possible route config errors.
   */
  def detectErrors(pages: Page*): Vector[String] =
    detectErrorsE(pages: _*)(Equal.equalA)

  /**
   * Check specified pages for possible route config errors.
   */
  def detectErrorsE(pages: Page*)(implicit eq: Equal[Page]): Vector[String] =
    Option(_detectErrors(pages: _*)) getOrElse Vector.empty

  @elidable(elidable.ASSERTION)
  private def _detectErrors(pages: Page*)(implicit eq: Equal[Page]): Vector[String] = {
    var errors = Vector.empty[String]
    for (p <- pages) {
      def error(msg: String): Unit = errors :+= s"Page [$p]: $msg"

      // page -> path -> page
      Try(path(p)) match {
        case Failure(f) => error(s"Path missing. ${f.getMessage}")
        case Success(path) =>
          parse(path) match {
            case -\/(r) => error(s"Parsing its path [${path.value}] leads to a redirect. Cannot verify that this is intended and not a 404.")
            case \/-(q) => if (q ≠ p) error(s"Parsing its path [${path.value}] leads to a different page: $q")
          }
      }

      // page -> action
      Try(action(p)) match {
        case Failure(f) => error(s"Action missing. ${f.getMessage}")
        case Success(a) => ()
      }

    }
    errors
  }
}


object RouterConfig {
  /** Either a redirect or a value representing the page to render. */
  type Parsed[Page] = Redirect[Page] \/ Page

  type Logger = (=> String) => IO[Unit]

  def consoleLogger: Logger =
    s => IO(dom.console.log(s"[Router] $s"))

  val nopLogger: Logger =
    Function const IO(())

  def defaultLogger: Logger =
    nopLogger

  def defaultRenderFn[Page]: (RouterCtl[Page], Resolution[Page]) => ReactElement =
    (_, r) => r.render()

  def defaultPostRenderFn[Page]: (Option[Page], Page) => IO[Unit] = {
    val io = IO(dom.window.scrollTo(0, 0))
    (_, _) => io
  }

  def withDefaults[Page](parse : Path => Parsed[Page],
                         path  : Page => Path,
                         action: Page => Action[Page]): RouterConfig[Page] =
    RouterConfig(parse, path, action, defaultRenderFn, defaultPostRenderFn, defaultLogger)
}
