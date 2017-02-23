package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{raw => Raw}
import scala.scalajs.js

sealed class ReactNode(val rawReactNode: Raw.ReactNode) extends TagMod {
  override def applyTo(b: Builder): Unit =
    b.appendChild(rawReactNode)
}

object ReactNode {
  def apply(n: Raw.ReactNode): ReactNode =
    new ReactNode(n)

  def cast(n: Any): ReactNode =
    new ReactNode(n.asInstanceOf[Raw.ReactNode])
}

// =====================================================================================================================

final class ReactElement(val rawReactElement: Raw.ReactElement) extends ReactNode(rawReactElement)

object ReactElement {
  def apply(n: Raw.ReactElement): ReactElement =
    new ReactElement(n)
}

// =====================================================================================================================

final class ReactArray(val rawArray: js.Array[Raw.ReactNode]) extends ReactNode(rawArray.asInstanceOf[Raw.ReactNode]) {

  def +(n: ReactNode): this.type = {
    rawArray push n.rawReactNode
    this
  }

  def ++[A](as: TraversableOnce[A])(implicit f: A => ReactNode): this.type = {
    for (a <- as)
      rawArray push f(a).rawReactNode
    this
  }
}

object ReactArray {
  def empty(): ReactArray =
    new ReactArray(new js.Array)

  def apply(ns: ReactNode*): ReactArray =
    empty() ++ ns
}