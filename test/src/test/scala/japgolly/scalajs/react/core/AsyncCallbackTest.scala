package japgolly.scalajs.react.core

import japgolly.scalajs.react.{AsyncCallback, Callback}
import utest._

object AsyncCallbackTest extends TestSuite {

  private case class Log() {
    private var l = Vector.empty[String]
    def logs = l
    def +=(s: String) = l :+= s
    def apply(s: String) = Callback { this += s }
  }

  override def tests = Tests {

    'Callback {
      val log = Log()

      'async {
        val cb = log("async").async.toCallback >> log("post")
        cb.runNow()
        log.logs ==> Vector("post")
      }

      'asAsyncCallback {
        val cb = log("async").asAsyncCallback.toCallback >> log("post")
        cb.runNow()
        log.logs ==> Vector("async", "post")
      }
    }

  }
}
