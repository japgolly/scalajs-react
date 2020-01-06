package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.CallbackTo

final case class StaticOrDynamic[A](value: Either[CallbackTo[A], A]) {
  def merge: CallbackTo[A] =
    value match {
      case Right(a) => CallbackTo.pure(a)
      case Left(c)  => c
    }
}

object StaticOrDynamic {

  def partition[A](xs: List[StaticOrDynamic[A]]): (List[A], List[CallbackTo[A]]) = {
    var statics = List.empty[A]
    var dynamics = List.empty[CallbackTo[A]]
    for (x <- xs)
      x.value match {
        case Right(a) => statics ::= a
        case Left(ca) => dynamics ::= ca
      }
    (statics, dynamics)
  }

  object Helpers {

    @inline def static[A](a: A): StaticOrDynamic[A] =
      StaticOrDynamic(Right(a))

    @inline def dynamic[A](a: CallbackTo[A]): StaticOrDynamic[A] =
      StaticOrDynamic(Left(a))
  }
}
