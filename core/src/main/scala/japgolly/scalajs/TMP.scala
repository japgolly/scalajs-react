// TODO: Only call Hooks at the top level. Don’t call Hooks inside loops, conditions, or nested functions.
//   - idea 1: macro
//   - idea 2: monad
//   - PROBLEM: custom hooks... maybe a monad is needed after all... need to tell by sig (i.e the $ param enough?)
//   - Value in purifying? Technically the right thing to do. More info in types = more power, more informed
//     Also covers cases where $ provided to helper class and methods execute side-effects
//   - Cases
//     - if
//     - Array.fill
//     - lazy vals
//     - vars
//     - CallbackTo(_).when().runNow()
//     - put calls in a fn and then use those fns in if (same as custom hooks)
//   - Too restrictive if implemented? Escape hatch?
//   - Is tracking where $ goes enough?
//   - Purpose: help avoid reasonable mistakes, not prevent malice

// TODO: useState set callbacks are Reusable.byRef
// TODO: useReducer dispatch callbacks are Reusable.byRef
// TODO: useRef[A](f: js.Function0[A], deps: js.UndefOr[HookDeps] = js.native): HookRef[A] = js.native
// TODO: Add variance to UseReducer? Likeliness of .map vs variance
// TODO: @_nkgm: BTW useCallback shouldn't necessarily take a js.Function0 callback - there’s valid cases where you want a Function1+ eg useCallback(event => …) to pass down to a child component (also this would explain why react doesn’t automatically pass the dependencies as function arguments)

//   @inline def withHooks(f: HooksDsl => VdomNode): ScalaFnComponent2[Unit] =
//     withHooks[Unit]((_, h) => f(h))
//   def withHooks[P](f: (P, HooksDsl) => VdomNode): ScalaFnComponent2[P] =
//     ???
