package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.console

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

    .renderRR { (_, a, b, c, d, e, f) =>
      val sum = a + b + c + d.value + e.value + f.value
      sum
    }
}
