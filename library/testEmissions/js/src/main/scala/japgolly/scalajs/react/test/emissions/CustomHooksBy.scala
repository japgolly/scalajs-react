package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object CustomHooksBy {

  val customHook1 = CustomHook[Int].useStateBy(_ + 1).buildReturning(_.hook1)
  val customHook2 = CustomHook[Int].useStateBy(_ * 2).buildReturning(_.hook1)
  val customHook3 = CustomHook[Int].useStateBy(_ * 3).buildReturning(_.hook1)

  val Component = ScalaFnComponent.withHooks[Int]
    .customBy(p => customHook1(p - 1))
    .customBy((_, s1) => customHook2(s1.value))
    .customBy($ => customHook3($.hook2.value))
    .render { (p, s1, s2, s3) =>
      val sum = p + s1.value + s2.value + s3.value
      <.div("Sum = ", sum)
    }
}
