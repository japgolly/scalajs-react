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
        ticker.ctx.tickAll(1.second)
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

  locally {
    implicit val ticker = Ticker()
    implicit val instance = new AsyncAsyncCallback(asyncCallbackToIO, ioToAsyncCallback)
    checkAll("Async[AsyncCallback]", AsyncTests[AsyncCallback].async[Int, Int, Int](10.millis))
  }
}
