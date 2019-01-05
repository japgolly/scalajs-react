package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{raw => Raw}

trait VdomNode extends VdomNodeOrTagMod {
  def rawNode: Raw.React.Node

  override def applyTo(b: Builder): Unit =
    b.appendChild(rawNode)

  def ~(next: VdomNode): VdomNode =
    VdomFragment.two(this, next)
}

object VdomNode {
  def apply(n: Raw.React.Node): VdomNode =
    new VdomNode {
      override def rawNode = n
    }

  def cast(n: Any): VdomNode =
    apply(n.asInstanceOf[Raw.React.Node])

  private[vdom] val empty: VdomNode =
    apply(null)
}
