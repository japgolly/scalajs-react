package japgolly.scalajs.react.internal

import scala.scalajs.js

final class WeakMap[-K <: js.Object, V](val raw: WeakMap.Raw[K, V]) extends AnyVal {

  def getOrSet(key: K)(value: => V): V =
    raw.get(key).getOrElse {
      val v = value
      raw.set(key, v)
      v
    }

  def castK[K2[_], V2[_]]: WeakMap.CastK[K2, V2] =
    new WeakMap.CastK[K2, V2](this.asInstanceOf[WeakMap[js.Object, Any]])
}

object WeakMap {

  def apply[K <: js.Object, V](): WeakMap[K, V] = {
    val raw = js.Dynamic.newInstance(js.Dynamic.global.WeakMap)().asInstanceOf[Raw[K, V]]
    new WeakMap(raw)
  }

  @js.native
  trait Raw[-K <: js.Object, V] extends js.Object {
    def get(k: K): js.UndefOr[V] = js.native
    def set(k: K, v: V): this.type = js.native
    def delete(k: K, v: V): Boolean = js.native
    def has(k: K): Boolean = js.native
  }

  final class CastK[K[_], V[_]](m: WeakMap[js.Object, Any]) {
    def getOrSet[A](key: K[A])(value: => V[A]): V[A] =
      m.getOrSet(key.asInstanceOf[js.Object])(value).asInstanceOf[V[A]]
  }
}
