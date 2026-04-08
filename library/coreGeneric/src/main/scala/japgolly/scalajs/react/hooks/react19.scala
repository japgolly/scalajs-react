package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.hooks.HookResult
import japgolly.scalajs.react.hooks.Hooks._
import japgolly.scalajs.react.util.Effect.Async

trait react19 {

  // Had to move this here due to a Scala 2 overloading bug
  /** Lets you defer updating a part of the UI.
    *
    * @see
    *   {@link https://react.dev/reference/react/useDeferredValue}
    *
    * @since 3.0.0 / React 18.0.0
    */
  @inline final def useDeferredValue[A](value: A): HookResult[A] =
    UseDeferredValue(value).toHookResult

  /** Lets you defer updating a part of the UI.
    *
    * @see
    *   {@link https://react.dev/reference/react/useDeferredValue}
    *
    * @param initialValue A value to use during the initial render of a component. If this option is omitted,
    *           useDeferredValue will not defer during the initial render, because there’s no previous version of value
    *           that it can render instead.
    *
    * @since 4.0.0 / React 19
    */
  @inline final def useDeferredValue[A](value: A, initialValue: A): HookResult[A] =
    UseDeferredValue(value, initialValue).toHookResult

  /** @since 4.0.0 / React 19 */
  @inline final def useActionState[S](action: S => S, initialState: S): HookResult[UseActionState[S, Unit]] =
    UseActionState(action, initialState).toHookResult

  /** @since 4.0.0 / React 19 */
  @inline final def useActionState[S, P](action: (S, P) => S, initialState: S): HookResult[UseActionState[S, P]] =
    UseActionState(action, initialState).toHookResult

  /** @since 4.0.0 / React 19 */
  @inline final def useActionStateAsync[G[_], S](action: S => G[S], initialState: S)(implicit G: Async[G]): HookResult[UseActionState[S, Unit]] =
    UseActionState.async(action, initialState)(G).toHookResult

  /** @since 4.0.0 / React 19 */
  @inline final def useActionStateAsync[G[_], S, P](action: (S, P) => G[S], initialState: S)(implicit G: Async[G]): HookResult[UseActionState[S, P]] =
    UseActionState.async(action, initialState)(G).toHookResult

  /** @since 4.0.0 / React 19 */
  @inline final def useActionState[S](action: S => S, initialState: S, permalink: String): HookResult[UseActionState[S, Unit]] =
    UseActionState(action, initialState, permalink).toHookResult

  /** @since 4.0.0 / React 19 */
  @inline final def useActionState[S, P](action: (S, P) => S, initialState: S, permalink: String): HookResult[UseActionState[S, P]] =
    UseActionState(action, initialState, permalink).toHookResult

  /** @since 4.0.0 / React 19 */
  @inline final def useActionStateAsync[G[_], S](action: S => G[S], initialState: S, permalink: String)(implicit G: Async[G]): HookResult[UseActionState[S, Unit]] =
    UseActionState.async(action, initialState, permalink)(G).toHookResult

  /** @since 4.0.0 / React 19 */
  @inline final def useActionStateAsync[G[_], S, P](action: (S, P) => G[S], initialState: S, permalink: String)(implicit G: Async[G]): HookResult[UseActionState[S, P]] =
    UseActionState.async(action, initialState, permalink)(G).toHookResult

  /** @since 4.0.0 / React 19 */
  @inline final def useFormStatus: HookResult[FormStatus] =
    UseFormStatus().toHookResult
}
