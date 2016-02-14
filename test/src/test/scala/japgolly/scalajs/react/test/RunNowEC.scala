package japgolly.scalajs.react.test

import scala.concurrent.ExecutionContextExecutor

object RunNowEC extends ExecutionContextExecutor {
  object Implicit {
    implicit def runNow: ExecutionContextExecutor = RunNowEC
  }

  def execute(runnable: Runnable): Unit = {
    try {
      runnable.run()
    } catch {
      case t: Throwable => reportFailure(t)
    }
  }

  def reportFailure(t: Throwable): Unit =
    t.printStackTrace()
}
