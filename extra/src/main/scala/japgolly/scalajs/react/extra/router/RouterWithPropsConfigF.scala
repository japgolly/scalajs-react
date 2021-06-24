package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.extra.router.RouterConfig.Logger
import japgolly.scalajs.react.util.Effect.{Dispatch, Sync}
import japgolly.scalajs.react.vdom.VdomElement
import org.scalajs.dom
import scala.scalajs.LinkingInfo.developmentMode
import scala.util.{Failure, Success, Try}

case class RouterWithPropsConfigF[F[_], Page, Props](
      rules       : RoutingRulesF[F, Page, Props],
      renderFn    : (RouterCtlF[F, Page], ResolutionWithProps[Page, Props]) => Props => VdomElement,
      postRenderFn: (Option[Page], Page, Props) => F[Unit],
      logger      : Logger,
     )(implicit val effect: Sync[F]) { self =>

  @inline private def F = effect

  def logWith(l: Logger): RouterWithPropsConfigF[F, Page, Props] =
    copy(logger = l)

  def logToConsole: RouterWithPropsConfigF[F, Page, Props] =
    logWith(RouterConfig.consoleLogger)

  /**
   * Specify how to render a page once it's resolved. This function will be applied to all renderable pages.
   */
  def renderWithP(f: (RouterCtlF[F, Page], ResolutionWithProps[Page, Props]) => Props => VdomElement): RouterWithPropsConfigF[F, Page, Props] =
    copy(renderFn = f)

  /**
   * Specify how to render a page once it's resolved. This function will be applied to all renderable pages.
   */
  def renderWith(f: (RouterCtlF[F, Page], ResolutionWithProps[Page, Props]) => VdomElement): RouterWithPropsConfigF[F, Page, Props] =
    copy(renderFn = (ctl, ctx) => _ => f(ctl, ctx))

  /**
   * Specify (entirely) what to do after the router renders.
   *
   * @param f Given the previous page, the current page that just rendered and props, return a callback.
   */
  def setPostRenderP[G[_]](f: (Option[Page], Page, Props) => G[Unit])(implicit G: Dispatch[G]): RouterWithPropsConfigF[F, Page, Props] =
    copy(postRenderFn = F.transDispatchFn3(f))

  /**
   * Specify (entirely) what to do after the router renders.
   *
   * @param f Given the previous page and the current page that just rendered, return a callback.
   */
  def setPostRender[G[_]](f: (Option[Page], Page) => G[Unit])(implicit G: Dispatch[G]): RouterWithPropsConfigF[F, Page, Props] =
    setPostRenderP((previous, current, _) => f(previous, current))

  /**
   * Add an procedure to be performed after the router renders.
   *
   * @param f Given the previous page, the current page that just rendered and props, return a callback.
   */
  def onPostRenderP[G[_]](f: (Option[Page], Page, Props) => G[Unit])(implicit G: Dispatch[G]): RouterWithPropsConfigF[F, Page, Props] = {
    val f2 = F.transDispatchFn3(f)
    setPostRenderP((a, b, c) => F.chain(this.postRenderFn(a, b, c), f2(a, b, c)))
  }

  /**
   * Add an procedure to be performed after the router renders.
   *
   * @param f Given the previous page and the current page that just rendered, return a callback.
   */
  def onPostRender[G[_]](f: (Option[Page], Page) => G[Unit])(implicit G: Dispatch[G]): RouterWithPropsConfigF[F, Page, Props] =
    onPostRenderP((previous, current, _) => f(previous, current))

  /**
   * Change the document title after the router renders.
   *
   * @param f Given the current page that just rendered and props, return a new title.
   */
  def setTitleP(f: (Page, Props) => String): RouterWithPropsConfigF[F, Page, Props] =
    setTitleOptionP((p, c) => Some(f(p, c)))

  /**
   * Change the document title after the router renders.
   *
   * @param f Given the current page that just rendered, return a new title.
   */
  def setTitle(f: Page => String): RouterWithPropsConfigF[F, Page, Props] =
    setTitleOption(p => Some(f(p)))

  /**
   * Change the document title after the router renders.
   *
   * @param f Given the current page that just rendered and props, return potential new title.
   */
  def setTitleOptionP(f: (Page, Props) => Option[String]): RouterWithPropsConfigF[F, Page, Props] =
    onPostRenderP((_, page, c) =>
      f(page, c).fold(F.empty)(title => F.delay(dom.document.title = title)))

  /**
   * Change the document title after the router renders.
   *
   * @param f Given the current page that just rendered, return potential new title.
   */
  def setTitleOption(f: Page => Option[String]): RouterWithPropsConfigF[F, Page, Props] =
    setTitleOptionP((page, _) => f(page))

  /** Asserts that the page arguments provided, don't encounter any route config errors.
    *
    * If any errors are detected, the Router will be replaced with a new dummy router that displays the error messages.
    *
    * If you want direct, programmatic access to the errors themselves, use [[detectErrors()]] instead.
    *
    * Note: Requires that `Page#equals()` be sensible.
    * Note: If in production-mode (`fullOptJS`), this always returns `this`.
    *
    * @return In the event that errors are detected, a new [[RouterConfig]] that displays them; else this unmodified.
    */
  def verify(page1: Page, pages: Page*): RouterWithPropsConfigF[F, Page, Props] =
    if (developmentMode)
      _verify(page1, pages: _*)
    else
      this

  private def _verify(page1: Page, pages: Page*): RouterWithPropsConfigF[F, Page, Props] = {
    val errors = F.runSync(detectErrors(page1 +: pages: _*))
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

      val newRules = RoutingRulesF[F, Page, Props](
        parseMulti     = _ => static[Option[RouterConfig.Parsed[Page]]](Some(Right(page1))) :: Nil,
        path           = _ => Path.root,
        actionMulti    = (_, _) => Nil,
        fallbackAction = (_, _) => RendererF[F, Page, Props](_ => (_: Props) => el),
        whenNotFound   = _ => F.pure(Right(page1)),
      )

      RouterConfig.withDefaults(newRules)
    }
  }

  /** Check specified pages for possible route config errors, and returns any detected.
    *
    * Note: Requires that `Page#equals()` be sensible.
    * Note: If in production-mode (`fullOptJS`), this always returns an empty collection.
    *
    * @return Error messages (or an empty collection if no errors are detected).
    */
  def detectErrors(pages: Page*): F[Seq[String]] =
    if (developmentMode)
      F.map(_detectErrors(pages: _*))(v => v)
    else
      F.pure(Nil)

  private def _detectErrors(pages: Page*): F[Vector[String]] = F.delay {

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
          Try(F.runSync(rules.parse(path))) match {
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
          Try(F.runSync(rules.action(path, page))) match {
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
