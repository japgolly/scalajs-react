package japgolly.scalajs

import scala.scalajs.js
import scala.scalajs.js.|

/*
Bad approaches
==============
* Building Types via conjunction - too hard to map
* JS + implicit ops - extern JS types can't be changed
* PSBN = annoying. PS usually enough.


[ ] Prevent certain lifecycle methods being called in certain scopes.
[ ] Make easy to add functionality (such as Id/CallbackTo, S zoom, P map).
[ ] All components: Id/Callback.
[ ] All components: S zoom.
[ ] All components: P map.
[ ] Typify PropsChildren.
[ ] Easily facade JS components.
[ ] Easily facade JS ES6 components.
[ ] Create ES6 components in Scala.
*/

package object react {

  type Callback = CallbackTo[Unit]

  type Key = String | Boolean | raw.JsNumber

  type Ref = String // TODO Ummm.....

  // TODO Rename?
  type BackendScope[P, S] = ScalaComponent.BackendScope[P, S]

  @inline implicit def toJsComponentToMountedOps
      [P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted]
      (c: JsComponent[P, S, CT, JsComponent.Mounted[P, S, R]]): JsComponent.CompToMountedOps[P, S, CT, R] =
    new JsComponent.CompToMountedOps(c)

  import CtorType._
  @inline implicit def toCtorOpsF[P, U](base: Component[P, PropsAndChildren, U]): OpsF[P, U] = new OpsF(base.ctor)
  @inline implicit def toCtorOpsP[P, U](base: Component[P, Props           , U]): OpsP[P, U] = new OpsP(base.ctor)
  @inline implicit def toCtorOpsC[P, U](base: Component[P, Children        , U]): OpsC[P, U] = new OpsC(base.ctor)
  @inline implicit def toCtorOpsV[P, U](base: Component[P, Void            , U]): OpsV[   U] = new OpsV(base.ctor)
}
