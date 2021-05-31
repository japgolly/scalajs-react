package japgolly.scalajs.react.raw

import scala.annotation.nowarn
import scala.scalajs.js
import scala.scalajs.js.|

/** See  https://reactjs.org/docs/hooks-reference.html
  *
  * @since React 16.8.0 / scalajs-react 1.8.0
  */
@js.native
@nowarn("cat=unused")
trait Hooks extends js.Object {

  final type HookDeps = js.UndefOr[js.Array[Any]] | Null

  final type UseStateSetter[S] = js.Function1[S | js.Function1[S, S], Unit]
  final type UseState[S] = js.Tuple2[S, UseStateSetter[S]]

  final def useState[S](initial: S | js.Function0[S]): UseState[S] = js.native

  final type UseEffectArg = js.Function0[js.UndefOr[js.Function0[Any]]]
  final def useEffect(effect: UseEffectArg,
                      deps  : js.UndefOr[HookDeps] = js.native): Unit = js.native

  final def useLayoutEffect(effect: js.Function0[js.UndefOr[js.Function0[Any]]],
                            deps  : js.UndefOr[HookDeps] = js.native): Unit = js.native

  final def useContext[A <: js.Any](ctx: React.Context[A]): A = js.native

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
}
