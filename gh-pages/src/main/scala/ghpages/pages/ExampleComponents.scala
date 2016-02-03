package ghpages.pages

import japgolly.scalajs.react._, vdom.prefix_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router.RouterCtl

object ExampleComponents {

  case class Props(current: Example, router: RouterCtl[Example], examples: Vector[Example])

  implicit val propsReuse = Reusability.caseClass[Props]

  val menu = ReactComponentB[Props]("Example menu")
    .render_P { p =>
      def menuItem(e: Example) = {
        val active = e == p.current
        p.router.link(e)(
          ^.classSet1("list-group-item", "active" -> active),
          e.title)
      }
      <.div(^.cls := "col-md-2",
        <.div(^.cls := "list-group",
          p.examples map menuItem))
    }
    .configure(Reusability.shouldComponentUpdate)
    .build

  val body = ReactComponentB[Example]("Example body")
    .render_P(eg =>
      <.div(
        ^.cls := "col-md-10",
        eg.render()))
    .build

  val component = ReactComponentB[Props]("Examples")
    .render_P(p =>
      <.div(^.cls := "row",
        menu(p),
        body(p.current))
    ).build
}