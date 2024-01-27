package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object UseCallback {

  val Component = ScalaFnComponent.withHooks[Int]

    .useCallback(Callback.log("aaaaaaaaaaaaaa"))
    .useCallbackBy($ => Callback.log("bbbbbbbbbbbbbb" + $.props))
    .useCallbackBy((p, _, _) => Callback.log("cccccccccccccc" + p))

    .renderRR {  (p, a, b, c) =>
      a.runNow()
      b.runNow()
      c.runNow()
      p
    }
}
