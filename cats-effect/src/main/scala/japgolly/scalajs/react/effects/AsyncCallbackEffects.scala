package japgolly.scalajs.react.effects

import cats.effect._
import cats.effect.unsafe.IORuntime
import cats.~>
import japgolly.scalajs.react.{AsyncCallback, Callback}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Either

object AsyncCallbackEffects {

  import scalajs.js
  js.constructorOf[js.Error].stackTraceLimit = 200

  lazy val defaultAsyncCallbackToIO =
    asyncCallbackToIO(_.async.toCallback.runNow())

  def asyncCallbackToIO(dispatch: Callback => Unit): AsyncCallback ~> IO = new (AsyncCallback ~> IO) {
    override def apply[A](f: AsyncCallback[A]): IO[A] =
      // IO.async_[A](k => f.completeWith(t => Callback(k(t.toEither))).runNow())
      // IO.async_[A](k => f.attempt.flatMap(t => AsyncCallback.delay(k(t))))
      // IO.async[A](k => IO {
      //   f.attempt.flatMap(t => AsyncCallback.delay(k(t))).toCallback.runNow()
      //   None
      // })
      IO.async[A](k => IO {
        // f.attempt.flatMap(t => AsyncCallback.delay(k(t))).run2().map(x => IO.delay(x.runNow()))
        val s = new AsyncCallback.State
        val g = f.completeWith(s)
        val d = g(t => Callback(k(t.toEither)))

        dispatch(d)
        s.onCancelOption.map(x => IO.delay(x.runNow()))

        // def nest(c: Callback): Callback = Callback {
        //   nested = true
        //   try
        //     c.runNow()
        //   finally
        //     nested = false
        // }

        // val dispatch2 = nest(dispatch)

        // println(s"Dispatching '${f.name}' ...")
        // println(nested)

        // println(s">>>>>>>>>>>>>>>> ${f.name} -- ${s.cancels.length}")

        // if (
        //   // !nested &&
        //   false &&
        // s.cancels.isEmpty) {
        //   dispatch2.runNow()
        //   None
        // } else {
        //   // dispatch2.async.toCallback.runNow()
        //   s.onCancelOption.map(x => IO.delay(nest(x).runNow()))
        // }

        // def result() = s.onCancelOption.map(x => IO.delay(x.runNow()))
        // try {
        //   dispatch.runNow()
        // } catch {
        //   case t: Throwable =>
        //     // result()
        //     t.printStackTrace()
        //     throw t
        // }
        // result()
      })
  }

  // var nested = false

  def xxxxxx(name: String, r: Exception = new RuntimeException()):Unit = {
    println()
    println(name + ":")
    // r.printStackTrace(System.out)
    for (f <- r.getStackTrace().iterator.drop(1)) {
      val s = f.toString()
      // if (!s.contains("AnonFunctions"))
      if (s.contains("/japgolly/"))
        println(s
          .replace("/home/golly/projects/public/scalajs-react/cats-effect/target/scala-2.13/ext-cats-effect-test-fastopt/file:", "")
          .replaceAll("/?home/golly/projects/public/scalajs-react/([^.]+?/)+", "SJR/")
          .replace("japgolly.scalajs.react.effects.AsyncCallbackEffects$", "")
          .replace("AsyncAsyncCallback.japgolly$scalajs$react$effects$AsyncCallbackEffects$", "")
        )
    }
  }

  var x0 = 0
  var x1 = 0
  var x2 = 0
  class IOToAsyncCallback(r: IORuntime) extends (IO ~> AsyncCallback) { self =>
    println("IOToAsyncCallback")
    override def apply[A](f: IO[A]): AsyncCallback[A] = {

      x0 += 1
      // println("x0: " + x0)
      // if (x0 == 170000) xxxxxx("X0")

      // AsyncCallback[A](k => Callback(f.unsafeRunAsync(t => k(t.toTry).runNow())(r)))
      AsyncCallback[A] { k =>
        x1 += 1
        // println("x1: " + x1)
        // if (x1 == 174000) xxxxxx("X1")
        Callback {
          x2 += 2
          // println("x2: " + x2)
          // if (x2 == 349000) xxxxxx("X2")

          f.unsafeRunAsync { t =>
            // println("x3")
            k(t.toTry).runNow()
          }(r)
        }
      }
    }

    def transFiber[E, A](f: Fiber[IO, E, A]): Fiber[AsyncCallback, E, A] =
      new Fiber[AsyncCallback, E, A] {
        override def cancel = apply(f.cancel)
        override def join   = apply(f.join.map(_.mapK(self)))
      }
  }

  // implicit def ioToAsyncCallback(implicit r: IORuntime): IOToAsyncCallback =
  //   new IOToAsyncCallback(r)

  // ===================================================================================================================

  // implicit def asyncAsyncCallback(implicit r: IORuntime): AsyncAsyncCallback =
  //   new AsyncAsyncCallback(new IOToAsyncCallback(r))

  final class AsyncAsyncCallback(ac: IOToAsyncCallback, io: AsyncCallback ~> IO) extends Async[AsyncCallback] {
    println("AsyncAsyncCallback")

    // @inline
    private implicit def autoIoToAsyncCallback[A](f: IO[A]): AsyncCallback[A] =
      ac(f)

    @noinline override def unit: AsyncCallback[Unit] =
      AsyncCallback.unit

    // override def never[A] =
    //   AsyncCallback.never

    @noinline override def pure[A](x: A): AsyncCallback[A] =
      AsyncCallback.pure(x)

    @noinline override def raiseError[A](e: Throwable): AsyncCallback[A] =
      AsyncCallback.throwException(e)

    @noinline override def handleErrorWith[A](fa: AsyncCallback[A])(f: Throwable => AsyncCallback[A]): AsyncCallback[A] =
      fa.handleError(f)

    @noinline override def flatMap[A, B](fa: AsyncCallback[A])(f: A => AsyncCallback[B]): AsyncCallback[B] =
      fa.flatMap(f)

    @noinline override def tailRecM[A, B](a: A)(f: A => AsyncCallback[Either[A,B]]): AsyncCallback[B] =
      AsyncCallback.tailrec(a)(f)

    @noinline override def forceR[A, B](fa: AsyncCallback[A])(fb: AsyncCallback[B]): AsyncCallback[B] =
      Async[IO].forceR(
        io(fa))(
          io(fb))

    @noinline override def uncancelable[A](body: Poll[AsyncCallback] => AsyncCallback[A]): AsyncCallback[A] =
      Async[IO].uncancelable { pollIO =>
        val poll = new Poll[AsyncCallback] {
          override def apply[A](fa: AsyncCallback[A]): AsyncCallback[A] =
            pollIO(io(fa))
        }
        io(body(poll))
      }

    @noinline override def canceled: AsyncCallback[Unit] =
      AsyncCallback.canceled

    @noinline override def onCancel[A](fa: AsyncCallback[A], fu: AsyncCallback[Unit]): AsyncCallback[A] =
      fa.onCancel(fu)
      // io(fa)
      // .onCancel(
      //   io(fu))

      var mon = 0
    @noinline override def monotonic: AsyncCallback[FiniteDuration] = {
      mon += 1
      val i = mon
      val x =
      AsyncCallback.delay {
        val l = System.nanoTime().nanos
        // println(s"[$i] WTF? $l")
        l
      }
      x.name = s"mon $i"
      x
    }

    @noinline override def realTime: AsyncCallback[FiniteDuration] =
      AsyncCallback.delay(System.currentTimeMillis().millis)

    @noinline override def suspend[A](hint: kernel.Sync.Type)(thunk: => A): AsyncCallback[A] =
      AsyncCallback.delay(thunk)

    @noinline override def start[A](fa: AsyncCallback[A]): AsyncCallback[Fiber[AsyncCallback,Throwable,A]] =
      io(fa).start.map(ac.transFiber(_))

    @noinline override def cede: AsyncCallback[Unit] =
      IO.cede

    @noinline override def ref[A](a: A): AsyncCallback[Ref[AsyncCallback,A]] =
      IO.ref(a).map(_.mapK(ac))

    @noinline override def deferred[A]: AsyncCallback[Deferred[AsyncCallback,A]] =
      IO.deferred[A].map(_.mapK(ac))

    @noinline override def sleep(time: FiniteDuration): AsyncCallback[Unit] =
      AsyncCallback.unit.delay(time)

    @noinline override def evalOn[A](fa: AsyncCallback[A], ec: ExecutionContext): AsyncCallback[A] =
      io(fa).evalOn(ec)

    @noinline override def executionContext: AsyncCallback[ExecutionContext] =
      IO.executionContext

    @noinline override def cont[K, R](body: Cont[AsyncCallback,K,R]): AsyncCallback[R] =
      Async.defaultCont(body)(this)
  }
}