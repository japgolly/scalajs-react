package japgolly.scalajs

import scala.scalajs.js
import scala.scalajs.js.|

package object react extends ReactEventTypes {

  type Callback = CallbackTo[Unit]

  // Same as raw.Key except its non-null
  type Key = String | Boolean | raw.JsNumber

  type Ref = String // TODO Ummm.....


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

  val ScalaFnComponent = component.ScalaFn
  type ScalaFnComponent [P, CT[-p, +u] <: CtorType[p, u]] = ScalaFnComponent.Component[P, CT]
}
