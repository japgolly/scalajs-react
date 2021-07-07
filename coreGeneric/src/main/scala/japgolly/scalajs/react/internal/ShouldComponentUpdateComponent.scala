package japgolly.scalajs.react.internal

import japgolly.scalajs.react.Reusability
import japgolly.scalajs.react.component.builder.EntryPoint
import japgolly.scalajs.react.vdom.VdomNode

object ShouldComponentUpdateComponent {

  type Props = (Int, () => VdomNode)

  private implicit val reusability: Reusability[Props] =
    Reusability.by(_._1)

  val Component =
    EntryPoint[Props]("")
      .render_P(_._2())
      .configure(Reusability.shouldComponentUpdate)
      .build

  def apply(rev: Int, vdom: () => VdomNode): VdomNode =
    Component((rev, vdom))
}
