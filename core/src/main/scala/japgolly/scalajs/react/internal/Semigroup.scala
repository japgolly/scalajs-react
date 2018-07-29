package japgolly.scalajs.react.internal

import japgolly.scalajs.react.{Callback, CallbackTo}

final case class Semigroup[A](append: (A, => A) => A) extends AnyVal

object Semigroup {

  implicit val callback = Semigroup[Callback](_ >> _)

  val eitherCB = Semigroup[CallbackTo[Boolean]](_ || _)

  def optionFirst[A] = Semigroup[Option[A]](_ orElse _)
}
