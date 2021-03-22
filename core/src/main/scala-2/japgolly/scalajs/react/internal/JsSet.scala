package japgolly.scalajs.react.internal

import scala.scalajs.js
import scala.scalajs.js.annotation._

@js.native
@JSGlobal("Set")
class JsSet[A]() extends js.Object with js.Iterable[A] {
  def this(array: js.Iterable[A]) = this()

  def clear(): Unit = js.native

  @JSName(js.Symbol.iterator)
  override def jsIterator(): js.Iterator[A] = js.native

  def size: Int = js.native
}

object JsSet {
  /** Returns a new empty map */
  @inline def empty[V]: JsSet[V] = new JsSet[V]()

  @inline def apply[A](values: A*): JsSet[A] = new JsSet(js.Array(values: _*))
}