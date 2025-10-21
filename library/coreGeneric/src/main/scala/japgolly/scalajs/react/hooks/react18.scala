package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.Reusability
import japgolly.scalajs.react.hooks.HookResult
import japgolly.scalajs.react.hooks.Hooks._
import japgolly.scalajs.react.util.Effect.Sync
import scala.scalajs.js

trait react18 {

  /** Generates unique IDs that can be passed to accessibility attributes.
    *
    * @see
    *   https://react.dev/reference/react/useId
    *
    * @since 3.0.0 / React 18.0.0
    */
  @inline final def useId: HookResult[String] =
    UseId().toHookResult

  /** Allows components to avoid undesirable loading states by waiting for content to load before
    * transitioning to the next screen. It also allows components to defer slower, data fetching
    * updates until subsequent renders so that more crucial updates can be rendered immediately.
    *
    * **If some state update causes a component to suspend, that state update should be wrapped in a
    * transition.**
    *
    * @see
    *   {@link https://react.dev/reference/react/useTransition}
    *
    * @since 3.0.0 / React 18.0.0
    */
  @inline final def useTransition: HookResult[UseTransition] =
    UseTransition().toHookResult

  /** Lets you subscribe to an external store.
    *
    * @see
    *   {@link https://react.dev/reference/react/useSyncExternalStore}
    *
    * @since 3.0.0 / React 18.0.0
    */
  @inline final def useSyncExternalStore[F[_], A](
    subscribe: F[Unit] => F[F[Unit]],
    getSnapshot: F[A],
    getServerSnapshot: js.UndefOr[F[A]] = js.undefined
  )(implicit F: Sync[F]): HookResult[A] =
    UseSyncExternalStore(subscribe, getSnapshot, getServerSnapshot).toHookResult


  /** The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations, but before any
    * layout Effects fire. Use this to insert styles before any Effects fire that may need to read layout. Updates
    * scheduled inside useLayoutEffect will be flushed synchronously, before the browser has a chance to paint.
    *
    * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
    *
    * If you'd only like to execute the effect when your component is mounted, then use [[useInsertionEffectOnMount]].
    * If you'd only like to execute the effect when certain values have changed, then use [[useInsertionEffectWithDeps]].
    *
    * @see https://react.dev/reference/react/useInsertionEffect#useInsertionEffect
    *
    * @since 3.0.0 / React 18.0.0
    */
  @inline final def useInsertionEffect[A](effect: A)(implicit isEffectArg: UseEffectArg[A]): HookResult[Unit] =
    HookResult(UseEffect.unsafeCreateInsertion(effect))

  /** The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations, but before any
    * layout Effects fire. Use this to insert styles before any Effects fire that may need to read layout. Updates
    * scheduled inside useLayoutEffect will be flushed synchronously, before the browser has a chance to paint.
    *
    * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
    *
    * This will only execute the effect when your component is mounted.
    *
    * @see https://react.dev/reference/react/useInsertionEffect#useInsertionEffect
    *
    * @since 3.0.0 / React 18.0.0
    */
  @inline final def useInsertionEffectOnMount[A](effect: A)(implicit isEffectArg: UseEffectArg[A]): HookResult[Unit] =
    HookResult(UseEffect.unsafeCreateInsertionOnMount(effect))

  /** The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations, but before any
    * layout Effects fire. Use this to insert styles before any Effects fire that may need to read layout. Updates
    * scheduled inside useLayoutEffect will be flushed synchronously, before the browser has a chance to paint.
    *
    * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
    *
    * This will only execute the effect when values in the first argument change.
    *
    * @see https://react.dev/reference/react/useInsertionEffect#useInsertionEffect
    *
    * @since 3.0.0 / React 18.0.0
    */
  @inline final def useInsertionEffectWithDeps[D: Reusability, A](deps: => D)(effect: D => A)(
    implicit isEffectArg: UseEffectArg[A]
  ): HookResult[Unit] =
    ReusableEffect.useInsertionEffect(deps)(effect).toHookResult

  /** Lets you defer updating a part of the UI.
    *
    * @see
    *   {@link https://react.dev/reference/react/useDeferredValue}
    *
    * @since 3.0.0 / React 18.0.0
    */
  @inline final def useDeferredValue[A](value: A): HookResult[A] =
    UseDeferredValue(value).toHookResult

  // initialValue was added in React 19 - Replace when we upgrade to React 19
  // @inline final def useDeferredValue[A](value: A, initialValue: js.UndefOr[A] = js.undefined): HookResult[A] =
  //   UseDeferredValue(value, initialValue).toHookResult
}
