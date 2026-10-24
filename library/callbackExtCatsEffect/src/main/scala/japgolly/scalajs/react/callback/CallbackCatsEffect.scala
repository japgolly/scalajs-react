package japgolly.scalajs.react.callback

import cats.effect._
import cats.effect.kernel.CancelScope
import cats.effect.unsafe.IORuntime
import cats.~>
import japgolly.scalajs.react.ReactCatsEffect
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Either, Failure, Success}

object CallbackCatsEffect {

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
        // `CallbackTo` is uncancelable (`rootCancelScope = CancelScope.Uncancelable`). In Cats Effect's `MonadCancel` /
        // `Sync` (like `SyncIO`), `onCancel(fa, fin)` must only run `fin` when `fa` is canceled, so for an uncancelable
        // effect `onCancel(fa, fin)` should simply be `fa`.
        fa

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
    new IOToAsyncCallback(ReactCatsEffect.runtimeFn)

  implicit lazy val asyncAsyncCallback: AsyncAsyncCallback =
    new AsyncAsyncCallback(asyncCallbackToIO, ioToAsyncCallback)

  // ===================================================================================================================
  // Async, abstract

  class AsyncCallbackToIO(dispatch: Callback => Unit) extends (AsyncCallback ~> IO) {
    override def apply[A](f: AsyncCallback[A]): IO[A] =
      IO.async[Option[A]](k => IO {
        val s = new AsyncCallback.State
        var finDone = false
        var finCb: Either[Throwable, Unit] => Unit = null
        s.onCancel(AsyncCallback.delay {
          finDone = true
          if (finCb ne null) finCb(Right(()))
          k(Right(None))
        })
        val g = f.underlyingRepr(s)
        val d = g(t =>
          Callback {
            if (s.shouldFinalize)
              s.performCancel()
            else
              k(t.toEither.map(Some(_)))
          }
        )
        dispatch(d)
        Some(IO.async_[Unit] { cb =>
          finCb = cb
          s.cancelled = true
          if (s.shouldFinalize)
            s.performCancel()
          if (finDone)
            cb(Right(()))
        })
      }).flatMap {
        case Some(a) => IO.pure(a)
        case None    => IO.canceled.as(null.asInstanceOf[A])
      }
  }

  class IOToAsyncCallback(runtime: () => IORuntime) extends (IO ~> AsyncCallback) { self =>
    override def apply[A](f: IO[A]): AsyncCallback[A] =
      new AsyncCallback[A](s => k => s.cancelably(Callback {
        implicit val rt: IORuntime = runtime()
        implicit val ec: ExecutionContext = rt.compute
        val f0 = if (s.ec ne null) f.evalOn(s.ec) else f
        if (s.masks == 0) {
          var done = false
          var cancelToken = -1
          val cancel = f0
            .attempt
            .flatMap(IO.pure)
            .onCancel(IO {
              done = true
              if (!s.finalized && cancelToken >= 0) s.unCancel(cancelToken)
              s.performCancel()
            })
            .flatMap {
              case Right(a) =>
                IO {
                  done = true
                  if (!s.finalized && cancelToken >= 0) s.unCancel(cancelToken)
                  s.cancelably(k(Success(a))).runNow()
                }
              case Left(e) =>
                IO {
                  done = true
                  if (!s.finalized && cancelToken >= 0) s.unCancel(cancelToken)
                  s.cancelably(k(Failure(e))).runNow()
                }
            }
            .unsafeRunCancelable()
          if (!done && !s.finalized)
            cancelToken = s.onCancel(AsyncCallback.fromFuture(cancel()))
        } else {
          var res: Either[Throwable, A] = null
          val maskedIO = IO.uncancelable(_ => f0.attempt.map { r => res = r }).flatMap(_ => IO.unit)
          maskedIO.unsafeRunAsyncOutcome {
            case Outcome.Canceled() =>
              s.cancelled = true
              if (res ne null)
                s.cancelably(k(res.toTry)).runNow()
            case Outcome.Errored(e) =>
              s.cancelably(k(Failure(e))).runNow()
            case Outcome.Succeeded(_) =>
              s.cancelably(k(res.toTry)).runNow()
          }
        }
      }))

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
      fa.attempt >> fb

    override def uncancelable[A](body: Poll[AsyncCallback] => AsyncCallback[A]): AsyncCallback[A] =
      new AsyncCallback[A](s => k => s.cancelably(Callback.suspend {
        s.masks += 1
        val id = s.masks

        val poll = new Poll[AsyncCallback] {
          override def apply[B](fb: AsyncCallback[B]): AsyncCallback[B] =
            new AsyncCallback[B](s2 => k2 => s2.cancelably(Callback.suspend {
              if ((s2 eq s) && (s2.masks == id)) {
                s2.masks -= 1
                s2.cancelably {
                  fb.underlyingRepr(s2) { t =>
                    s2.cancelably {
                      s2.masks += 1
                      k2(t)
                    }
                  }
                }
              } else
                fb.underlyingRepr(s2)(k2)
            }))
        }

        val next =
          try body(poll)
          catch {
            case t: Throwable => AsyncCallback.throwException(t)
          }

        next.underlyingRepr(s) { t =>
          Callback.suspend {
            Callback.unless(s.finalized) {
              s.masks -= 1
              k(t)
            }
          }
        }
      }))

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

    override def evalOn[A](fa: AsyncCallback[A], ec: ExecutionContext): AsyncCallback[A] = {
      val faWithEc = new AsyncCallback[A](s => k => s.cancelably(Callback.suspend {
        val prev = s.ec
        s.ec = ec
        fa.underlyingRepr(s) { t =>
          Callback.suspend {
            Callback.unless(s.finalized) {
              s.ec = prev
              k(t)
            }
          }
        }
      }))
      io(faWithEc).evalOn(ec)
    }

    override def executionContext: AsyncCallback[ExecutionContext] =
      new AsyncCallback[ExecutionContext](s => k => s.cancelably(Callback.suspend {
        if (s.ec ne null)
          k(Success(s.ec))
        else
          ac(IO.executionContext).underlyingRepr(s)(k)
      }))

    override def asyncCheckAttempt[A](
        k: (Either[Throwable, A] => Unit) => AsyncCallback[Either[Option[AsyncCallback[Unit]], A]]): AsyncCallback[A] =
      uncancelable { poll =>
        AsyncCallback.promise[A].asAsyncCallback.flatMap { case (p, complete) =>
          var done = false
          val resume: Either[Throwable, A] => Unit =
            ea =>
              if (!done) {
                done = true
                complete(ea.toTry).runNow()
              }
          k(resume).flatMap {
            case Right(a) =>
              done = true
              AsyncCallback.pure(a)
            case Left(Some(fin)) =>
              onCancel(poll(p), fin)
            case Left(None) =>
              p
          }
        }
      }

    override def cont[K, R](body: Cont[AsyncCallback,K,R]): AsyncCallback[R] =
      Async.defaultCont(body)(this)
  }

}
