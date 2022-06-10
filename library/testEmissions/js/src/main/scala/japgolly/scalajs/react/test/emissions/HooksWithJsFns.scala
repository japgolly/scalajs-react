package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.hooks.HookCtx
import japgolly.scalajs.react.vdom.html_<^._
import scala.scalajs.js

object HooksWithJsFns {

  private type US = Hooks.UseState[Int]

  private def jsState0() =
    123

  private def jsState1: js.Function2[Int, US, Int] =
    _ + _.value

  private val jsState2: js.Function1[HookCtx.P2[Int, US, US], Int] =
    $ => $.props + $.hook1.value + $.hook2.value

  private def render: js.Function4[Int, US, US, US, VdomNode] =
    (p, s1, s2, s3) => {
      val sum = p + s1.value + s2.value + s3.value
      <.button(
        "Sum = ", sum,
        ^.onClick --> s1.modState(_ + 1),
      )
    }

  val Component = ScalaFnComponent.withHooks[Int]
    .useState(jsState0())
    .useStateBy(jsState1)
    .useStateBy(jsState2)
    .render(render)
}
