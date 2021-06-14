package japgolly.scalajs.react.util

import japgolly.scalajs.react.callback._
import scala.scalajs.js

trait EffectCallback {
  import Effect._

  implicit object callback extends Sync[CallbackTo] {

    override val empty =
      Callback.empty

    @inline override def delay[A](a: => A) =
      CallbackTo(a)

    @inline override def pure[A](a: A) =
      CallbackTo.pure(a)

    @inline override def map[A, B](fa: CallbackTo[A])(f: A => B) =
      fa.map(f)

    @inline override def flatMap[A, B](fa: CallbackTo[A])(f: A => CallbackTo[B]) =
      fa.flatMap(f)

    @inline override def runSync[A](f: => CallbackTo[A]) =
      f.runNow()

    override def option_[O[_], A](f: => O[CallbackTo[A]])(implicit O: OptionLike[O]) =
      Callback(O.foreach(f)(_.runNow()))

    @inline override def fromJsFn0[A](f: js.Function0[A]) =
      CallbackTo.fromJsFn(f)
  }

  // ===================================================================================================================

  implicit object asyncCallback extends Async[AsyncCallback] {
    override def async[A](fa: Async.Untyped[A]): AsyncCallback[A] =
      AsyncCallback[A](f => CallbackTo(fa(f(_).toJsFn)))

    override def runAsync[A](fa: => AsyncCallback[A]): Async.Untyped[A] =
      f => fa.completeWith(t => CallbackTo(f(t))).toJsFn

    override def toJsPromise[A](fa: => AsyncCallback[A]): () => js.Promise[A] =
      () => fa.unsafeToJsPromise()
  }
}
