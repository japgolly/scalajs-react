package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.facade
import japgolly.scalajs.react.internal.ScalaJsReactConfigMacros

trait VdomNode extends TagMod {
  def rawNode: facade.React.Node

  override def applyTo(b: VdomBuilder): Unit =
    b.appendChild(rawNode)
}

object VdomNode {
  def apply(n: facade.React.Node): VdomNode =
    new VdomNode {
      override def rawNode = n
    }

  @inline def cast(n: Any): VdomNode =
    apply(n.asInstanceOf[facade.React.Node])

  private[vdom] val empty: VdomNode =
    apply(null)

  def static(vdom: VdomNode): VdomNode =
    macro ScalaJsReactConfigMacros.vdomNodeStatic
}

trait VdomNodeScalaSpecificImplicits {
  // I have no idea why I don't need to prevent Unit or Boolean here, but the tests say its fine /shrug
  @inline implicit def vdomNodeFromRawReactNode(v: facade.React.Node): VdomNode =
    VdomNode(v)
}