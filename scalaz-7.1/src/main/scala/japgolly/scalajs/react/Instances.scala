package japgolly.scalajs.react

import japgolly.scalajs.react.vdom.Optional
import scalaz.{Optional => _, _}
import scalaz.Isomorphism.<~>
import scalaz.effect.IO
import Scalaz.Id

trait ScalazReactInstances {

  implicit val callbackScalazMonad: Monad[CallbackTo] =
    new Monad[CallbackTo] {
      override def point[A](a: => A): CallbackTo[A] =
        CallbackTo(a)
      override def bind[A, B](fa: CallbackTo[A])(f: A => CallbackTo[B]): CallbackTo[B] =
        fa >>= f
      override def map[A, B](fa : CallbackTo[A])(f : A => B): CallbackTo[B] =
        fa map f
    }

  implicit val maybeInstance: Optional[Maybe] = new Optional[Maybe] {
    @inline final override def foreach[A](m: Maybe[A])(f: A => Unit): Unit = m.cata(f, ())
    @inline final override def fold[A, B](t: Maybe[A], b: => B)(f: A => B): B = t.cata(f, b)
    @inline final override def isEmpty[A](t: Maybe[A]): Boolean = t.isEmpty
  }

  implicit val callbackToItself: (CallbackTo ~> CallbackTo) =
    new (CallbackTo ~> CallbackTo) { override def apply[A](a: CallbackTo[A]) = a }

  implicit val scalazIdToCallback: (Id ~> CallbackTo) =
    new (Id ~> CallbackTo) { override def apply[A](a: Id[A]) = CallbackTo pure a }

  implicit val scalazIoToCallback: (IO ~> CallbackTo) =
    new (IO ~> CallbackTo) { override def apply[A](a: IO[A]) = CallbackTo(a.unsafePerformIO()) }

  val scalazIoToCallbackIso: (CallbackTo <~> IO) =
    new (CallbackTo <~> IO) {
      override val from = scalazIoToCallback
      override val to: CallbackTo ~> IO =
        new (CallbackTo ~> IO) { override def apply[A](a: CallbackTo[A]): IO[A] = IO(a.runNow()) }
    }

}
