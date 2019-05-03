package japgolly.scalajs.react.core

import japgolly.scalajs.react.{AsyncCallback, Callback}
import utest._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

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
        log.logs ==> Vector("post") // "async" will be scheduled by JS sometime after this test
        cb.runNow()
        log.logs ==> Vector("post", "post")
      }

      'asAsyncCallback {
        val cb = log("async").asAsyncCallback.toCallback >> log("post")
        cb.runNow()
        log.logs ==> Vector("async", "post")
        cb.runNow()
        log.logs ==> Vector("async", "post", "async", "post")
      }
    }

    'AsyncCallback {
      'fromFuture {
        "should be sync if the Future has already completed" - {
          var hasRun = false
          val cb = AsyncCallback.fromFuture(Future.successful(()))
          cb.completeWith(_ => Callback {
            hasRun = true
          }).runNow()
          hasRun ==> true
          val future = cb.asCallbackToFuture.runNow()
          future.isCompleted ==> true
        }
      }

      'promise {
        val (ac, complete) = AsyncCallback.promise[Int].runNow()
        var r1,r2,r3 = 0
        ac.map(i => r1 = i).toCallback.runNow()
        r1 ==> 0
        complete(Success(666)).runNow()
        r1 ==> 666
        r2 ==> 0
        r1 = 123
        ac.map(i => r2 = i).toCallback.runNow()
        r1 ==> 123
        r2 ==> 666
        r3 ==> 0
        r2 = 123
        ac.map(i => r3 = i).toCallback.runNow()
        r1 ==> 123
        r2 ==> 123
        r3 ==> 666
      }
    }

  }
}
