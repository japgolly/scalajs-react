package japgolly.scalajs.react

import cats.effect._
import cats.effect.kernel.CancelScope
import cats.effect.unsafe.IORuntime
import cats.~>
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Either

object ScalaJsReactCatsEffect {

  // ===================================================================================================================
  // Sync, concrete

  implicit lazy val syncIOToCallback: (SyncIO ~> CallbackTo) =
    new (SyncIO ~> CallbackTo) {
      override def apply[A](f: SyncIO[A]): CallbackTo[A] =
        CallbackTo(f.unsafeRunSync())
    }

  implicit lazy val callbackToSyncIO: (CallbackTo ~> SyncIO) =
    new (CallbackTo ~> SyncIO) {
      override def apply[A](f: CallbackTo[A]): SyncIO[A] =
        SyncIO(f.runNow())
    }

  implicit lazy val syncCallback: Sync[CallbackTo] =
    new Sync[CallbackTo] {

      override def unit: CallbackTo[Unit] =
        Callback.empty

      override def pure[A](x: A): CallbackTo[A] =
        CallbackTo.pure(x)

      override def raiseError[A](e: Throwable): CallbackTo[A] =
        CallbackTo.throwException(e)

      override def handleErrorWith[A](fa: CallbackTo[A])(f: Throwable => CallbackTo[A]): CallbackTo[A] =
        fa.handleError(f)

      override def flatMap[A, B](fa: CallbackTo[A])(f: A => CallbackTo[B]): CallbackTo[B] =
        fa.flatMap(f)

      override def tailRecM[A, B](a: A)(f: A => CallbackTo[Either[A,B]]): CallbackTo[B] =
        CallbackTo.tailrec(a)(f)

      override def rootCancelScope: CancelScope =
        CancelScope.Uncancelable

      override def forceR[A, B](fa: CallbackTo[A])(fb: CallbackTo[B]): CallbackTo[B] =
        fa.attempt >> fb

      override def uncancelable[A](body: Poll[CallbackTo] => CallbackTo[A]): CallbackTo[A] =
        body(pollId)

      private lazy val pollId: Poll[CallbackTo] =
        new Poll[CallbackTo] {
          override def apply[A](fa: CallbackTo[A]) = fa
        }

      override def canceled: CallbackTo[Unit] =
        Callback.empty

      override def onCancel[A](fa: CallbackTo[A], fin: CallbackTo[Unit]): CallbackTo[A] =
        fin.async.toCallback >> fa

      override def monotonic: CallbackTo[FiniteDuration] =
        CallbackTo(System.nanoTime().nanos)

      override def realTime: CallbackTo[FiniteDuration] =
        CallbackTo(System.currentTimeMillis().millis)

      override def suspend[A](hint: kernel.Sync.Type)(thunk: => A): CallbackTo[A] =
        CallbackTo(thunk)
    }

  // ===================================================================================================================
  // Async, concrete

  implicit lazy val asyncCallbackToIO: AsyncCallbackToIO =
    new AsyncCallbackToIO(_.async.toCallback.runNow())

  implicit lazy val ioToAsyncCallback: IOToAsyncCallback =
    new IOToAsyncCallback(IORuntime.global)

  implicit lazy val asyncAsyncCallback: AsyncAsyncCallback =
    new AsyncAsyncCallback(asyncCallbackToIO, ioToAsyncCallback)

  // ===================================================================================================================
  // Async, abstract

  class AsyncCallbackToIO(dispatch: Callback => Unit) extends (AsyncCallback ~> IO) {
    override def apply[A](f: AsyncCallback[A]): IO[A] =
      IO.async[A](k => IO {
        val s = new AsyncCallback.State
        val g = f.underlyingRepr(s)
        val d = g(t => Callback(k(t.toEither)))
        dispatch(d)
        s.cancelCallback.map(x => IO.delay(x.runNow()))
      })
  }

  class IOToAsyncCallback(r: IORuntime) extends (IO ~> AsyncCallback) { self =>
    override def apply[A](f: IO[A]): AsyncCallback[A] =
      AsyncCallback[A] { k =>
        Callback {
          f.unsafeRunAsync { t =>
            k(t.toTry).runNow()
          }(r)
        }
      }

    def transFiber[E, A](f: Fiber[IO, E, A]): Fiber[AsyncCallback, E, A] =
      new Fiber[AsyncCallback, E, A] {
        override def cancel = apply(f.cancel)
        override def join   = apply(f.join.map(_.mapK(self)))
      }
  }

  class AsyncAsyncCallback(io: AsyncCallback ~> IO, ac: IOToAsyncCallback) extends Async[AsyncCallback] {

    @inline private implicit def autoIoToAsyncCallback[A](f: IO[A]): AsyncCallback[A] =
      ac(f)

    override def unit: AsyncCallback[Unit] =
      AsyncCallback.unit

    override def never[A] =
      AsyncCallback.never

    override def pure[A](x: A): AsyncCallback[A] =
      AsyncCallback.pure(x)

    override def raiseError[A](e: Throwable): AsyncCallback[A] =
      AsyncCallback.throwException(e)

    override def handleErrorWith[A](fa: AsyncCallback[A])(f: Throwable => AsyncCallback[A]): AsyncCallback[A] =
      fa.handleError(f)

    override def flatMap[A, B](fa: AsyncCallback[A])(f: A => AsyncCallback[B]): AsyncCallback[B] =
      fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => AsyncCallback[Either[A,B]]): AsyncCallback[B] =
      AsyncCallback.tailrec(a)(f)

    override def forceR[A, B](fa: AsyncCallback[A])(fb: AsyncCallback[B]): AsyncCallback[B] =
      Async[IO].forceR(io(fa))(io(fb))

    override def uncancelable[A](body: Poll[AsyncCallback] => AsyncCallback[A]): AsyncCallback[A] =
      Async[IO].uncancelable { pollIO =>
        val poll = new Poll[AsyncCallback] {
          override def apply[A](fa: AsyncCallback[A]): AsyncCallback[A] =
            pollIO(io(fa))
        }
        io(body(poll))
      }

    override def canceled: AsyncCallback[Unit] =
      AsyncCallback.cancel

    override def onCancel[A](fa: AsyncCallback[A], fu: AsyncCallback[Unit]): AsyncCallback[A] =
      fa.onCancel(fu)
    override def monotonic: AsyncCallback[FiniteDuration] =
      AsyncCallback.delay(System.nanoTime().nanos)

    override def realTime: AsyncCallback[FiniteDuration] =
      AsyncCallback.delay(System.currentTimeMillis().millis)

    override def suspend[A](hint: kernel.Sync.Type)(thunk: => A): AsyncCallback[A] =
      AsyncCallback.delay(thunk)

    override def start[A](fa: AsyncCallback[A]): AsyncCallback[Fiber[AsyncCallback,Throwable,A]] =
      io(fa).start.map(ac.transFiber(_))

    override def cede: AsyncCallback[Unit] =
      IO.cede

    override def ref[A](a: A): AsyncCallback[Ref[AsyncCallback,A]] =
      IO.ref(a).map(_.mapK(ac))

    override def deferred[A]: AsyncCallback[Deferred[AsyncCallback,A]] =
      IO.deferred[A].map(_.mapK(ac))

    override def sleep(time: FiniteDuration): AsyncCallback[Unit] =
      AsyncCallback.unit.delay(time)

    override def evalOn[A](fa: AsyncCallback[A], ec: ExecutionContext): AsyncCallback[A] =
      io(fa).evalOn(ec)

    override def executionContext: AsyncCallback[ExecutionContext] =
      IO.executionContext

    override def cont[K, R](body: Cont[AsyncCallback,K,R]): AsyncCallback[R] =
      Async.defaultCont(body)(this)
  }
}
