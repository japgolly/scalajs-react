package ghpages.pages

import ghpages.examples.util.ErrorHandler
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router.RouterCtl

object ExampleComponents {

  case class Props(current: Example, router: RouterCtl[Example], examples: Vector[Example])

  implicit val propsReuse = Reusability.derive[Props]

  val menu = ScalaComponent.builder[Props]("Example menu")
    .render_P { p =>
      def menuItem(e: Example) = {
        val active = e == p.current
        p.router.link(e)(
          ^.classSet1("list-group-item", "active" -> active),
          e.title)
      }
      <.div(^.cls := "col-md-2",
        <.div(^.cls := "list-group")(
          p.examples.map(menuItem): _*))
    }
    .configure(Reusability.shouldComponentUpdate)
    .build

  private val errorHandler =
    ErrorHandler.pure(e =>
      <.pre(
        ^.color := "#c00",
        s"ERROR: ${e.message}\n${e.stack}"))

  val body = ScalaComponent.builder[Example]("Example body")
    .render_P(eg =>
      <.div(
        ^.cls := "col-md-10",
        errorHandler(eg.render())))
    .build

  val component = ScalaComponent.builder[Props]("Examples")
    .render_P(p =>
      <.div(^.cls := "row",
        menu(p),
        body(p.current)))
    .build
}
