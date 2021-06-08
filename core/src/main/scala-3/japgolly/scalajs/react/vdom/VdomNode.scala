package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{raw => Raw}
import scala.util.NotGiven

trait VdomNode extends TagMod {
  def rawNode: Raw.React.Node

  override def applyTo(b: VdomBuilder): Unit =
    b.appendChild(rawNode)
}

object VdomNode {

  def apply(n: Raw.React.Node): VdomNode =
    new VdomNode {
      override def rawNode = n
    }

  inline def cast(inline n: Any): VdomNode =
    apply(n.asInstanceOf[Raw.React.Node])

  private[vdom] val empty: VdomNode =
    apply(null)

  inline def static(vdom: VdomNode): VdomNode =
    japgolly.scalajs.react.ScalaComponent.static("")(vdom).ctor()
}

trait VdomNodeScalaSpecificImplicits {
  inline implicit def vdomNodeFromRawReactNode(v: Raw.React.Node)(using
      inline evU: NotGiven[v.type <:< Unit],
      inline evB: NotGiven[v.type <:< Boolean],
    ): VdomNode =
    VdomNode(v)
}