package japgolly.scalajs

import scala.scalajs.js
import scala.scalajs.js.|
import japgolly.scalajs.react.internal.Effect

package object react extends ReactEventTypes {

  // Same as raw.Key except it's non-null
  type Key = String | Boolean | raw.JsNumber

  type Callback = CallbackTo[Unit]

  type StateAccessPure[S] = StateAccess[CallbackTo, S]
  type StateAccessImpure[S] = StateAccess[Effect.Id, S]

  val GenericComponent = component.Generic
  type GenericComponent[P, CT[-p, +u] <: CtorType[p, u], U] = GenericComponent.Component[P, CT, U]

  val JsComponent = component.Js
  type JsComponent[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u]] = JsComponent.Component[P, S, CT]
  type JsComponentPlusFacade[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u], R <: js.Object] = JsComponent.ComponentPlusFacade[P, S, CT, R]

  val JsFnComponent = component.JsFn
  type JsFnComponent[P <: js.Object, CT[-p, +u] <: CtorType[p, u]] = JsFnComponent.Component[P, CT]

  val ScalaComponent = component.Scala
  type ScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]] = ScalaComponent.Component[P, S, B, CT]
  type BackendScope[P, S] = ScalaComponent.BackendScope[P, S]
  type ScalaComponentConfig[P, C <: Children, S, B] = component.ScalaBuilder.Config[P, C, S, B]

  val ScalaFnComponent = component.ScalaFn
  type ScalaFnComponent[P, CT[-p, +u] <: CtorType[p, u]] = ScalaFnComponent.Component[P, CT]
}
