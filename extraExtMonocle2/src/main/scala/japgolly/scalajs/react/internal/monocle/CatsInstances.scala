package japgolly.scalajs.react.internal.monocle

import cats.{Applicative, Functor}

// Not using out-of-the-box Cats instances because it brings many other typeclasses into the output JS
object CatsInstances {

  implicit lazy val functorOption: Functor[Option] =
    new Functor[Option] {
      override def map[A, B](fa: Option[A])(f: A => B) = fa.map(f)
    }

  // Not using the Cats instance because it brings many other typeclasses into the output JS
  implicit lazy val applicativeOption: Applicative[Option] =
    new Applicative[Option] {
      override def pure[A](x: A): Option[A] = Option(x)
      override def ap[A, B](ff: Option[A => B])(fa: Option[A]): Option[B] = fa.flatMap(a => ff.map(_(a)))
    }

}
