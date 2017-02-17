package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.internal.Effect

/**
  * Type-classes for abstracting over things that have state.
  */
object StateAccess extends StateAccessLowPriorityImplicits {

  trait Write[-I, S] {
    val setState: I => S => Callback
    val setStateCB: I => (S, Callback) => Callback

    val modState: I => (S => S) => Callback
    val modStateCB: I => ((S => S), Callback) => Callback
  }

  trait ReadWriteF[F[_], -I, S] extends Write[I, S] {
    val state: I => F[S]
  }

  type ReadWrite[-I, S] = ReadWriteF[Effect.Id, I, S]
  type ReadWriteCB[-I, S] = ReadWriteF[CallbackTo, I, S]

  // ===================================================================================================================

  def Write[I, S](_setState: I => S => Callback,
                  _setStateCB: I => (S, Callback) => Callback,
                  _modState: I => (S => S) => Callback,
                  _modStateCB: I => ((S => S), Callback) => Callback): Write[I, S] =
    new Write[I, S] {
      override val setState = _setState
      override val modState = _modState
      override val setStateCB = _setStateCB
      override val modStateCB = _modStateCB
    }

  def ReadWriteF[F[_], I, S](_state: I => F[S],
                             _setState: I => S => Callback,
                             _setStateCB: I => (S, Callback) => Callback,
                             _modState: I => (S => S) => Callback,
                             _modStateCB: I => ((S => S), Callback) => Callback): ReadWriteF[F, I, S] =
    new ReadWriteF[F, I, S] {
      override val state = _state
      override val setState = _setState
      override val modState = _modState
      override val setStateCB = _setStateCB
      override val modStateCB = _modStateCB
    }

  // ===================================================================================================================

  private def castRW[F[_], I, S](w: ReadWriteF[F, _, _]) = w.asInstanceOf[ReadWriteF[F, I, S]]

  private[this] val _mountedId = ReadWriteF[Effect.Id, GenericComponent.BaseMounted[Effect.Id, _, X, _, _], X](
    _.state,
    i => s => Callback(i.setState(s)),
    i => (s, cb) => Callback(i.setState(s, cb)),
    i => f => Callback(i.modState(f)),
    i => (f, cb) => Callback(i.modState(f, cb)))
  implicit def mountedId[S]: ReadWrite[GenericComponent.BaseMounted[CallbackTo, _, S, _, _], S] =
    castRW(_mountedId)

  private[this] val _mountedCB = ReadWriteF[CallbackTo, GenericComponent.BaseMounted[CallbackTo, _, X, _, _], X](
    _.state, i => i.setState(_), i => i.setState(_, _), i => i.modState(_), i => i.modState(_, _))
  implicit def mountedCB[S]: ReadWriteCB[GenericComponent.BaseMounted[CallbackTo, _, S, _, _], S] =
    castRW(_mountedCB)

  private[this] val _scalaLifecycleRW = ReadWriteF[Effect.Id, ScalaComponent.Lifecycle.StateRW[_, X, _], X](
    _.state, i => i.setState(_), i => i.setState(_, _), i => i.modState(_), i => i.modState(_, _))
  implicit def scalaLifecycleRW[S]: ReadWrite[ScalaComponent.Lifecycle.StateRW[_, S, _], S] =
    castRW(_scalaLifecycleRW)
}

sealed trait StateAccessLowPriorityImplicits {
  import StateAccess.Write

  protected sealed trait X
  private def castW[I, S](w: Write[_, _]) = w.asInstanceOf[Write[I, S]]

  private[this] val _scalaLifecycleW = Write[ScalaComponent.Lifecycle.StateW[_, X, _], X](
    i => i.setState(_), i => i.setState(_, _), i => i.modState(_), i => i.modState(_, _))
  implicit def scalaLifecycleW[S]: Write[ScalaComponent.Lifecycle.StateW[_, S, _], S] =
    castW(_scalaLifecycleW)
}