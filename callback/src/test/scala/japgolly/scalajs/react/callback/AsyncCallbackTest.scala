package japgolly.scalajs.react.callback

import cats.Monad
import japgolly.microlibs.testutil.TestUtil._
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits._
import scala.util.Success
import sourcecode.Line
import utest._

object AsyncCallbackTest extends TestSuite {

  private case class Log() {
    private var l = Vector.empty[String]
    def logs = l
    def +=(s: String) = l :+= s
    def apply(s: String) = Callback { this += s }
  }

  private final val asyncTestTimeout = 2000

  private def asyncTest[A](ac: AsyncCallback[A]): Future[A] = {
    ac.timeoutMs(asyncTestTimeout).map {
      case Some(a) => a
      case None    => fail(s"Async test timed out after ${asyncTestTimeout / 1000} sec.")
    }.unsafeToFuture()
  }

  private def assertMutexRejection(mutex: AsyncCallback[Unit] => AsyncCallback[Unit])(implicit l: Line): AsyncCallback[Unit] =
    mutex(AsyncCallback.unit).timeoutMs(500).delayMs(1).map(o => assertEq(o.isDefined, false))

  private def testInMutex(mutex: AsyncCallback[Unit] => AsyncCallback[Unit])
                         (test: AsyncCallback[Any]): AsyncCallback[Unit] =
    for {
      b <- AsyncCallback.barrier.asAsyncCallback
      _ <- mutex(b.await).fork_.asAsyncCallback // mutex start
      _ <- test
      _ <- b.complete.asAsyncCallback // mutex end
      _ <- mutex(AsyncCallback.unit)
    } yield ()

  private def testWriteMutex(mutex: AsyncCallback[Unit] => AsyncCallback[Unit]): AsyncCallback[Unit] =
    testInMutex(mutex)(assertMutexRejection(mutex))

  override def tests = Tests {

    "fromCallback" - {
      val log = Log()

      "async" - {
        val cb = log("async").async.toCallback >> log("post")
        cb.runNow()
        log.logs ==> Vector("post") // "async" will be scheduled by JS sometime after this test
        cb.runNow()
        log.logs ==> Vector("post", "post")
      }

      "asAsyncCallback" - {
        val cb = log("async").asAsyncCallback.toCallback >> log("post")
        cb.runNow()
        log.logs ==> Vector("async", "post")
        cb.runNow()
        log.logs ==> Vector("async", "post", "async", "post")
      }
    }

    "fromFuture" - {
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

    "promise" - {
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

    "toCallback purity" - {
      def test(f: (=> Unit) => AsyncCallback[Unit]): Unit = {
        var runs = 0
        val a = f{ runs += 1 }
        runs ==> 0
        val c = a.toCallback
        runs ==> 0
        c.runNow()
        runs ==> 1
        c.runNow()
        runs ==> 2
      }

      "delay" - test(AsyncCallback.delay(_))
    }

    "stackSafety" - {
      type F[A] = AsyncCallback[A]
      implicit object monad extends Monad[F] {
        override def pure[A](a: A) = AsyncCallback.pure(a)
        override def flatMap[A, B](fa: F[A])(f: A => F[B]) = fa.flatMap(f)
        override def tailRecM[A, B](a: A)(f: A => F[Either[A,B]]) = AsyncCallback.tailrec(a)(f)
      }
      "nestedFlatMapsInTailrecLoop"    - StackSafety.nestedFlatMapsInTailrecLoop[F]
      "nestedFlatMapsInNonTailrecLoop" - StackSafety.nestedFlatMapsInNonTailrecLoop[F]
    }

    "sync" - {
      "sync" - {
        val a = AsyncCallback.pure(123)
        val r = a.sync.runNow()
        r ==> Right(123)
        a.unsafeRunNowSync() ==> 123
      }
      "async" - {
        val r = AsyncCallback.pure(123).delayMs(1).sync.runNow()
        r.isLeft ==> true
      }
    }

    "barrier" - {
      val b = AsyncCallback.barrier.runNow()
      assertEq(b.isComplete.runNow(), false)
      b.complete.runNow()
      assertEq(b.isComplete.runNow(), true)
    }

    "debounce" - {
      val t = new TestTimer
      var i = 0
      val c = AsyncCallback.debounce(100, AsyncCallback.delay(i += 1))(t).toCallback

      c.runNow()
      assertEq(i, 0)
      t.progressTimeBy(90)
      assertEq(i, 0)
      t.progressTimeBy(20)
      assertEq(i, 1)
      t.progressTimeBy(120)
      assertEq(i, 1)

      c.runNow()
      c.runNow()
      t.progressTimeBy(80)
      c.runNow()
      t.progressTimeBy(20)
      assertEq(i, 1)
      t.progressTimeBy(70)
      assertEq(i, 1)
      t.progressTimeBy(20)
      assertEq(i, 2)

      t.progressTimeBy(2000)
      assertEq(i, 2)
    }

    "fork" - {
      "ok" - asyncTest {
        for {
          (task, completeTask) <- AsyncCallback.promise[Int].asAsyncCallback
          forked               <- task.fork.asAsyncCallback
          _                    <- forked.isComplete.asAsyncCallback.tap(assertEq(_, false))
          _                    <- completeTask(Success(123)).asAsyncCallback
          _                    <- forked.await.tap(assertEq(_, 123))
          _                    <- forked.isComplete.asAsyncCallback.tap(assertEq(_, true))
        } yield ()
      }

      "ko" - asyncTest {
        for {
          f <- AsyncCallback.throwException[Int](new RuntimeException("argh")).delayMs(1).fork.asAsyncCallback
          t <- f.await.attemptTry
        } yield assert(t.isFailure)
      }
    }

    "countDownLatch" - {
      "3" - asyncTest {
        var t = 0
        var completedAt = -1
        for {
          b <- AsyncCallback.barrier.asAsyncCallback
          l <- AsyncCallback.countDownLatch(3).asAsyncCallback
          _ <- l.await.flatMapSync(_ => {completedAt = t; b.complete}).fork_.asAsyncCallback
          _ <- l.pending.asAsyncCallback.tap(assertEq(_, 3))
          _ <- l.isComplete.asAsyncCallback.tap(assertEq(_, false))

          _ <- l.countDown.asAsyncCallback
          _ <- l.pending.asAsyncCallback.tap(assertEq(_, 2))
          _ <- l.isComplete.asAsyncCallback.tap(assertEq(_, false))

          _ <- l.countDown.asAsyncCallback
          _ <- l.pending.asAsyncCallback.tap(assertEq(_, 1))
          _ <- l.isComplete.asAsyncCallback.tap(assertEq(_, false))

          _ <- AsyncCallback.delay { t = 5 }
          _ <- l.countDown.asAsyncCallback
          _ <- l.pending.asAsyncCallback.tap(assertEq(_, 0))
          _ <- l.isComplete.asAsyncCallback.tap(assertEq(_, true))
          _ <- l.await.map(_ => completedAt = t).fork_.asAsyncCallback
          _ <- b.await

        } yield assertEq(completedAt, 5)
      }

      "0" - asyncTest {
        for {
          l <- AsyncCallback.countDownLatch(0).asAsyncCallback
          _ <- l.pending.asAsyncCallback.tap(assertEq(_, 0))
          _ <- l.isComplete.asAsyncCallback.tap(assertEq(_, true))
          _ <- l.await
        } yield ()
      }
    }

    "awaitAll" - {
      "ok" - asyncTest {
        for {
          b1 <- AsyncCallback.barrier.asAsyncCallback
          b2 <- AsyncCallback.barrier.asAsyncCallback
          f  <- AsyncCallback.awaitAll(b1.await.ret(1), b2.await.ret("a")).fork.asAsyncCallback
          _  <- f.isComplete.asAsyncCallback.tap(assertEq(_, false))
          _  <- b1.complete.asAsyncCallback
          _  <- f.isComplete.asAsyncCallback.tap(assertEq(_, false))
          _  <- b2.complete.asAsyncCallback
          _  <- f.await
          _  <- f.isComplete.asAsyncCallback.tap(assertEq(_, true))
        } yield ()
      }

      "ko" - asyncTest {
        val b1 = AsyncCallback.unit
        val b2 = AsyncCallback.throwException(new RuntimeException("yep")).delayMs(1)
        for {
          t <- AsyncCallback.awaitAll(b1, b2).attemptTry
        } yield assert(t.isFailure)
      }
    }

    "mutex" - asyncTest {
      for {
        mutex <- AsyncCallback.mutex.asAsyncCallback
        _     <- testWriteMutex(mutex(_))
      } yield ()
    }

    "readWriteMutex" - asyncTest {
      for {
        mutex <- AsyncCallback.readWriteMutex.asAsyncCallback

        _     <- testWriteMutex(mutex.write) // confirm write blocks writes

        b1    <- AsyncCallback.barrier.asAsyncCallback
        b2    <- AsyncCallback.barrier.asAsyncCallback
        l     <- AsyncCallback.countDownLatch(2).asAsyncCallback
        _     <- mutex.read(l.countDown.asAsyncCallback >> b1.await).fork_.asAsyncCallback // read mutex enter
        _     <- mutex.read(l.countDown.asAsyncCallback >> b2.await).fork_.asAsyncCallback // read mutex enter
        _     <- l.await // confirm we've got two readers in the mutex at the same time
        _     <- assertMutexRejection(mutex.write) // confirm we can't use the write mutex
        _     <- b1.complete.asAsyncCallback // read mutex exit
        _     <- b2.complete.asAsyncCallback // read mutex exit

        _     <- testInMutex(mutex.write)(assertMutexRejection(mutex.read)) // confirm write blocks reads

      } yield ()
    }

    "ref" - asyncTest {
      for {
        ref <- AsyncCallback.ref[Int].asAsyncCallback
        g1  <- ref.get.fork.asAsyncCallback
        _   <- ref.getIfAvailable.asAsyncCallback.tap(assertEq(_, None))
        _   <- ref.setIfUnset(123)
        _   <- ref.getIfAvailable.asAsyncCallback.tap(assertEq(_, Some(123)))
        _   <- ref.setIfUnset(456)
        _   <- ref.getIfAvailable.asAsyncCallback.tap(assertEq(_, Some(123)))
        _   <- g1.await.tap(assertEq(_, 123))
        _   <- ref.set(987)
        _   <- ref.getIfAvailable.asAsyncCallback.tap(assertEq(_, Some(987)))
        _   <- ref.get.tap(assertEq(_, 987))
      } yield ()
    }

  }
}
