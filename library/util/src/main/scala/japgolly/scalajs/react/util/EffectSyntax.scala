package japgolly.scalajs.react.util

import scala.scalajs.js

trait EffectSyntax
  extends EffectSyntax.ForAsync
     with EffectSyntax.ForSync

object EffectSyntax {
  import Effect._

  trait ForMonad {
    implicit def sjrEffectMonadOps[F[_], A](fa: F[A])(implicit f: Monad[F]): MonadOps[F, A] =
      new MonadOps[F, A] {
        override protected def F = f
        override protected def self = fa
      }
  }

  trait MonadOps[F[_], A] {
    protected def F: Monad[F]
    protected def self: F[A]

    def map    [B](f: A => B)   : F[B] = F.map(self)(f)
    def flatMap[B](f: A => F[B]): F[B] = F.flatMap(self)(f)
    def >>     [B](fb: F[B])    : F[B] = F.chain(self, fb)
  }

  // ===================================================================================================================

  trait ForEffect extends ForMonad {
    implicit def sjrEffectEffectOps[F[_], A](fa: F[A])(implicit f: Effect[F]): EffectOps[F, A] =
      new EffectOps[F, A] {
        override protected def F = f
        override protected def self = fa
      }
  }

  trait EffectOps[F[_], A] extends MonadOps[F, A] {
    override protected def F: Effect[F]

    def finallyRun [B]      (runFinally: => F[B])  : F[A]  = F.finallyRun(self, runFinally)
    def handleError[AA >: A](f: Throwable => F[AA]): F[AA] = F.handleError[A, AA](self)(f)
  }

  // ===================================================================================================================

  trait ForDispatch extends ForEffect {
    implicit def sjrEffectDispatchOps[F[_], A](fa: F[A])(implicit f: Dispatch[F]): DispatchOps[F, A] =
      new DispatchOps[F, A] {
        override protected def F = f
        override protected def self = fa
      }
  }

  trait DispatchOps[F[_], A] extends EffectOps[F, A] {
    override protected def F: Dispatch[F]

    def dispatch()  : Unit               = F.dispatch(self)
    def toDispatchFn: js.Function0[Unit] = F.dispatchFn(self)
  }

  // ===================================================================================================================

  trait ForUnsafeSync extends ForDispatch {
    implicit def sjrEffectUnsafeSyncOps[F[_], A](fa: F[A])(implicit f: UnsafeSync[F]): UnsafeSyncOps[F, A] =
      new UnsafeSyncOps[F, A] {
        override protected def F = f
        override protected def self = fa
      }
  }

  trait UnsafeSyncOps[F[_], A] extends DispatchOps[F, A] {
    override protected def F: UnsafeSync[F]

    def runSync()                : A               = F.runSync(self)
    def toJsFn                   : js.Function0[A] = F.toJsFn(self)
    def unless_(cond: => Boolean): F[Unit]         = F.unless_(cond)(self)
    def when_  (cond: => Boolean): F[Unit]         = F.when_(cond)(self)
  }

  // ===================================================================================================================

  trait ForSync extends ForUnsafeSync {
    implicit def sjrEffectSyncOps[F[_], A](fa: F[A])(implicit f: Sync[F]): SyncOps[F, A] =
      new SyncOps[F, A] {
        override protected def F = f
        override protected def self = fa
      }
  }

  trait SyncOps[F[_], A] extends UnsafeSyncOps[F, A] {
    override protected def F: Sync[F]

    def isEmpty: Boolean = F.isEmpty(self)
    def reset  : F[Unit] = F.reset(self)
  }

  // ===================================================================================================================

  trait ForAsync extends ForDispatch {
    implicit def sjrEffectAsyncOps[F[_], A](fa: F[A])(implicit f: Async[F]): AsyncOps[F, A] =
      new AsyncOps[F, A] {
        override protected def F = f
        override protected def self = fa
      }
  }

  trait AsyncOps[F[_], A] extends DispatchOps[F, A] {
    override protected def F: Async[F]

    def runAsync   : Async.Untyped[A]    = F.runAsync(self)
    def toJsPromise: () => js.Promise[A] = F.toJsPromise(self)
  }

}
