package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._

object ScalaSpecificHooksTest {
  import HooksTest._

  // TODO: https://github.com/lampepfl/dotty/issues/12663
  // I swapped the order of the last two hooks to avoid use of a CtxFn after a DynamicNextStep.
  def testCustomHook(): Unit = {
    val counter = new Counter

    val hookS = CustomHook[Int].useStateBy(identity).buildReturning(_.hook1)
    val hookE = CustomHook[Int].useEffectBy(counter.incCB(_)).build

    val comp = ScalaFnComponent.withHooks[PI]
      .custom(hookE(10))
      .custom(hookS(3)) // <--------------------------------------- s1
      .custom(hookS.contramap[PI](_.pi)) // <---------------------- s2
      .customBy((p, s, _) => hookE(p.pi + s.value))
      .customBy($ => hookE($.props.pi + $.hook1.value + 1))
      .customBy($ => hookS($.props.pi + $.hook1.value + 1)) // <--- s3
      .render((_, s1, s2, s3) =>
        <.div(
          s"${s1.value}:${s2.value}:${s3.value}",
          <.button(^.onClick --> s1.modState(_ + 1))
        )
      )

    test(comp(PI(5))) { t =>
      t.assertText("3:5:9")
      assertEq(counter.value, 10 + (5+3) + (5+3+1))
      counter.value = 0
      t.clickButton()
      t.assertText("4:5:9")
      assertEq(counter.value, 10 + (5+4) + (5+4+1))
    }
  }

}
