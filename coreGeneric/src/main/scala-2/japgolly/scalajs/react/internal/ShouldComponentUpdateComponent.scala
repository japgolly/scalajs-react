package japgolly.scalajs.react.internal

import japgolly.scalajs.react.{CtorType, Reusability}
import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.component.builder.ComponentBuilder.Step1
import japgolly.scalajs.react.vdom.VdomNode

object ShouldComponentUpdateComponent {

  type Props = (Int, () => VdomNode)

  private implicit val reusability: Reusability[Props] =
    Reusability.by(_._1)

  type Component = Scala.Component[Props, Unit, Unit, CtorType.Props]

  private var _component: Component =
    null

  def unsafeComponent(): Component =
    _component

  def Component: Component =
    macro ScalaJsReactConfigMacros.shouldComponentUpdateComponent

  def init(transformedName: String): Unit =
    if (_component == null)
      _component = new Step1[Props](transformedName)
        .render_P(_._2())
        .configure(Reusability.shouldComponentUpdate)
        .build

  def apply(rev: Int, vdom: () => VdomNode): VdomNode =
    macro ScalaJsReactConfigMacros.shouldComponentUpdateComponentApply
}
