package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object HooksTrivial {

  private val ctx = React.createContext(100)
  private def add(n: Int): (Int, Int) => Int = _ + _ + n

  val Component = ScalaFnComponent.withHooks[Int]

    .useContext(ctx)
    .useContextBy(_ => ctx)
    .useContextBy((_, _, _) => ctx)

    .useDebugValue("aaaaa")
    .useDebugValueBy(_ => 321654)
    .useDebugValueBy((_, _, _, _) => 987)

    .useReducer(add(0), 100)
    .useReducerBy((p, _, _, _, _) => add(p), _ + _ + _ + _ + _.value)
    .useReducerBy($ => add($.hook4.value), $ => $.props + $.hook4.value + $.hook5.value)

    .useState(123)
    .useStateBy((p, a, _, _, _, _, f, _) => p + a + f.value)
    .useStateBy($ => $.props + $.hook1 + $.hook6.value)

    .useForceUpdate

    .renderRR { (_, a, b, c, d, e, f, g, h, i, forceUpdate) =>
      val sum = a + b + c + d.value + e.value + f.value + g.value + h.value + i.value
      <.div(sum, ^.onClick --> forceUpdate)
    }
}
