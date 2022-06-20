package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.console

object HooksPrimative {

  val Component = ScalaFnComponent.withHooks[Unit]

    .localVal(1111)
    .localValBy(_ => 2222)
    .localValBy((_, _, _) => 3333)

    .localLazyVal(4444)
    .localLazyValBy(_ => 5555)
    .localLazyValBy((_, _, _, _, _, _) => 6666)

    .localVar(7777)
    .localVarBy(_ => 8888)
    .localVarBy((_, _, _, _, _, _, _, _, _) => 9999)

    .unchecked(console.log("aaaaa"))
    .uncheckedBy(_ => "bbbbb")
    .uncheckedBy((_, _, _, _, _, _, _, _, _, _, _) => console.log("ccccc"))

    .renderRR {  (_, a, b, c, d, e, f, g, h, i, j) =>
      val sum = a + b + c + d() + e() + f() + g.value + h.value + i.value + j.length
      sum
    }
}
