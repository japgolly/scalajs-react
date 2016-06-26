package japgolly.scalajs.react.internal

import japgolly.scalajs.react.Callback

case class Semigroup[A](append: (A, => A) => A) extends AnyVal

object Semigroup {
//  implicit val unit     = Semigroup[Unit    ]((_, b) => b)
  implicit val callback = Semigroup[Callback](_ >> _)
           val either   = Semigroup[Boolean ](_ || _)
}
