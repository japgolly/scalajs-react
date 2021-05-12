package japgolly.scalajs.react.raw

import scala.annotation.nowarn
import scala.scalajs.js
import scala.scalajs.js.|

/** See  https://reactjs.org/docs/hooks-reference.html
  *
  * @since 16.8.0
  */
@js.native
@nowarn("cat=unused")
trait Hooks extends js.Object {

  final type HookDeps = js.UndefOr[js.Array[js.Any]] | Null

  // TODO: Need to Box S in case user's S is a function
  final type UseState[S] = js.Tuple2[S, js.Function1[S | js.Function1[S, S], Unit]]

  final def useState[S](initial: S | js.Function0[S]): UseState[S] = js.native

  final def useEffect(effect: js.Function0[js.UndefOr[js.Function0[Any]]],
                      deps  : js.UndefOr[HookDeps] = js.native): Unit = js.native

  final def useLayoutEffect(effect: js.Function0[js.UndefOr[js.Function0[Any]]],
                            deps  : js.UndefOr[HookDeps] = js.native): Unit = js.native

  final def useContext[A <: js.Any](ctx: React.Context[A]): React.Context[A] = js.native

  final type UseReducer[S, A] = js.Tuple2[S, js.Function1[A, Unit]]
  final def useReducer[   S, A](reducer: js.Function2[S, A, S], initialArg: S                          ): UseReducer[S, A] = js.native
  final def useReducer[I, S, A](reducer: js.Function2[S, A, S], initialArg: I, init: js.Function1[I, S]): UseReducer[S, A] = js.native

  final def useCallback(callback: js.Function0[Unit], deps: js.UndefOr[HookDeps] = js.native): js.Function0[Unit] = js.native

  final def useMemo[A](f: js.Function0[A], deps: js.UndefOr[HookDeps] = js.native): js.Function0[A] = js.native

  final def useRef[A](f: js.Function0[A], deps: js.UndefOr[HookDeps] = js.native): HookRef[A] = js.native

  // TODO: useImperativeHandle

  final def useDebugValue(desc: js.Any): Unit = js.native
  final def useDebugValue[A](value: A, desc: A => js.Any): Unit = js.native
}

@js.native
trait HookRef[+A] extends js.Object {
  val current: A = js.native
}
