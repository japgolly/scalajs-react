package japgolly.scalajs.react.feature

import japgolly.scalajs.react.raw.React.{LazyResult, UnderlyingLazyResult}
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{AsyncCallback, raw => Raw}
import scala.scalajs.js

final class Lazy(val raw: Raw.React.Lazy) extends AnyVal

object Lazy {

  def apply(n: => VdomNode): Lazy =
    apply(AsyncCallback.point(n).delayMs(0))

  def apply(a: AsyncCallback[VdomNode]): Lazy = {
    val p = () => a.map(toRawResult).unsafeToJsPromise()
    fromJs(Raw.React.`lazy`(p))
  }

  def toRawResult(input: VdomNode): LazyResult = {
    val node: UnderlyingLazyResult = input.rawNode
    js.Dynamic.literal(default = node.asInstanceOf[js.Any]).asInstanceOf[LazyResult]
  }

  def fromJs(raw: Raw.React.Lazy): Lazy =
    new Lazy(raw)
}
