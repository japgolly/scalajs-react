package japgolly.scalajs

import japgolly.scalajs.react.util.DefaultEffects._
import japgolly.scalajs.react.util.Effect.Id
import scala.scalajs.js

package object react extends ReactEventTypes with ReactExtensions {

  type Key = facade.React.Key

  type StateAccessPure[S] = StateAccess[Sync, Async, S]
  type StateAccessImpure[S] = StateAccess[Id, Async, S]

  type SetStateFnPure[S] = SetStateFn[Sync, Async, S]
  type SetStateFnImpure[S] = SetStateFn[Id, Async, S]

  type ModStateFnPure[S] = ModStateFn[Sync, Async, S]
  type ModStateFnImpure[S] = ModStateFn[Id, Async, S]

  type ModStateWithPropsFnPure[P, S] = ModStateWithPropsFn[Sync, Async, P, S]
  type ModStateWithPropsFnImpure[P, S] = ModStateWithPropsFn[Id, Async, P, S]

  val GenericComponent = component.Generic
  type GenericComponent[P, CT[-p, +u] <: CtorType[p, u], U] = GenericComponent.ComponentSimple[P, CT, U]

  val JsComponent = component.Js
  type JsComponent[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u]] = JsComponent.Component[P, S, CT]
  type JsComponentWithFacade[P <: js.Object, S <: js.Object, F <: js.Object, CT[-p, +u] <: CtorType[p, u]] = JsComponent.ComponentWithFacade[P, S, F, CT]

  val JsFnComponent = component.JsFn
  type JsFnComponent[P <: js.Object, CT[-p, +u] <: CtorType[p, u]] = JsFnComponent.Component[P, CT]

  val JsForwardRefComponent = component.JsForwardRef
  type JsForwardRefComponent[P <: js.Object, R, CT[-p, +u] <: CtorType[p, u]] = JsForwardRefComponent.Component[P, R, CT]

  type ScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]] = component.Scala.Component[P, S, B, CT]
  type BackendScope[P, S] = component.Scala.BackendScope[P, S]

  val ScalaFnComponent = component.ScalaFn
  type ScalaFnComponent[P, CT[-p, +u] <: CtorType[p, u]] = ScalaFnComponent.Component[P, CT]

  val ScalaForwardRefComponent = component.ScalaForwardRef
  type ScalaForwardRefComponent[P, R, CT[-p, +u] <: CtorType[p, u]] = ScalaForwardRefComponent.Component[P, R, CT]

  // type CustomHook[I, O] = hooks.CustomHook[I, O]
  // val CustomHook        = hooks.CustomHook
  // val Hooks             = hooks.Hooks
  // val HooksApi          = hooks.Api

  type ~=>[-A, +B] = Reusable[A => B]

  lazy val preventDefault: ReactEvent => Sync[Unit] =
    _.preventDefaultCB

  lazy val stopPropagation: ReactEvent => Sync[Unit] =
    _.stopPropagationCB

  lazy val preventDefaultAndStopPropagation: ReactEvent => Sync[Unit] =
    e => sync.delay {
      e.preventDefault()
      e.stopPropagation()
    }
}
