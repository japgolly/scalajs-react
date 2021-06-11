package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.facade
import japgolly.scalajs.react.internal.SafeEffect
import scala.util.NotGiven

trait VdomNode extends TagMod {
  def rawNode: facade.React.Node

  override def applyTo(b: VdomBuilder): Unit =
    b.appendChild(rawNode)

  @inline final def renderIntoDOM(container: facade.ReactDOM.Container): facade.React.ComponentUntyped =
    facade.ReactDOM.render(rawNode, container)

  @inline final def renderIntoDOM[F[_], A](container: facade.ReactDOM.Container, callback: => F[A])(implicit F: SafeEffect.Sync[F]): facade.React.ComponentUntyped =
    facade.ReactDOM.render(rawNode, container, F.toJsFn0(callback))
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