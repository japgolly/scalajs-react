package downstream.mima200

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

// Because in `object Hooks.UseReducer`, I changed `private def _unsafeCreate` to `def fromJs`
object HookUseReducer {

  final case class PI(pi: Int) {
    def unary_- : PI = PI(-pi)
    def *(n: Int): PI = PI(pi * n)
    def +(n: Int): PI = PI(pi + n)
    def +(n: PI): PI = PI(pi + n.pi)
  }

  private def add(n: Int): (Int, Int) => Int = _ + _ + n

  val comp = ScalaFnComponent.withHooks[PI]
    .useReducer(add(0), 100)
    .useReducerBy((_, s1) => add(s1.value), (p, s1) => p.pi + s1.value)
    .useReducerBy($ => add($.hook1.value), $ => $.props.pi + $.hook1.value + $.hook2.value)
    .render((p, s1, s2, s3) =>
      <.div(
        <.div(s"P=$p, s1=${s1.value}, s2=${s2.value}, s3=${s3.value}"),
        <.button(^.onClick --> s1.dispatch(1)),
        <.button(^.onClick --> s2.dispatch(10)),
        <.button(^.onClick --> s3.dispatch(100)),
    ))
}
