package japgolly.scalajs.react.extra.router

import japgolly.scalajs.react.util.Effect.Sync
import scala.scalajs.js

private[router] final case class StaticOrDynamic[A](value: Either[js.Function0[A], A]) {
  def merge: js.Function0[A] =
    value match {
      case Right(a) => () => a
      case Left(c)  => c
    }
}

private[router] object StaticOrDynamic {

  def partition[F[_], A](xs: List[StaticOrDynamic[A]])(implicit F: Sync[F]): (List[A], List[F[A]]) = {
    var statics = List.empty[A]
    var dynamics = List.empty[F[A]]
    for (x <- xs)
      x.value match {
        case Right(a) => statics ::= a
        case Left(ca) => dynamics ::= F.fromJsFn0(ca)
      }
    (statics, dynamics)
  }

  object Helpers {

    @inline def static[A](a: A): StaticOrDynamic[A] =
      StaticOrDynamic(Right(a))

    @inline def dynamic[F[_], A](fa: F[A])(implicit F: Sync[F]): StaticOrDynamic[A] =
      StaticOrDynamic(Left(F.toJsFn(fa)))
  }
}
