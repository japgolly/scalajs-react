package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.component.{Js => JsComponent}
import japgolly.scalajs.react.hooks.Hooks.{UseRef, UseStateWithReuse}
import japgolly.scalajs.react.util.DefaultEffects
import japgolly.scalajs.react.vdom.TopNode
import japgolly.scalajs.react.{CtorType, Ref, Reusability, Reusable, ScalaComponent}
import scala.reflect.ClassTag
import scala.scalajs.js


trait extra {
  /** Provides a Callback that when invoked forces a re-render of your component. */
  @inline final val useForceUpdate: HookResult[Reusable[DefaultEffects.Sync[Unit]]] =
    CustomHook.useForceUpdate.toHookResult


  /** Create a mutable ref that will persist for the full lifetime of the component. */
  @inline final val useRefToAnyVdom: HookResult[Ref.ToAnyVdom] =
    HookResult(UseRef.unsafeCreateToAnyVdom())

  /** Create a mutable ref that will persist for the full lifetime of the component. */
  @inline final def useRefToVdom[N <: TopNode: ClassTag]: HookResult[Ref.ToVdom[N]] =
    HookResult(UseRef.unsafeCreateToVdom[N]())

  /** Create a mutable ref that will persist for the full lifetime of the component. */
  @inline final def useRefToScalaComponent[P, S, B]: HookResult[Ref.ToScalaComponent[P, S, B]] =
    HookResult(UseRef.unsafeCreateToScalaComponent[P, S, B]())

  /** Create a mutable ref that will persist for the full lifetime of the component. */
  @inline final def useRefToScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]](
    c: ScalaComponent.Component[P, S, B, CT]
  ): HookResult[Ref.WithScalaComponent[P, S, B, CT]] =
    HookResult(UseRef.unsafeCreateToScalaComponent(c))

  /** Create a mutable ref that will persist for the full lifetime of the component. */
  @inline final def useRefToJsComponent[P <: js.Object, S <: js.Object]
    : HookResult[Ref.ToJsComponent[P, S, JsComponent.RawMounted[P, S]]] =
    HookResult(UseRef.unsafeCreateToJsComponent[P, S]())

  /** Create a mutable ref that will persist for the full lifetime of the component. */
  @inline final def useRefToJsComponentWithMountedFacade[P <: js.Object, S <: js.Object, F <: js.Object]
    : HookResult[Ref.ToJsComponent[P, S, JsComponent.RawMounted[P, S] with F]] =
    HookResult(UseRef.unsafeCreateToJsComponentWithMountedFacade[P, S, F]())

  /** Create a mutable ref that will persist for the full lifetime of the component. */
  @inline final def useRefToJsComponent[F[_], A[_], P1, S1, CT1[-p, +u] <: CtorType[p,
                                                                            u
  ], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p,
                                                                                                    u
  ]](
    a: Ref.WithJsComponentArg[F, A, P1, S1, CT1, R, P0, S0]
  ): HookResult[Ref.WithJsComponent[F, A, P1, S1, CT1, R, P0, S0]] =
    HookResult(UseRef.unsafeCreateToJsComponent(a))    

  /**
  * Returns a stateful value, and a function to update it.
  *
  * During the initial render, the returned state is the same as the value passed as the first
  * argument (initialState).
  *
  * During subsequent re-renders, the first value returned by useState will always be the most recent
  * state after applying updates.
  */
  @inline final def useStateWithReuse[S: ClassTag: Reusability](
    initialState: => S
  ): HookResult[UseStateWithReuse[S]] =
    HookResult(UseStateWithReuse.unsafeCreate(initialState))


}
