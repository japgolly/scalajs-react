package downstream

import cats.effect._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Carrot {

  Globals.onComponentInit()

  case class Props(name: String, io: IO[Unit] = IO.unit) {
    def render = Component(this)
  }

  implicit val reusability: Reusability[Props] =
    Reusability.caseClassExcept[Props]("io")

  val Component = ScalaComponent.builder[Props]("CarRot!")
    .render { $ =>
      Globals.carrotRenders += 1
      <.div("Hello ", $.props.name)
    }
    .configure(Reusability.shouldComponentUpdate)
    .componentDidMount(_ => Callback(Globals.carrotMountsA += 1))
    .componentDidMount(_ => SyncIO(Globals.carrotMountsB += 1))
    .componentDidMount(_.props.io)
    .build
}
