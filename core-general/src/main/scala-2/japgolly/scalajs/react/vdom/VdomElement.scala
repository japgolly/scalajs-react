package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.internal.ScalaJsReactConfigMacros
import japgolly.scalajs.react.facade

trait VdomElement extends VdomNode {

  override def rawNode = rawElement

  def rawElement: facade.React.Element
}

object VdomElement {
  def apply(n: facade.React.Element): VdomElement =
    new VdomElement {
      override def rawElement = n
    }

  def static(vdom: VdomElement): VdomElement =
    macro ScalaJsReactConfigMacros.vdomElementStatic
}
