package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._

object ScalaSpecificHooksTest {
  import HooksTest._

  // TODO: https://github.com/lampepfl/dotty/issues/12663
  // This is the original version
  def testCustomHook(): Unit = {
    val counter = new Counter
    val hookS = CustomHook[Int].useStateBy(identity).buildReturning(_.hook1)
    val hookE = CustomHook[Int].useEffectBy(counter.incCB(_)).build

    val comp = ScalaFnComponent.withHooks[PI]
      .custom(hookE(10))
      .custom(hookS(3)) // <--------------------------------------- s1
      .custom(hookS.contramap[PI](_.pi)) // <---------------------- s2
      .customBy((p, s, _) => hookE(p.pi + s.value))
      .customBy($ => hookS($.props.pi + $.hook1.value + 1)) // <--- s3
      .customBy($ => hookE($.props.pi + $.hook1.value + 1))
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

  // Copy of above but using renderRR
  def testCustomHookRR(): Unit = {
    val counter = new Counter
    val hookS = CustomHook[Int].useStateBy(identity).buildReturning(_.hook1)
    val hookE = CustomHook[Int].useEffectBy(counter.incCB(_)).build

    val comp = ScalaFnComponent.withHooks[PI]
      .custom(hookE(10))
      .custom(hookS(3)) // <--------------------------------------- s1
      .custom(hookS.contramap[PI](_.pi)) // <---------------------- s2
      .customBy((p, s, _) => hookE(p.pi + s.value))
      .customBy($ => hookS($.props.pi + $.hook1.value + 1)) // <--- s3
      .customBy($ => hookE($.props.pi + $.hook1.value + 1))
      .renderRR((_, s1, s2, s3) =>
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
