package japgolly.scalajs.react.facade

import scala.scalajs.js
import scala.scalajs.js.|

/** See  https://reactjs.org/docs/hooks-reference.html
  *
  * @since React 16.8.0 / scalajs-react 2.0.0
  */
@js.native
trait Hooks extends js.Object {

  final type HookDeps = js.UndefOr[js.Array[_]] | Null

  final type UseStateSetter[S] = js.Function1[S | js.Function1[S, S], Unit]
  final type UseState[S] = js.Tuple2[S, UseStateSetter[S]]

  final type UseTransition = js.Tuple2[Boolean, js.Function1[js.Function0[Unit | js.Thenable[Any]], Unit]]

  final def useState[S](initial: S | js.Function0[S]): UseState[S] = js.native

  final type UseEffectArg = js.Function0[js.UndefOr[js.Function0[Any]]]
  final def useEffect(effect: UseEffectArg,
                      deps  : js.UndefOr[HookDeps] = js.native): Unit = js.native

  final def useLayoutEffect(effect: js.Function0[js.UndefOr[js.Function0[Any]]],
                            deps  : js.UndefOr[HookDeps] = js.native): Unit = js.native

  final def useInsertionEffect(effect: js.Function0[js.UndefOr[js.Function0[Any]]],
                               deps  : js.UndefOr[HookDeps] = js.native): Unit = js.native

  final def useContext[A](ctx: React.Context[A]): A = js.native

  final type UseReducerDispatch[-A] = js.Function1[A, Unit]
  final type UseReducer[+S, -A] = js.Tuple2[S, UseReducerDispatch[A]]
  final def useReducer[   S, A](reducer: js.Function2[S, A, S], initialState: S                        ): UseReducer[S, A] = js.native
  final def useReducer[I, S, A](reducer: js.Function2[S, A, S], initialArg: I, init: js.Function1[I, S]): UseReducer[S, A] = js.native

  final def useCallback[F <: js.Function](callback: F, deps: js.UndefOr[HookDeps] = js.native): F = js.native

  final def useMemo[A](f: js.Function0[A], deps: js.UndefOr[HookDeps] = js.native): A = js.native

  final def useRef[A](f: A): React.RefHandle[A] = js.native

  final def useImperativeHandle[A](
    ref   : React.RefHandle[A | Null] | ((A | Null) => Any) | Null | Unit,
    create: js.Function0[A],
    deps  : js.UndefOr[HookDeps] = js.native): Unit = js.native

  final def useDebugValue(desc: Any): Unit = js.native
  final def useDebugValue[A](value: A, desc: A => Any): Unit = js.native

  final def useId(): String = js.native

  final def useTransition(): UseTransition = js.native

  final type UseSyncExternalStoreSubscribeArg = js.Function1[js.Function0[Unit], js.Function0[Unit]]
  final def useSyncExternalStore[A](
    subscribe: UseSyncExternalStoreSubscribeArg,
    getSnapshot: js.Function0[A],
    getServerSnapshot: js.UndefOr[js.Function0[A]] = js.undefined
  ): A = js.native

  final def useDeferredValue[A](value: A, initialValue: js.UndefOr[A] = js.undefined): A = js.native

  /** @since 4.0.0 / React 19.2 */
  final def useEffectEvent[F <: js.Function](callback: F): F = js.native

  /** @since 4.0.0 / React 19 */
  final type UseActionState[S, P] = js.Tuple3[S, js.Function1[P, Unit], Boolean]

  /** @since 4.0.0 / React 19 */
  final def useActionState[S, P](
    action: js.Function2[S, P, S | js.Thenable[S]],
    initialState: S,
    permalink: js.UndefOr[String] = js.undefined
  ): UseActionState[S, P] = js.native

  /** @since 4.0.0 / React 19 */
  final def useOptimistic[S](
    passthrough: S,
  ): UseOptimistic[S] = js.native

  /** @since 4.0.0 / React 19 */
  final def useOptimistic[S, A](
    passthrough: S,
    reducer: js.Function2[S, A, S],
  ): UseOptimisticWithAction[S, A] = js.native

  final type UseOptimistic          [S]    = js.Tuple2[S, js.Function1[S | js.Function1[S, S], Unit]]
  final type UseOptimisticWithAction[S, A] = js.Tuple2[S, js.Function1[A                     , Unit]]

}
