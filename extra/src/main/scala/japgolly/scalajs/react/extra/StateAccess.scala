package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.internal.Effect

/**
  * Type-classes for abstracting over things that have state.
  */
object StateAccess extends StateAccessLowPriorityImplicits {

  class Write[I, S](final val setState: I => S => Callback,
                    final val modState: I => (S => S) => Callback)

  class ReadWrite[I, S](final val state: I => S,
                        setState: I => S => Callback,
                        modState: I => (S => S) => Callback) extends Write[I, S](setState, modState)

  // ===================================================================================================================

  private def castRW[I, S](w: ReadWrite[_, _]) = w.asInstanceOf[ReadWrite[I, S]]

  private[this] val _mountedId = new ReadWrite[GenericComponent.BaseMounted[Effect.Id, _, X, _, _], X](
    _.state,
    i => s => Callback(i.setState(s)),
    i => f => Callback(i.modState(f)))
  implicit def mountedId[I <: GenericComponent.BaseMounted[CallbackTo, _, S, _, _], S]: ReadWrite[I, S] =
    castRW(_mountedId)

  private[this] val _scalaLifecycleRW = new ReadWrite[ScalaComponent.Lifecycle.StateRW[_, X, _], X](
    _.state,
    i => i.setState(_),
    i => i.modState(_))
  implicit def scalaLifecycleRW[I <: ScalaComponent.Lifecycle.StateRW[_, X, _], S]: ReadWrite[I, S] =
    castRW(_scalaLifecycleRW)
}

sealed trait StateAccessLowPriorityImplicits {
  import StateAccess.Write

  protected sealed trait X
  private def castW[I, S](w: Write[_, _]) = w.asInstanceOf[Write[I, S]]

  private[this] val _mountedCB = new Write[GenericComponent.BaseMounted[CallbackTo, _, X, _, _], X](
    i => i.setState(_),
    i => i.modState(_))
  implicit def mountedCB[I <: GenericComponent.BaseMounted[CallbackTo, _, S, _, _], S]: Write[I, S] =
    castW(_mountedCB)

  private[this] val _scalaLifecycleW = new Write[ScalaComponent.Lifecycle.StateW[_, X, _], X](
    i => i.setState(_),
    i => i.modState(_))
  implicit def scalaLifecycleW[I <: ScalaComponent.Lifecycle.StateW[_, X, _], S]: Write[I, S] =
    castW(_scalaLifecycleW)
}