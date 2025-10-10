package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.facade
import japgolly.scalajs.react.util.Effect.Dispatch
import scala.util.NotGiven

trait VdomNode extends TagMod {
  def rawNode: facade.React.Node

  override def applyTo(b: VdomBuilder): Unit =
    b.appendChild(rawNode)

  @deprecated("Use ReactDOMClient.createRoot and root.render instead", "2.2.0 / React v18")
  inline final def renderIntoDOM(container: facade.ReactDOM.Container): facade.React.ComponentUntyped =
    facade.ReactDOM.render(rawNode, container)

  @deprecated("Use ReactDOMClient.createRoot and root.render instead", "2.2.0 / React v18")
  inline final def renderIntoDOM[G[_]](container: facade.ReactDOM.Container, callback: => G[Unit])(implicit G: Dispatch[G]): facade.React.ComponentUntyped =
    facade.ReactDOM.render(rawNode, container, G.dispatchFn(callback))
}

object VdomNode {

  def apply(n: facade.React.Node): VdomNode =
    new VdomNode {
      override def rawNode = n
    }

  inline def cast(inline n: Any): VdomNode =
    apply(n.asInstanceOf[facade.React.Node])

  private[vdom] val empty: VdomNode =
    apply(null)

  inline def static(vdom: VdomNode): VdomNode =
    japgolly.scalajs.react.ScalaComponent.static("")(vdom).ctor()
}

trait VdomNodeScalaSpecificImplicits {
  inline implicit def vdomNodeFromRawReactNode(v: facade.React.Node)(using
      inline evU: NotGiven[v.type <:< Unit],
      inline evB: NotGiven[v.type <:< Boolean],
    ): VdomNode =
    VdomNode(v)
}
