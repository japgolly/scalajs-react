package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object CustomHooks {

  val customHook1 = CustomHook[Int].useStateBy(_ + 1).buildReturning(_.hook1)
  val customHook2 = CustomHook[Unit].useEffect(Callback.log("hello")).build
  val customHook3 = CustomHook[Unit].useState(123).buildReturning(_.hook1)
  val customHook4 = CustomHook[Int].useStateBy(_ + 1).buildReturning(_.hook1)
  val customHook5 = CustomHook[Int].useStateBy(_ * 2).buildReturning(_.hook1)
  val customHook6 = CustomHook[Int].useStateBy(_ * 3).buildReturning(_.hook1)

  val Component = ScalaFnComponent.withHooks[Int]

    .custom(customHook1)
    .custom(customHook2)
    .custom(customHook3)

    .customBy((p, _, _) => customHook4(p - 1))
    .customBy((_, _, _, a) => customHook5(a.value))
    .customBy($ => customHook6($.hook4.value))

    .renderRR { (p, s1, s2, s3, s4, s5) =>
      val sum = p + s1.value + s2.value + s3.value + s4.value + s5.value
      <.div("Sum = ", sum)
    }
}
