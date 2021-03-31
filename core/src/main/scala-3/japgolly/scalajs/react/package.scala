package japgolly.scalajs

import japgolly.scalajs.react.internal.Effect.Id
import org.scalajs.dom
import org.scalajs.dom.html
import scala.scalajs.js

package object react extends ReactEventTypes, ReactExtensions {

  type Key = raw.React.Key

  type StateAccessPure[S] = StateAccess[CallbackTo, S]
  type StateAccessImpure[S] = StateAccess[Id, S]

  type SetStateFnPure[S] = SetStateFn[CallbackTo, S]
  type SetStateFnImpure[S] = SetStateFn[Id, S]

  type ModStateFnPure[S] = ModStateFn[CallbackTo, S]
  type ModStateFnImpure[S] = ModStateFn[Id, S]

  type ModStateWithPropsFnPure[P, S] = ModStateWithPropsFn[CallbackTo, P, S]
  type ModStateWithPropsFnImpure[P, S] = ModStateWithPropsFn[Id, P, S]

  val GenericComponent = component.Generic
  type GenericComponent[P, CT[-p, +u] <: CtorType[p, u], U] = GenericComponent.ComponentSimple[P, CT, U]

  val JsComponent = component.Js
  type JsComponent[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u]] = JsComponent.Component[P, S, CT]
  type JsComponentWithFacade[P <: js.Object, S <: js.Object, F <: js.Object, CT[-p, +u] <: CtorType[p, u]] = JsComponent.ComponentWithFacade[P, S, F, CT]

  val JsFnComponent = component.JsFn
  type JsFnComponent[P <: js.Object, CT[-p, +u] <: CtorType[p, u]] = JsFnComponent.Component[P, CT]

  val JsForwardRefComponent = component.JsForwardRef
  type JsForwardRefComponent[P <: js.Object, R, CT[-p, +u] <: CtorType[p, u]] = JsForwardRefComponent.Component[P, R, CT]

  val ScalaComponent = component.Scala
  type ScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]] = ScalaComponent.Component[P, S, B, CT]
  type BackendScope[P, S] = ScalaComponent.BackendScope[P, S]

  val ScalaFnComponent = component.ScalaFn
  type ScalaFnComponent[P, CT[-p, +u] <: CtorType[p, u]] = ScalaFnComponent.Component[P, CT]

  val ScalaForwardRefComponent = component.ScalaForwardRef
  type ScalaForwardRefComponent[P, R, CT[-p, +u] <: CtorType[p, u]] = ScalaForwardRefComponent.Component[P, R, CT]

  // Required for cross-building
  private[react] inline def scalajsReactRawPropsChildrenToJsUndef(r: raw.PropsChildren): js.UndefOr[raw.PropsChildren] =
    r.asInstanceOf[js.UndefOr[raw.PropsChildren]]

  // TODO: [3] package exports
  // type ~=>[-A, +B] = Reusable[A => B]

  val preventDefault: ReactEvent => Callback =
    _.preventDefaultCB

  val stopPropagation: ReactEvent => Callback =
    _.stopPropagationCB

  val preventDefaultAndStopPropagation: ReactEvent => Callback =
    e => e.preventDefaultCB >> e.stopPropagationCB
}
