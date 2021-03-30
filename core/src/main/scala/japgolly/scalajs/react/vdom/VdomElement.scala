package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{Callback, raw => Raw}

trait VdomElement extends VdomNode {

  override def rawNode = rawElement

  def rawElement: Raw.React.Element

  @inline final def renderIntoDOM(container: Raw.ReactDOM.Container, callback: Callback = Callback.empty): Raw.React.ComponentUntyped =
    Raw.ReactDOM.render(rawElement, container, callback.toJsFn)
}

object VdomElement {
  def apply(n: Raw.React.Element): VdomElement =
    new VdomElement {
      override def rawElement = n
    }

  // TODO: [3] re-enable after ScalaComponent
  // def static(vdom: VdomElement): VdomElement =
  //   japgolly.scalajs.react.ScalaComponent.static("")(vdom)()
}
