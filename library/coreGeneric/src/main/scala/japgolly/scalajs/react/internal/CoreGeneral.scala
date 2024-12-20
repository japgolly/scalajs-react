package japgolly.scalajs.react.internal

import japgolly.scalajs.react._
import japgolly.scalajs.react.util.Effect.Id
import japgolly.scalajs.react.util.{DomUtil, Effect}
import scala.scalajs.js

object CoreGeneral extends CoreGeneral

trait CoreGeneral
    extends ReactEventTypes
       with ReactExtensions
       with DomUtil
       with FacadeExports 
       with hooks.all {

  import japgolly.scalajs.react.util.DefaultEffects._

  final type StateAccessPure[S] = StateAccess[Sync, Async, S]
  final type StateAccessImpure[S] = StateAccess[Id, Async, S]

  final type SetStateFnPure[S] = SetStateFn[Sync, Async, S]
  final type SetStateFnImpure[S] = SetStateFn[Id, Async, S]

  final type ModStateFnPure[S] = ModStateFn[Sync, Async, S]
  final type ModStateFnImpure[S] = ModStateFn[Id, Async, S]

  final type ModStateWithPropsFnPure[P, S] = ModStateWithPropsFn[Sync, Async, P, S]
  final type ModStateWithPropsFnImpure[P, S] = ModStateWithPropsFn[Id, Async, P, S]

  final val GenericComponent = component.Generic
  final type GenericComponent[P, CT[-p, +u] <: CtorType[p, u], U] = GenericComponent.ComponentSimple[P, CT, U]

  final val JsComponent = component.Js
  final type JsComponent[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u]] = JsComponent.Component[P, S, CT]
  final type JsComponentWithFacade[P <: js.Object, S <: js.Object, F <: js.Object, CT[-p, +u] <: CtorType[p, u]] = JsComponent.ComponentWithFacade[P, S, F, CT]

  final val JsFnComponent = component.JsFn
  final type JsFnComponent[P <: js.Object, CT[-p, +u] <: CtorType[p, u]] = JsFnComponent.Component[P, CT]

  final val JsForwardRefComponent = component.JsForwardRef
  final type JsForwardRefComponent[P <: js.Object, R, CT[-p, +u] <: CtorType[p, u]] = JsForwardRefComponent.Component[P, R, CT]

  final type ScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]] = component.Scala.Component[P, S, B, CT]
  final type BackendScope[P, S] = component.Scala.BackendScope[P, S]

  final val ScalaFnComponent = component.ScalaFn
  final type ScalaFnComponent[P, CT[-p, +u] <: CtorType[p, u]] = ScalaFnComponent.Component[P, CT]

  final val ScalaForwardRefComponent = component.ScalaForwardRef
  final type ScalaForwardRefComponent[P, R, CT[-p, +u] <: CtorType[p, u]] = ScalaForwardRefComponent.Component[P, R, CT]

  final type CustomHook[I, O] = hooks.CustomHook[I, O]
  final val CustomHook        = hooks.CustomHook
  final val Hooks             = hooks.Hooks
  final val HooksApi          = hooks.Api

  final type HookResult[+A] = hooks.HookResult[A]
  final val HookResult      = hooks.HookResult

  final val ReactEffect = Effect

  final type ~=>[-A, +B] = Reusable[A => B]
}

abstract class CoreGeneralF[F[_]](implicit F: Effect.Sync[F]) extends CoreGeneral {

  final lazy val preventDefault: ReactEvent => F[Unit] =
    e => F.delay { e.preventDefault() }

  final lazy val stopPropagation: ReactEvent => F[Unit] =
    e => F.delay { e.stopPropagation() }

  final lazy val preventDefaultAndStopPropagation: ReactEvent => F[Unit] =
    e => F.delay {
      e.preventDefault()
      e.stopPropagation()
    }
}
