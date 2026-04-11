package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.ReactTestUtils._
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.test._
import japgolly.scalajs.react.vdom.html_<^._
import scala.collection.mutable
import utest._

object UseEffectEventTest extends AsyncTestSuite {

  def tests = Tests {
    "useEffectEvent" - {
      val log = mutable.ListBuffer.empty[String]

      val Component = ScalaFnComponent.withHooks[Unit]
        .useState(0) // hook1: trigger
        .useState(0) // hook2: non-reactive
        .useEffectEventBy(ctx => CallbackTo(ctx.hook2.value)) // hook3: effect event
        .useEffectWithDepsBy(_.hook1.value)($ => trigger =>
           Callback(log += s"trigger=$trigger, event=${$.hook3.value.runNow()}")
        )
        .render($ => <.div(
          <.button(^.onClick --> $.hook2.modState(_ + 1)), // inc
          <.button(^.onClick --> $.hook1.modState(_ + 1))  // trigger
        ))

      rendered(Component()).map(d => new DomTester(d.asHtml())).use { t =>
        assertEq(log.toList, List("trigger=0, event=0"))
        log.clear()

        for {
          _ <- t.clickButton(1) // inc
          _ =  assertEq(log.toList, Nil)

          _ <- t.clickButton(2) // trigger
          _ =  assertEq(log.toList, List("trigger=1, event=1"))
          _ =  log.clear()

          _ <- t.clickButton(1)
          _ <- t.clickButton(1)
          _ =  assertEq(log.toList, Nil)

          _ <- t.clickButton(2)
          _ =  assertEq(log.toList, List("trigger=2, event=3"))
        } yield ()
      }
    }
  }
}
