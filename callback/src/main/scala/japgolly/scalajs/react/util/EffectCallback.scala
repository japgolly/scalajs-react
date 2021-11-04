package japgolly.scalajs.react.util

import japgolly.scalajs.react.callback._
import scala.scalajs.js

abstract class EffectFallbacks1 extends EffectFallbacks2 {
  implicit def callback      : Effect.Sync    [CallbackTo    ] = EffectCallback.callback
  implicit def callbackOption: Effect.Dispatch[CallbackOption] = EffectCallback.callbackOption
  implicit def asyncCallback : Effect.Async   [AsyncCallback ] = EffectCallback.asyncCallback
}

object EffectCallback {
  import Effect._

  object callback extends Sync.WithDefaultDispatch[CallbackTo] {
    override val empty =
      Callback.empty

    override implicit val semigroupSyncUnit: Semigroup[Callback] =
      Semigroup(_ >> _)

    override val semigroupSyncOr: Semigroup[CallbackTo[Boolean]] =
      Semigroup(_ || _)

    @inline override def isEmpty[A](f: CallbackTo[A]) =
      f.isEmpty_?

    @inline override def reset[A](fa: CallbackTo[A]): Callback =
      fa.reset

    @inline override def runAll[A](callbacks: CallbackTo[A]*): Callback =
      Callback.runAll(callbacks: _*)

    @inline override def delay[A](a: => A) =
      CallbackTo(a)

    @inline override def pure[A](a: A) =
      CallbackTo.pure(a)

    @inline override def map[A, B](fa: CallbackTo[A])(f: A => B) =
      fa.map(f)

    @inline override def flatMap[A, B](fa: CallbackTo[A])(f: A => CallbackTo[B]) =
      fa.flatMap(f)

    @inline override def chain[A, B](fa: CallbackTo[A], fb: CallbackTo[B]) =
      fa >> fb

    @inline override def runSync[A](f: => CallbackTo[A]) =
      f.runNow()

    @inline override def finallyRun[A, B](fa: => CallbackTo[A], runFinally: => CallbackTo[B]) =
      fa.finallyRun(runFinally)

    @inline override def toJsFn[A](f: => CallbackTo[A]): js.Function0[A] =
      f.toJsFn

    @inline override def suspend[A](fa: => CallbackTo[A]): CallbackTo[A] =
      CallbackTo.suspend(fa)

    @inline override def fromJsFn0[A](f: js.Function0[A]) =
      CallbackTo.fromJsFn(f)

    @inline override def traverse_[A, B](as: => Iterable[A])(f: A => CallbackTo[B]) =
      Callback.traverse(as)(f(_).void)

    @inline override def traverseList[A, B](as: => List[A])(f: A => CallbackTo[B]) =
      CallbackTo.traverse(as)(f)

    @inline override def sequenceList[A](fas: => List[CallbackTo[A]]) =
      CallbackTo.sequence(fas)

    @inline override def handleError[A, AA >: A](fa: => CallbackTo[A])(f: Throwable => CallbackTo[AA]) =
      fa.handleError(f)

    @inline override def sequence_[A](fas: => Iterable[CallbackTo[A]]) =
      Callback.sequence(fas)

    @inline override def when_[A](cond: => Boolean)(fa: => CallbackTo[A]) =
      fa.when_(cond)

    @inline override def tailrec[A, B](a: A)(f: A => CallbackTo[Either[A,B]]): CallbackTo[B] =
      CallbackTo.tailrec(a)(f)
  }

  // ===================================================================================================================

  object callbackOption extends Dispatch[CallbackOption] {

    @inline override def delay[A](a: => A): CallbackOption[A] =
      CallbackOption.delay(a)

    @inline override def pure[A](a: A): CallbackOption[A] =
      CallbackOption.pure(a)

    @inline override def map[A, B](fa: CallbackOption[A])(f: A => B): CallbackOption[B] =
      fa map f

    @inline override def flatMap[A, B](fa: CallbackOption[A])(f: A => CallbackOption[B]): CallbackOption[B] =
      fa flatMap f

    @inline override def tailrec[A, B](a: A)(f: A => CallbackOption[Either[A,B]]) =
      CallbackOption.tailrec(a)(f)

    override def handleError[A, AA >: A](fa: => CallbackOption[A])(f: Throwable => CallbackOption[AA]) =
      fa.handleError(f)

    @inline override def finallyRun[A, B](fa: => CallbackOption[A], runFinally: => CallbackOption[B]) =
      fa.finallyRun(runFinally)

    override def dispatch[A](fa: CallbackOption[A]): Unit =
      fa.asCallback.void

    override def dispatchFn[A](fa: => CallbackOption[A]): js.Function0[Unit] =
      () => {fa.underlyingRepr(); ()}

    @inline override def suspend[A](fa: => CallbackOption[A]) =
      CallbackOption.suspend(fa)
  }

  // ===================================================================================================================

  object asyncCallback extends Async[AsyncCallback] with Dispatch.WithDefaults[AsyncCallback] {

    @inline override def delay[A](a: => A) =
      AsyncCallback.delay(a)

    @inline override def pure[A](a: A) =
      AsyncCallback.pure(a)

    @inline override def map[A, B](fa: AsyncCallback[A])(f: A => B) =
      fa.map(f)

    @inline override def flatMap[A, B](fa: AsyncCallback[A])(f: A => AsyncCallback[B]) =
      fa.flatMap(f)

    @inline override def finallyRun[A, B](fa: => AsyncCallback[A], runFinally: => AsyncCallback[B]) =
      fa.finallyRun(runFinally)

    override def tailrec[A, B](a: A)(f: A => AsyncCallback[Either[A,B]]): AsyncCallback[B] =
      AsyncCallback.tailrec(a)(f)

    override def async[A](fa: Async.Untyped[A]): AsyncCallback[A] =
      AsyncCallback[A](f => CallbackTo.fromJsFn(fa(f(_).toJsFn)))

    override def async_(onCompletion: Sync.Untyped[Unit] => Sync.Untyped[Unit]): AsyncCallback[Unit] =
      AsyncCallback.viaCallback(f => Callback.fromJsFn(onCompletion(f.toJsFn)))

    override def runAsync[A](fa: => AsyncCallback[A]): Async.Untyped[A] =
      f => fa.completeWith(t => CallbackTo.fromJsFn(f(t))).toJsFn

    override def first[A](f: Async.Untyped[A]) =
      AsyncCallback.first[A](g => Callback.fromJsFn(f(g.andThen(_.toJsFn))))

    override def toJsPromise[A](fa: => AsyncCallback[A]): () => js.Promise[A] =
      () => fa.unsafeToJsPromise()

    @inline override def fromJsPromise[A](pa: => js.Thenable[A]) =
      AsyncCallback.fromJsPromise(pa)

    @inline override def dispatch[A](fa: AsyncCallback[A]): Unit =
      fa.runNow()

    @inline override def handleError[A, AA >: A](fa: => AsyncCallback[A])(f: Throwable => AsyncCallback[AA]) =
      fa.handleError(f)

    @inline override def suspend[A](fa: => AsyncCallback[A]) =
      AsyncCallback.suspend(fa)
  }
}
