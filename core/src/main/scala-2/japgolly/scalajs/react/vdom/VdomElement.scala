package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.internal.ScalaJsReactConfigMacros
import japgolly.scalajs.react.{Callback, facade}

trait VdomElement extends VdomNode {

  override def rawNode = rawElement

  def rawElement: facade.React.Element

  @inline final def renderIntoDOM(container: facade.ReactDOM.Container, callback: Callback = Callback.empty): facade.React.ComponentUntyped =
    facade.ReactDOM.render(rawElement, container, callback.toJsFn)
}

object VdomElement {
  def apply(n: facade.React.Element): VdomElement =
    new VdomElement {
      override def rawElement = n
    }

  def static(vdom: VdomElement): VdomElement =
    macro ScalaJsReactConfigMacros.vdomElementStatic
}
