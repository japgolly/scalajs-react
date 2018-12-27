package japgolly.scalajs.react.feature

import japgolly.scalajs.react.raw.React.{LazyResult, UnderlyingLazyResult}
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.{Callback, CallbackTo, raw => Raw}
import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js

final class Lazy(val raw: Raw.React.Lazy) extends AnyVal

object Lazy extends LazyDsl {

  def fromJs(raw: Raw.React.Lazy): Lazy =
    new Lazy(raw)

  override def promise(f: (VdomNode => Callback) => Callback): Lazy = {
    val p: () => js.Promise[LazyResult] =
      () => new js.Promise[LazyResult]((resolve, _) =>
        f(node => Callback(resolve(toRawResult(node)))).runNow())
    fromJs(Raw.React.`lazy`(p))
  }

  def toRawResult(input: VdomNode): LazyResult = {
    val node: UnderlyingLazyResult = input.rawNode
    js.Dynamic.literal(default = node.asInstanceOf[js.Any]).asInstanceOf[LazyResult]
  }
}

trait LazyDsl extends Any {
  def promise(f: (VdomNode => Callback) => Callback): Lazy

  final def apply(n: => VdomNode): Lazy =
    fromCallback(CallbackTo(n))

  final def fromCallback(c: CallbackTo[VdomNode]): Lazy =
    promise(c.flatMap)

  final def delay(by: FiniteDuration): LazyDsl.Delay =
    delayMs(by.toMillis)

  final def delayMs(ms: Double): LazyDsl.Delay =
    new LazyDsl.Delay(ms)
}

object LazyDsl {
  final class Delay(private val ms: Double) extends AnyVal with LazyDsl {
    override def promise(f: (VdomNode => Callback) => Callback): Lazy =
      Lazy.promise(resolve => f(resolve).delayMs(ms).void)
  }
}