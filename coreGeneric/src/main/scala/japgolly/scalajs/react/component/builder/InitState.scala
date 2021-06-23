package japgolly.scalajs.react.component.builder

import japgolly.scalajs.react.internal.Box

private[builder] sealed trait InitState[-P, S]

private[builder] object InitState {

  case object Stateless extends InitState[Any, Any]

  @inline def stateless[S]: InitState[Any, S] =
    Stateless.asInstanceOf[InitState[Any, S]]

  final case class InitialState[-P, S](fn: Box[P] => Box[S]) extends InitState[P, S]

  final case class DerivedFromProps[-P, S](fn: P => S) extends InitState[P, S]

  final case class DerivedFromPropsAndState[-P, S](fn: (P, Option[S]) => S) extends InitState[P, S]
}
