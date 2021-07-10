package japgolly.scalajs.react.util

final case class Semigroup[A](append: (A, => A) => A) extends AnyVal

object Semigroup {

  def optionFirst[A]: Semigroup[Option[A]] =
    Semigroup(_ orElse _)
}
