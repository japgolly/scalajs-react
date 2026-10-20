package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.test.TestUtil._
import utest._

object TimerSupportTest extends AsyncTestSuite {

  class Backend extends TimerSupport {
    var ticks = 0
    val tick: Callback = Callback(ticks += 1)
  }

  override def tests = Tests {
    "guaranteedInterval" - {
      "noLeak" - {
        val b = new Backend
        for {
          _ <- b.setGuaranteedIntervalMs(b.tick, 10).asAsyncCallback
          _  = assertEq("initial", b.unmountProcCount(), 1)
          _ <- AsyncCallback.unit.delayMs(55) // Wait for multiple ticks
          _  = assert(b.ticks >= 2)
          _  = assertEq("after ticks", b.unmountProcCount(), 1)
        } yield ()
      }
      "stopOnUnmountDuringExecution" - {
        val b = new Backend
        var unmounted = false
        val tickAndUnmount: Callback = Callback {
          b.ticks += 1
          if (b.ticks == 1) {
            unmounted = true
            b.unmount.runNow()
          }
        }
        for {
          _ <- b.setGuaranteedIntervalMs(tickAndUnmount, 10).asAsyncCallback
          _ <- AsyncCallback.unit.delayMs(60)
          _  = assert(unmounted)
          _  = assertEq("no ticks after unmount", b.ticks, 1)
        } yield ()
      }
    }
  }
}
