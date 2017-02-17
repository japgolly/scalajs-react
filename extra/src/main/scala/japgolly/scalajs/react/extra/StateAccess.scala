package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.internal.Effect

/**
  * Type-classes for abstracting over things that have state.
  */
object StateAccess extends StateAccessImplicits {

  class WriteCB[-I, S](
    final val setState: I => S => Callback,
    final val setStateCB: I => (S, Callback) => Callback,
    final val modState: I => (S => S) => Callback,
    final val modStateCB: I => ((S => S), Callback) => Callback)

  class ReadFWriteCB[F[_], -I, S](
    final val state: I => F[S],
    setState: I => S => Callback,
    setStateCB: I => (S, Callback) => Callback,
    modState: I => (S => S) => Callback,
    modStateCB: I => ((S => S), Callback) => Callback)
    extends WriteCB[I, S](setState, setStateCB, modState, modStateCB)

  type ReadIdWriteCB[-I, S] = ReadFWriteCB[Effect.Id, I, S]
  type ReadCBWriteCB[-I, S] = ReadFWriteCB[CallbackTo, I, S]
}

// =====================================================================================================================
import StateAccess.{WriteCB, ReadFWriteCB, ReadIdWriteCB, ReadCBWriteCB}

sealed trait StateAccessImplicits1 {

  protected sealed trait X
  private def castW[I, S](w: WriteCB[_, _]) = w.asInstanceOf[WriteCB[I, S]]

  // WriteCB -- ScalaComponent.Lifecycle.StateW
  private[this] val _scalaLifecycleW = new WriteCB[ScalaComponent.Lifecycle.StateW[_, X, _], X](
    i => i.setState(_), i => i.setState(_, _), i => i.modState(_), i => i.modState(_, _))
  implicit def scalaLifecycleW[S]: WriteCB[ScalaComponent.Lifecycle.StateW[_, S, _], S] =
    castW(_scalaLifecycleW)
}

sealed trait StateAccessImplicits2 extends StateAccessImplicits1 {

  protected def castRW[F[_], I, S](w: ReadFWriteCB[F, _, _]) = w.asInstanceOf[ReadFWriteCB[F, I, S]]

  // ReadCBWriteCB -- GenericComponent.BaseMounted[CallbackTo
  private[this] val _mountedCB = new ReadFWriteCB[CallbackTo, GenericComponent.BaseMounted[CallbackTo, _, X, _, _], X](
    _.state, i => i.setState(_), i => i.setState(_, _), i => i.modState(_), i => i.modState(_, _))
  implicit def mountedCB[S]: ReadCBWriteCB[GenericComponent.BaseMounted[CallbackTo, _, S, _, _], S] =
    castRW(_mountedCB)
}

sealed trait StateAccessImplicits3 extends StateAccessImplicits2 {

  // ReadCBWriteCB -- GenericComponent.BaseMounted[Id
  private[this] lazy val _mountedIdCB = new ReadFWriteCB[CallbackTo, GenericComponent.BaseMounted[Effect.Id, _, X, _, _], X](
    i => CallbackTo(i.state),
    i => s => Callback(i.setState(s)),
    i => (s, cb) => Callback(i.setState(s, cb)),
    i => f => Callback(i.modState(f)),
    i => (f, cb) => Callback(i.modState(f, cb)))
  implicit def mountedIdCB[S]: ReadCBWriteCB[GenericComponent.BaseMounted[Effect.Id, _, S, _, _], S] =
    castRW(_mountedIdCB)
}

sealed trait StateAccessImplicits extends StateAccessImplicits3 {

  // ReadIdWriteCB -- GenericComponent.BaseMounted[Id
  private[this] val _mountedId = new ReadFWriteCB[Effect.Id, GenericComponent.BaseMounted[Effect.Id, _, X, _, _], X](
    _.state,
    i => s => Callback(i.setState(s)),
    i => (s, cb) => Callback(i.setState(s, cb)),
    i => f => Callback(i.modState(f)),
    i => (f, cb) => Callback(i.modState(f, cb)))
  implicit def mountedId[S]: ReadIdWriteCB[GenericComponent.BaseMounted[Effect.Id, _, S, _, _], S] =
    castRW(_mountedId)

  // ReadIdWriteCB -- ScalaComponent.Lifecycle.StateRW
  private[this] val _scalaLifecycleRW = new ReadFWriteCB[Effect.Id, ScalaComponent.Lifecycle.StateRW[_, X, _], X](
    _.state, i => i.setState(_), i => i.setState(_, _), i => i.modState(_), i => i.modState(_, _))
  implicit def scalaLifecycleRW[S]: ReadIdWriteCB[ScalaComponent.Lifecycle.StateRW[_, S, _], S] =
    castRW(_scalaLifecycleRW)
}