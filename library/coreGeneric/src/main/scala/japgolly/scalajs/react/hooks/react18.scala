package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.hooks.Hooks._
import japgolly.scalajs.react.util.Effect.Sync
import scala.scalajs.js
import japgolly.scalajs.react.Reusability

trait react18 {
  /**
  * Generates unique IDs that can be passed to accessibility attributes.
  *
  * @see
  *   https://react.dev/reference/react/useId
  */
  @inline final def useId: HookResult[String] =
    UseId().toHookResult

  /**
  * Allows components to avoid undesirable loading states by waiting for content to load before
  * transitioning to the next screen. It also allows components to defer slower, data fetching
  * updates until subsequent renders so that more crucial updates can be rendered immediately.
  *
  * **If some state update causes a component to suspend, that state update should be wrapped in a
  * transition.**
  *
  * @see
  *   {@link https://react.dev/reference/react/useTransition}
  */
  @inline final def useTransition: HookResult[UseTransition] =
    UseTransition().toHookResult

  /**
    * Lets you subscribe to an external store.
    *
    * @see
    *   {@link https://react.dev/reference/react/useSyncExternalStore}
    */
  @inline final def useSyncExternalStore[F[_], A](subscribe: F[Unit] => F[F[Unit]], getSnapshot: F[A], getServerSnapshot: js.UndefOr[F[A]] = js.undefined)(implicit F: Sync[F]): HookResult[A] =
    UseSyncExternalStore(subscribe, getSnapshot, getServerSnapshot).toHookResult


  /** The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations, but before any 
    * layout Effects fire. Use this to insert styles before any Effects fire that may need to read layout. Updates 
    * scheduled inside useLayoutEffect will be flushed synchronously, before the browser has a chance to paint.
    *
    * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
    *
    * If you'd only like to execute the effect when your component is mounted, then use [[useInsertionEffectOnMount]].
    * If you'd only like to execute the effect when certain values have changed, provide those certain values as
    * the first argument.
    *
    * @see https://react.dev/reference/react/useInsertionEffect#useInsertionEffect
    */
  @inline final def useInsertionEffect[A](effect: A)(implicit isEffectArg: UseEffectArg[A]): HookResult[Unit] =
    HookResult(UseEffect.unsafeCreateInsertion(effect))

  /** The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations, but before any 
    * layout Effects fire. Use this to insert styles before any Effects fire that may need to read layout. Updates 
    * scheduled inside useLayoutEffect will be flushed synchronously, before the browser has a chance to paint.
    *
    * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
    *
    * If you'd only like to execute the effect when your component is mounted, then use [[useInsertionEffectOnMount]].
    * If you'd only like to execute the effect when certain values have changed, provide those certain values as
    * the first argument.
    *
    * @see https://react.dev/reference/react/useInsertionEffect#useInsertionEffect
    */
  @inline final def useInsertionEffectOnMount[A](effect: A)(implicit isEffectArg: UseEffectArg[A]): HookResult[Unit] =
    HookResult(UseEffect.unsafeCreateInsertionOnMount(effect))

  /** The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations, but before any 
    * layout Effects fire. Use this to insert styles before any Effects fire that may need to read layout. Updates 
    * scheduled inside useLayoutEffect will be flushed synchronously, before the browser has a chance to paint.
    *
    * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
    *
    * If you'd only like to execute the effect when your component is mounted, then use [[useInsertionEffectOnMount]].
    * If you'd only like to execute the effect when certain values have changed, provide those certain values as
    * the first argument.
    *
    * @see https://react.dev/reference/react/useInsertionEffect#useInsertionEffect
    */
  @inline final def useInsertionEffectWithDeps[D: Reusability, A](deps: => D)(effect: D => A)(
    implicit isEffectArg: UseEffectArg[A]
  ): HookResult[Unit] =
    ReusableEffect.useInsertionEffect(deps)(effect).toHookResult
}