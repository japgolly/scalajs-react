package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.util.DefaultEffects.Sync

final case class StaticOrDynamic[A](value: Either[Sync[A], A]) {
  def merge: Sync[A] =
    value match {
      case Right(a) => Sync.pure(a)
      case Left(c)  => c
    }
}

object StaticOrDynamic {

  def partition[A](xs: List[StaticOrDynamic[A]]): (List[A], List[Sync[A]]) = {
    var statics = List.empty[A]
    var dynamics = List.empty[Sync[A]]
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

    @inline def dynamic[A](a: Sync[A]): StaticOrDynamic[A] =
      StaticOrDynamic(Left(a))
  }
}
