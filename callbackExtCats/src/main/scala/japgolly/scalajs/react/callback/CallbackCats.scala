package japgolly.scalajs.react.callback

import cats._

object CallbackCats {

  implicit lazy val callbackCatsMonadCallback: MonadError[CallbackTo, Throwable] =
    new MonadError[CallbackTo, Throwable] {

      override def pure[A](x: A): CallbackTo[A] =
        CallbackTo.pure(x)

      override def map[A, B](fa: CallbackTo[A])(f: A => B): CallbackTo[B] =
        fa.map(f)

      override def flatMap[A, B](fa: CallbackTo[A])(f: A => CallbackTo[B]): CallbackTo[B] =
        fa.flatMap(f)

      override def tailRecM[A, B](a: A)(f: A => CallbackTo[Either[A, B]]): CallbackTo[B] =
        CallbackTo.tailrec(a)(f)

      override def raiseError[A](e: Throwable): CallbackTo[A] =
        CallbackTo.throwException(e)

      override def handleErrorWith[A](fa: CallbackTo[A])(f: Throwable => CallbackTo[A]): CallbackTo[A] =
        fa.handleError(f)
    }

  implicit lazy val callbackCatsMonadAsyncCallback: MonadError[AsyncCallback, Throwable] =
    new MonadError[AsyncCallback, Throwable] {

      override def pure[A](x: A): AsyncCallback[A] =
        AsyncCallback.pure(x)

      override def ap[A, B](ff: AsyncCallback[A => B])(fa: AsyncCallback[A]) =
        ff.zipWith(fa)(_(_))

      override def ap2[A, B, Z](ff: AsyncCallback[(A, B) => Z])(fa: AsyncCallback[A], fb: AsyncCallback[B]) =
        ff.zipWith(fa.zip(fb))(_.tupled(_))

      override def map2[A, B, Z](fa: AsyncCallback[A], fb: AsyncCallback[B])(f: (A, B) => Z) =
        fa.zipWith(fb)(f)

      override def map[A, B](fa: AsyncCallback[A])(f: A => B): AsyncCallback[B] =
        fa.map(f)

      override def flatMap[A, B](fa: AsyncCallback[A])(f: A => AsyncCallback[B]): AsyncCallback[B] =
        fa.flatMap(f)

      override def tailRecM[A, B](a: A)(f: A => AsyncCallback[Either[A, B]]): AsyncCallback[B] =
        AsyncCallback.tailrec(a)(f)

      override def raiseError[A](e: Throwable): AsyncCallback[A] =
        AsyncCallback.throwException(e)

      override def handleErrorWith[A](fa: AsyncCallback[A])(f: Throwable => AsyncCallback[A]): AsyncCallback[A] =
        fa.handleError(f)
    }

  implicit lazy val callbackCatsMonadCallbackOption: MonadError[CallbackOption, Throwable] =
    new MonadError[CallbackOption, Throwable] {

      override def pure[A](x: A): CallbackOption[A] =
        CallbackOption.pure(x)

      override def map[A, B](fa: CallbackOption[A])(f: A => B): CallbackOption[B] =
        fa.map(f)

      override def flatMap[A, B](fa: CallbackOption[A])(f: A => CallbackOption[B]): CallbackOption[B] =
        fa.flatMap(f)

      override def tailRecM[A, B](a: A)(f: A => CallbackOption[Either[A, B]]): CallbackOption[B] =
        CallbackOption.tailrec(a)(f)

      override def raiseError[A](e: Throwable): CallbackOption[A] =
        CallbackOption.delay(throw e)

      override def handleErrorWith[A](fa: CallbackOption[A])(f: Throwable => CallbackOption[A]): CallbackOption[A] =
        fa.handleError(f)
    }
}
