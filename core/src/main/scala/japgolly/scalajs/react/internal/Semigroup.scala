package japgolly.scalajs.react.internal

import japgolly.scalajs.react.{Callback, CallbackTo}

final case class Semigroup[A](append: (A, => A) => A) extends AnyVal

object Semigroup {

  implicit val callback: Semigroup[Callback] =
    Semigroup(_ >> _)

  val eitherCB: Semigroup[CallbackTo[Boolean]] =
    Semigroup(_ || _)

  def optionFirst[A]: Semigroup[Option[A]] =
    Semigroup(_ orElse _)
}
