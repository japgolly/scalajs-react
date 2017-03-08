package japgolly.scalajs.react.internal

import japgolly.scalajs.react.{Callback, CallbackTo}

final case class Semigroup[A](append: (A, => A) => A) extends AnyVal

object Semigroup {
//  implicit val unit     = Semigroup[Unit    ]((_, b) => b)

  implicit val callback = Semigroup[Callback](_ >> _)

  val eitherCB = Semigroup[CallbackTo[Boolean]](_ || _)
}
