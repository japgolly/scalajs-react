package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{Callback, raw => Raw}

trait VdomElement extends VdomNode {

  override def rawNode = rawElement

  def rawElement: Raw.React.Element

  def renderIntoDOM(container: Raw.ReactDOM.Container, callback: Callback = Callback.empty): Raw.React.ComponentUntyped =
    Raw.ReactDOM.render(rawElement, container, callback.toJsFn)
}

object VdomElement {
  def apply(n: Raw.React.Element): VdomElement =
    new VdomElement {
      override def rawElement = n
    }
}
