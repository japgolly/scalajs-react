package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object CustomHooks {

  val customHook1 = CustomHook[Int].useStateBy(_ + 1).buildReturning(_.hook1)
  val customHook2 = CustomHook[Unit].useEffect(Callback.log("hello")).build
  val customHook3 = CustomHook[Unit].useState(123).buildReturning(_.hook1)

  val Component = ScalaFnComponent.withHooks[Int]
    .custom(customHook1)
    .custom(customHook2)
    .custom(customHook3)
    .render { (p, s1, s2) =>
      val sum = p + s1.value + s2.value
      <.div("Sum = ", sum)
    }
}
