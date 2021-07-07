package japgolly.scalajs.react.internal

import japgolly.scalajs.react.{CtorType, Reusability}
import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.component.builder.EntryPoint
import japgolly.scalajs.react.vdom.VdomNode
import scala.language.`3.0`

object ShouldComponentUpdateComponent {

  type Props = (Int, () => VdomNode)

  private implicit val reusability: Reusability[Props] =
    Reusability.by(_._1)

  private inline def createComponent() =
    EntryPoint[Props]("ShouldComponentUpdate")
      .render_P(_._2())
      .configure(Reusability.shouldComponentUpdate)
      .build

  type Component = Scala.Component[Props, Unit, Unit, CtorType.Props]

  private var _component: Component =
    null

  inline def Component: Component = {
    if (_component == null)
      _component = createComponent()
    _component
  }

  def apply(rev: Int, vdom: () => VdomNode): VdomNode =
    Component((rev, vdom))
}
