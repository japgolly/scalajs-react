package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.hooks.Hooks._
import japgolly.scalajs.react.util.Effect.Sync
import scala.scalajs.js

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
}