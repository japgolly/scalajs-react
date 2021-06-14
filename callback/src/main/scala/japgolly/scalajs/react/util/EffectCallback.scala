package japgolly.scalajs.react.util

import japgolly.scalajs.react.callback._
import scala.scalajs.js

trait EffectCallback {
  import Effect._

  implicit object callback extends Sync[CallbackTo] {

    override val empty =
      Callback.empty

    override implicit val semigroupSyncUnit: Semigroup[Callback] =
      Semigroup(_ >> _)

    override val semigroupSyncOr: Semigroup[CallbackTo[Boolean]] =
      Semigroup(_ || _)

    @inline override def isEmpty(f: Callback) =
      f.isEmpty_?

    @inline override def reset[A](fa: CallbackTo[A]): Callback =
      fa.reset

    @inline override def runAll(callbacks: CallbackTo[_]*): Callback =
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

    @inline override def finallyRun[A, B](fa: => CallbackTo[A], fb: => CallbackTo[B]) =
      fa.finallyRun(fb)

    @inline override def toJsFn0[A](f: => CallbackTo[A]): js.Function0[A] =
      f.toJsFn

    override def toJsFn1[A, Z](f: A => CallbackTo[Z]): js.Function1[A, Z] =
      a => f(a).runNow()

    @inline override def suspend[A](fa: => CallbackTo[A]): CallbackTo[A] =
      CallbackTo.suspend(fa)

    override def option_[O[_], A](f: => O[CallbackTo[A]])(implicit O: OptionLike[O]) =
      Callback(O.foreach(f)(_.runNow()))

    @inline override def fromJsFn0[A](f: js.Function0[A]) =
      CallbackTo.fromJsFn(f)

    @inline override def traverse_[A, B](as: Iterable[A])(f: A => CallbackTo[B]) =
      Callback.traverse(as)(f(_).void)

    @inline override def traverseList[A, B](as: List[A])(f: A => CallbackTo[B]) =
      CallbackTo.traverse(as)(f)

    @inline override def sequenceList[A](fas: List[CallbackTo[A]]) =
      CallbackTo.sequence(fas)

    @inline override def handleError[A, AA >: A](fa: CallbackTo[A])(f: Throwable => CallbackTo[AA]) =
      fa.handleError(f)

    @inline override def sequence_[A](fas: Iterable[CallbackTo[A]]) =
      Callback.sequence(fas)

    @inline override def when_[A](cond: Boolean)(fa: => CallbackTo[A]) =
      Callback.when(cond)(fa)
  }

  // ===================================================================================================================

  implicit object asyncCallback extends Async[AsyncCallback] {
    override def async[A](fa: Async.Untyped[A]): AsyncCallback[A] =
      AsyncCallback[A](f => CallbackTo(fa(f(_).toJsFn)))

    override def runAsync[A](fa: => AsyncCallback[A]): Async.Untyped[A] =
      f => fa.completeWith(t => CallbackTo(f(t))).toJsFn

    override def first[A](f: Async.Untyped[A]) =
      AsyncCallback.first[A](g => Callback.fromJsFn(f(g.andThen(_.toJsFn))))

    override def toJsPromise[A](fa: => AsyncCallback[A]): () => js.Promise[A] =
      () => fa.unsafeToJsPromise()
  }
}
