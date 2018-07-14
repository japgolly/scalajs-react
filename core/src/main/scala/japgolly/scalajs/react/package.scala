package japgolly.scalajs

import org.scalajs.dom
import org.scalajs.dom.html
import scala.scalajs.js
import japgolly.scalajs.react.internal.Effect.Id

package object react extends ReactEventTypes {

  type Key = raw.React.Key

  type Callback = CallbackTo[Unit]

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

  val ScalaComponent = component.Scala
  type ScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]] = ScalaComponent.Component[P, S, B, CT]
  type BackendScope[P, S] = ScalaComponent.BackendScope[P, S]

  val ScalaFnComponent = component.ScalaFn
  type ScalaFnComponent[P, CT[-p, +u] <: CtorType[p, u]] = ScalaFnComponent.Component[P, CT]

  /** Extensions to plain old DOM. */
  @inline implicit final class ReactExt_DomNode(private val n: dom.raw.Node) extends AnyVal {

    @inline def domCast[N <: dom.raw.Node]: N =
      n.asInstanceOf[N]

    @inline def domAsHtml: html.Element =
      domCast

    def domToHtml: Option[html.Element] =
      n match {
        case e: html.Element => Some(e)
        case _               => None
      }
  }

  @inline implicit final class ReactExt_OptionCallback(private val o: Option[Callback]) extends AnyVal {
    /** Convenience for `.getOrElse(Callback.empty)` */
    @inline def getOrEmpty: Callback =
       o.getOrElse(Callback.empty)
  }
}
