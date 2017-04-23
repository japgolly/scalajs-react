package ghpages.pages

import japgolly.scalajs.react._, vdom.html_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router.{RouterConfigDsl, RouterCtl}
import ghpages.examples._
import ghpages.examples.util._

abstract class Example(val title: String,
                       val routerPath: String,
                       val render: () => VdomElement)

object Example {
  implicit val reusability: Reusability[Example] = Reusability.by_==
}

abstract class ExampleCollection {
  implicit protected def auto1(v: SideBySide.Content): () => VdomElement = () => v()
  implicit protected def auto2(v: SingleSide.Content): () => VdomElement = () => v()

  val values: Vector[Example]
  def default: Example = values.head

  def routes = RouterConfigDsl[Example].buildRule { dsl =>
    import dsl._
    import ExampleComponents._
    values.map(e =>
      staticRoute(e.routerPath, e) ~> renderR(r => component(Props(e, r, values)))
    ).reduce(_ | _)
  }
}


object ExamplesJs extends ExampleCollection {
  case object Hello        extends Example("Hello World",     "hello",         HelloMessageExample.content)
  case object Timer        extends Example("Timer",           "timer",         TimerExample       .content)
  case object Todo         extends Example(TodoExample.title, "todo",          TodoExample        .content)
  case object Refs         extends Example("Refs",            "refs",          RefsExample        .content)
  case object ProductTable extends Example("Product Table",   "product-table", ProductTableExample.content)
  case object Animation    extends Example("Animation",       "animation",     AnimationExample   .content)

  override val values = Vector[Example](
    Hello, Timer, Todo, Refs, ProductTable, Animation
  )
}


object ExamplesScala extends ExampleCollection {
  case object StateMonad    extends Example("State monads",       "state-monad",       StateMonadExample      .content)
  case object Touch         extends Example("Touch events",       "touch-events",      TouchExample           .content)
  case object StateSnapshot extends Example("StateSnapshot",      "state-snapshot",    StateSnapshotExample   .content)
  case object Reuse         extends Example("Reusability",        "reusability",       ReuseExample           .content)
  case object EventListen   extends Example("EventListener",      "event-listener",    EventListenerExample   .content)
  case object CallbackOpt   extends Example("CallbackOption",     "callback-option",   CallbackOptionExample  .content)
  case object WebSockets    extends Example("WebSockets",         "websockets",        WebSocketsExample      .content)
  case object Checkbox3     extends Example("Tri-state Checkbox", "tristate-checkbox", TriStateCheckboxExample.content)

  override val values = Vector[Example](
    EventListen, CallbackOpt, StateSnapshot, Reuse, StateMonad, Touch, WebSockets, Checkbox3
  ).sortBy(_.title)
}
