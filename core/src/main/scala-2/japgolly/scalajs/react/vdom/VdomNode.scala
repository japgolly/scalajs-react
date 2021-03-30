package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{raw => Raw}

trait VdomNode extends TagMod {
  def rawNode: Raw.React.Node

  override def applyTo(b: Builder): Unit =
    b.appendChild(rawNode)
}

object VdomNode {
  def apply(n: Raw.React.Node): VdomNode =
    new VdomNode {
      override def rawNode = n
    }

  @inline def cast(n: Any): VdomNode =
    apply(n.asInstanceOf[Raw.React.Node])

  private[vdom] val empty: VdomNode =
    apply(null)

  // TODO: [3] re-enable after ScalaComponent
  // def static(vdom: VdomNode): VdomNode =
  //   japgolly.scalajs.react.ScalaComponent.static("")(vdom)()
}
