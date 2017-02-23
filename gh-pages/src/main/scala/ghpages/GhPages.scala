package ghpages

import org.scalajs.dom
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react._, vdom.html_<^._, ScalazReact._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router._
import pages._

object GhPages extends JSApp {

  sealed trait Page
  case object Home          extends Page
  case class Eg(e: Example) extends Page
  case object Doco          extends Page

  val routerConfig = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._

    def exampleRoutes: Rule =
      (ExamplesJs.routes | ExamplesScala.routes)
        .prefixPath_/("#examples").pmap[Page](Eg) { case Eg(e) => e }

    (trimSlashes
    | staticRoute(root,   Home) ~> render(HomePage.component())
    | staticRoute("#doc", Doco) ~> render(DocoPage.component())
    | exampleRoutes
    )
      .notFound(redirectToPage(Home)(Redirect.Replace))
      .renderWith(layout)
      .verify(Home, Eg(ExamplesJs.Hello), Eg(ExamplesScala.EventListen), Doco)
  }

  def layout(c: RouterCtl[Page], r: Resolution[Page]) =
    <.div(
      navMenu(c),
      <.div(^.cls := "container", r.render()))

  val navMenu = ScalaComponent.build[RouterCtl[Page]]("Menu")
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
    if (dom.window.location.hostname == "localhost")
      BaseUrl.fromWindowOrigin_/
    else
      BaseUrl.fromWindowOrigin / "scalajs-react/"

  @JSExport
  override def main(): Unit = {
    dom.console.info("Router logging is enabled. Enjoy!")
    val router = Router(baseUrl, routerConfig.logToConsole)
    router() renderIntoDOM dom.document.body
  }
}
