package ghpages

import ghpages.pages._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom

object GhPages {

  sealed trait Page
  case object Home          extends Page
  case class Eg(e: Example) extends Page
  case object Doco          extends Page
  case object AsyncTests    extends Page

  val routerConfig = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._

    def exampleRoutes: Rule =
      (ExamplesJs.routes | ExamplesScala.routes)
        .prefixPath_/("#examples").pmap[Page](Eg.apply) { case Eg(e) => e }

    (trimSlashes
    | staticRoute(root,          Home)       ~> render(HomePage.component())
    | staticRoute("#doc",        Doco)       ~> render(DocoPage.component())
    | staticRoute("#test/async", AsyncTests) ~> render(secret.tests.AsyncTest.Component())
    | exampleRoutes
    )
      .notFound(redirectToPage(Home)(SetRouteVia.HistoryReplace))
      .renderWith(layout)
      .verify(Home, Eg(ExamplesJs.Hello), Eg(ExamplesScala.Ajax1), Doco)
  }

  def layout(c: RouterCtl[Page], r: Resolution[Page]) =
    <.div(
      navMenu(c),
      <.div(^.cls := "container", r.render()))

  val navMenu = ScalaComponent.builder[RouterCtl[Page]]
    .render_P { ctl =>
      def nav(name: String, target: Page) =
        <.li(
          ^.cls := "navbar-brand active",
          ctl setOnClick target,
          name)
      <.div(
        ^.cls := "navbar navbar-default",
        <.ul(
          ^.cls := "navbar-header",
          nav("Home",               Home),
          nav("JS-Ported Examples", Eg(ExamplesJs.default)),
          nav("Scala Examples",     Eg(ExamplesScala.default)),
          nav("Documentation",      Doco)))
    }
    .configure(Reusability.shouldComponentUpdate)
    .build

  val baseUrl =
    dom.window.location.hostname match {
      case "localhost" | "127.0.0.1" | "0.0.0.0" =>
        BaseUrl.fromWindowUrl(_.takeWhile(_ != '#'))
      case _ =>
        BaseUrl.fromWindowOrigin / "scalajs-react/"
    }

  def main(args: Array[String]): Unit = {
    val container = dom.document.getElementById("root")
    val root = ReactDOMClient.createRoot(container)
    dom.console.info("Router logging is enabled. Enjoy!")
    val router = Router(baseUrl, routerConfig.logToConsole)
    root.render(router())
  }
}
