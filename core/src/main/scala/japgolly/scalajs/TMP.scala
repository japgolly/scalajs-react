// TODO: Only call Hooks at the top level. Don’t call Hooks inside loops, conditions, or nested functions.
//   - idea 1: macro
//   - idea 2: monad

// TODO: useCallback(callback: js.Function0[Unit], deps: js.UndefOr[HookDeps] = js.native): js.Function0[Unit] = js.native
// TODO: useContext[A <: js.Any](ctx: React.Context[A]): React.Context[A] = js.native
// TODO: useDebugValue(desc: js.Any): Unit = js.native
// TODO: useDebugValue[A](value: A, desc: A => js.Any): Unit = js.native
// TODO: useMemo[A](f: js.Function0[A], deps: js.UndefOr[HookDeps] = js.native): A = js.native
// TODO: useReducer[   S, A](reducer: js.Function2[S, A, S], initialArg: S                          ): UseReducer[S, A] = js.native
// TODO: useReducer[I, S, A](reducer: js.Function2[S, A, S], initialArg: I, init: js.Function1[I, S]): UseReducer[S, A] = js.native
// TODO: useRef[A](f: js.Function0[A], deps: js.UndefOr[HookDeps] = js.native): HookRef[A] = js.native



//   @inline def withHooks(f: HooksDsl => VdomNode): ScalaFnComponent2[Unit] =
//     withHooks[Unit]((_, h) => f(h))

//   def withHooks[P](f: (P, HooksDsl) => VdomNode): ScalaFnComponent2[P] =
//     ???
// }

// trait HooksDsl {

//   def useCallback(c: Callback): Reusable[Callback] =
//     Reusable.callbackByRef(
//       Callback.fromJsFn(
//         Raw.React.useCallback(
//           c.toJsFn)))

//   def useCallback[D](callback: Callback, deps: D)(implicit reuse: Reusability[D]): Reusable[Callback] = {
//     // TODO: Use generic ver later
//     val prev = useState(deps)
//     val callback2 = callback
//       .finallyRun(prev.setState(deps))
//       .unless_(reuse.test(prev.state, deps))
//     useCallback(callback2)
//   }

//   // TODO: Consider use Xxxx(deps)(body) for nice Scala usage. Example:
//   // $.useMemo(deps) {
//   //   asdfklajshflkajhsdf
//   // }
