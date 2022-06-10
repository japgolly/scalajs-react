package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.hooks.HookCtx
import japgolly.scalajs.react.vdom.html_<^._

object HooksWithScalaFns {

  private type US = Hooks.UseState[Int]

  private def state0() =
    123

  private def state1(p: Int, s: US) =
    p + s.value

  private val state2: HookCtx.P2[Int, US, US] => Int =
    $ => $.props + $.hook1.value + $.hook2.value

  private val state3: (Int, US, US, US) => Int =
    _ + _.value + _.value + _.value

  private def render(p: Int, s1: US, s2: US, s3: US, s4: US) = {
    val sum = p + s1.value + s2.value + s3.value + s4.value
    <.button(
      "Sum = ", sum,
      ^.onClick --> s1.modState(_ + 1),
    )
  }

  val Component = ScalaFnComponent.withHooks[Int]
    .useState(state0())
    .useStateBy(state1(_, _))
    .useStateBy(state2)
    .useStateBy(state3)
    .render(render _)
}
