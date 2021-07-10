package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.facade
import scala.scalajs.js

/** This is mutable so don't let it escape a local pure function.
  * Elements must have keys.
  * VdomArray itself cannot be assigned a key.
  */
final class VdomArray(val rawArray: js.Array[facade.React.Node]) extends VdomNode {

  override def rawNode = rawArray.asInstanceOf[facade.React.Node]

  def +=(n: VdomNode): this.type = {
    rawArray push n.rawNode
    this
  }

  def ++=[A](as: IterableOnce[A])(implicit f: A => VdomNode): this.type = {
    for (a <- as.iterator)
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
