package ghpages

import org.scalajs.dom
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react._, vdom.prefix_<^._, ScalazReact._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router._
import pages._

object GhPages extends JSApp {

  sealed trait Page
  case object Home                 extends Page
  case class Examples(eg: Example) extends Page
  case object Doco                 extends Page

  val routerConfig = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._

    def exampleRoutes: Rule =
      Example.routes.prefixPath_/("#examples").pmap[Page](Examples) { case Examples(e) => e }

    (trimSlashes
    | staticRoute(root,   Home) ~> render(HomePage.component())
    | staticRoute("#doc", Doco) ~> render(DocoPage.component())
    | exampleRoutes
    )
      .notFound(redirectToPage(Home)(Redirect.Replace))
      .renderWith(layout)
      .verify(Home, Examples(Example.Hello), Examples(Example.Todo), Doco)
  }

  def layout(c: RouterCtl[Page], r: Resolution[Page]) =
    <.div(
      navMenu(c),
      <.div(^.cls := "container", r.render()))

  val navMenu = ReactComponentB[RouterCtl[Page]]("Menu")
    .render { ctl =>
      def nav(name: String, target: Page) =
        <.li(
          ^.cls := "navbar-brand active",
          ctl setOnClick target,
          name)
      <.div(
        ^.cls := "navbar navbar-default",
        <.ul(
          ^.cls := "navbar-header",
          nav("Home",          Home),
          nav("Examples",      Examples(Example.default)),
          nav("Documentation", Doco)))
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
    router() render dom.document.body
  }
}
