package japgolly.scalajs.react.callback

import cats.effect._
import cats.effect.laws.AsyncTests
import cats.effect.testkit.TestInstances
import cats.kernel.Eq
import cats.tests.CatsSuite
import cats.{Id, Order, ~>}
import org.scalacheck._
import scala.concurrent.duration._
import scala.scalajs.js

final class AsyncTest extends CatsSuite with TestInstances {
  import CallbackCatsEffect.{AsyncAsyncCallback, AsyncCallbackToIO, ioToAsyncCallback}

  private val backlog = new js.Array[Callback]

  implicit val asyncCallbackToIO: AsyncCallbackToIO =
    new AsyncCallbackToIO(backlog.push(_))

  implicit def arbAsyncCallback[A: Arbitrary: Cogen](implicit t: Ticker, f: IO ~> AsyncCallback): Arbitrary[AsyncCallback[A]] =
    Arbitrary(arbitraryIO[A].arbitrary.map(f(_)))

  implicit def eqAsyncCallback[A](implicit A: Eq[A], t: Ticker): Eq[AsyncCallback[A]] =
    eqIOA[A].contramap(asyncCallbackToIO(_))

  implicit def ordAsyncCallbackFiniteDuration(implicit t: Ticker): Order[AsyncCallback[FiniteDuration]] =
    orderIoFiniteDuration.contramap(asyncCallbackToIO(_))

  private val someK: Id ~> Option =
    new ~>[Id, Option] { def apply[A](a: A) = a.some }

  private def unsafeRun2[A](ioa: IO[A])(implicit ticker: Ticker): Outcome[Option, Throwable, A] =
    try {
      var results: Outcome[Option, Throwable, A] = Outcome.Succeeded(None)

      ioa
        .flatMap(IO.pure(_))
        .handleErrorWith(IO.raiseError(_))
        .unsafeRunAsyncOutcome { oc => results = oc.mapK(someK) }(unsafe
          .IORuntime(ticker.ctx, ticker.ctx, scheduler, () => (), unsafe.IORuntimeConfig()))

      def go(i: Int): Unit = {
        ticker.ctx.tickAll()
        if (backlog.length > 0) {
          for (f <- backlog)
            f.reset.runNow()
          backlog.clear()
          if (i < 100)
            go(i + 1)
        }
      }
      go(0)

      results
    } catch {
      case t: Throwable =>
        t.printStackTrace()
        throw t
    }

  implicit def asyncCallbackToProp(implicit t: Ticker): AsyncCallback[Boolean] => Prop =
    a => {
      val io = asyncCallbackToIO(a)
      val x = unsafeRun2(io)
      Prop(x.fold(false, _ => false, _.getOrElse(false)))
    }

  implicit def cogenAsyncCallback[A: Cogen](implicit t: Ticker): Cogen[AsyncCallback[A]] =
    Cogen[Outcome[Option, Throwable, A]].contramap(acb => unsafeRun2(asyncCallbackToIO(acb)))

  locally {
    implicit val ticker = Ticker()
    implicit val instance = new AsyncAsyncCallback(asyncCallbackToIO, ioToAsyncCallback)
    checkAll("Async[AsyncCallback]", AsyncTests[AsyncCallback].async[Int, Int, Int](10.millis))
  }

  test("AsyncCallbackToIO cancellation") {
    implicit val ticker = Ticker()
    var cancelled = false
    var afterCancel = false
    val b = AsyncCallback.barrier.runNow()
    val (p, complete) = AsyncCallback.promise[Unit].runNow()
    val ac =
      for {
        _ <- b.complete.asAsyncCallback
        _ <- p.onCancel(AsyncCallback.delay { cancelled = true })
        _ <- AsyncCallback.delay { afterCancel = true }
      } yield ()

    val io =
      for {
        fiber <- asyncCallbackToIO(ac).start
        _     <- asyncCallbackToIO(b.await)
        _     <- fiber.cancel
        _     <- IO(complete(scala.util.Success(())).runNow())
      } yield ()

    unsafeRun2(io)
    assert(cancelled)
    assert(!afterCancel)

    assert(unsafeRun2(asyncCallbackToIO(AsyncCallback.cancel)) == Outcome.Canceled())
  }

  test("CallbackCats.callbackCatsMonadAsyncCallback ap/ap2/map2 consistency with flatMap") {
    val M = CallbackCats.callbackCatsMonadAsyncCallback
    val err = new RuntimeException("boom")
    var ran = 0
    val fa = AsyncCallback.delay { ran += 1; 1 }

    val ff1 = AsyncCallback.throwException[Int => Int](err)
    M.ap(ff1)(fa).attempt.unsafeRunNowSync()
    assert(ran == 0)

    M.map2(ff1, fa)(_(_)).attempt.unsafeRunNowSync()
    assert(ran == 0)

    val ff2 = AsyncCallback.delay[(Int, Int) => Int] { ran += 1; _ + _ }
    M.ap2(ff2)(AsyncCallback.throwException[Int](err), fa).attempt.unsafeRunNowSync()
    assert(ran == 0)
  }
}
