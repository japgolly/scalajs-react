package ghpages.pages

import scalaz.Equal
import scalaz.syntax.equal._
import japgolly.scalajs.react._, vdom.prefix_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router2.{RouterConfigDsl, RouterCtl}
import ghpages.examples._

sealed abstract class Example(val title: String,
                              val routerPath: String,
                              val render: () => ReactElement)

object Example {
  import ghpages.examples.util._
  implicit private def auto1(v: SideBySide.Content): () => ReactElement = () => v()
  implicit private def auto2(v: SingleSide.Content): () => ReactElement = () => v()

  case object Hello        extends Example("Hello World",        "hello",            HelloMessageExample .content)
  case object Timer        extends Example("Timer",              "timer",            TimerExample        .content)
  case object Todo         extends Example(TodoExample.title,    "todo",             TodoExample         .content)
  case object StateMonad   extends Example("State monads",       "state-monad",      StateMonadExample   .content)
  case object Refs         extends Example("Refs",               "refs",             RefsExample         .content)
  case object ProductTable extends Example("Product Table",      "product-table",    ProductTableExample .content)
  case object Animation    extends Example("Animation",          "animation",        AnimationExample    .content)
  case object PictureApp   extends Example("AjaxPictureApp",     "ajax-picture-app", PictureAppExample   .content)
  case object Touch        extends Example("Touch events",       "touch-events",     TouchExample        .content)
  case object ExternalVar  extends Example("ExternalVar",        "external-var",     ExternalVarExample  .content)
  case object Reuse        extends Example("Reusability",        "reusability",      ReuseExample        .content)
  case object EventListen  extends Example("EventListener",      "event-listener",   EventListenerExample.content)

  implicit val equality   : Equal[Example]       = Equal.equalA
  implicit val reusability: Reusability[Example] = Reusability.byEqual

  val values = Vector[Example](
    Hello, Timer, Todo, Refs, ProductTable, Animation, // Ported ReactJS examples
    EventListen, ExternalVar, Reuse, StateMonad,       // Scala only examples
    Touch, PictureApp)                                 // General usage

  def default: Example =
    values.head

  def routes = RouterConfigDsl[Example].buildRule { dsl =>
    import dsl._
    import ExamplesPage._
    values.map(e =>
      staticRoute(e.routerPath, e) ~> renderR(r => component(Props(e, r)))
    ).reduce(_ | _)
  }
}

// =====================================================================================================================

object ExamplesPage {

  case class Props(current: Example, router: RouterCtl[Example])

  implicit val propsReuse = Reusability.caseClass[Props]

  val menu = ReactComponentB[Props]("Example menu")
    .render { p =>
      def menuItem(e: Example) = {
        val active = e === p.current
        <.li(
          ^.classSet1("list-group-item", "active" -> active),
          p.router setOnClick e,
          e.title)
      }
      <.div(^.cls := "col-md-2",
        <.ul(^.cls := "list-group",
          Example.values map menuItem))
    }
    .configure(Reusability.shouldComponentUpdate)
    .build

  val body = ReactComponentB[Example]("Example body")
    .render(eg =>
      <.div(
        ^.cls := "col-md-10",
        eg.render()))
    .build

  val component = ReactComponentB[Props]("Examples")
    .render(p =>
      <.div(^.cls := "row",
        menu(p),
        body(p.current))
    ).build
}
