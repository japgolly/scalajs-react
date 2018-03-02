package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{Callback, raw => Raw}
import scala.scalajs.js

sealed class VdomNode(val rawNode: Raw.React.Node) extends TagMod {
  override def applyTo(b: Builder): Unit =
    b.appendChild(rawNode)
}

object VdomNode {
  def apply(n: Raw.React.Node): VdomNode =
    new VdomNode(n)

  def cast(n: Any): VdomNode =
    new VdomNode(n.asInstanceOf[Raw.React.Node])

  private[vdom] val empty: VdomNode =
    apply(null)

}

// =====================================================================================================================

final class VdomElement(val rawElement: Raw.React.Element) extends VdomNode(rawElement) {
  def renderIntoDOM(container: Raw.ReactDOM.Container, callback: Callback = Callback.empty): Raw.React.ComponentUntyped =
    Raw.ReactDOM.render(rawElement, container, callback.toJsFn)
}

object VdomElement {
  def apply(n: Raw.React.Element): VdomElement =
    new VdomElement(n)
}

// =====================================================================================================================

/** Elements must have keys. Array itself cannot. */
final class VdomArray(val rawArray: js.Array[Raw.React.Node]) extends VdomNode(rawArray.asInstanceOf[Raw.React.Node]) {

  def +=(n: VdomNode): this.type = {
    rawArray push n.rawNode
    this
  }

  def ++=[A](as: TraversableOnce[A])(implicit f: A => VdomNode): this.type = {
    for (a <- as)
      rawArray push f(a).rawNode
    this
  }
}

object VdomArray {
  def empty(): VdomArray =
    new VdomArray(new js.Array)

  /** Elements must have keys. Array itself cannot. */
  def apply(ns: VdomNode*): VdomArray =
    empty() ++= ns
}
