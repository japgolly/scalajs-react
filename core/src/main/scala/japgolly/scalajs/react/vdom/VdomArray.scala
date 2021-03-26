package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{raw => Raw}
import scala.collection.compat._
import scala.scalajs.js

/** This is mutable so don't let it escape a local pure function.
  * Elements must have keys.
  * VdomArray itself cannot be assigned a key.
  */
final class VdomArray(val rawArray: js.Array[Raw.React.Node]) extends VdomNode {

  override def rawNode = rawArray.asInstanceOf[Raw.React.Node]

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
