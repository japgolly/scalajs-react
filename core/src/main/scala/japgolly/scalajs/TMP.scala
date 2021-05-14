// TODO: Only call Hooks at the top level. Don’t call Hooks inside loops, conditions, or nested functions.
//   - idea 1: macro
//   - idea 2: monad
// TODO: useState set callbacks are Reusable.byRef
// TODO: useReducer dispatch callbacks are Reusable.byRef
// TODO: useRef[A](f: js.Function0[A], deps: js.UndefOr[HookDeps] = js.native): HookRef[A] = js.native

//   @inline def withHooks(f: HooksDsl => VdomNode): ScalaFnComponent2[Unit] =
//     withHooks[Unit]((_, h) => f(h))
//   def withHooks[P](f: (P, HooksDsl) => VdomNode): ScalaFnComponent2[P] =
//     ???
