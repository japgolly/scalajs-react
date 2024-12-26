package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.feature.Context
import japgolly.scalajs.react.hooks.Hooks._
import japgolly.scalajs.react.{Reusability, _}

trait react17 {
  /**
    * Returns a memoized callback.
    *
    * Pass an inline callback and dependencies. useCallback will return a memoized version of the
    * callback that only changes if one of the dependencies has changed. This is useful when passing
    * callbacks to optimized child components that rely on reference equality to prevent unnecessary
    * renders.
    *
    * @see
    *   https://reactjs.org/docs/hooks-reference.html#usecallback
    */
  @inline final def useCallback[A](callback: A)(implicit isCallbackArg: UseCallbackArg[A]): HookResult[Reusable[A]] =
    UseCallback(callback).toHookResult

  /**
    * Returns a memoized callback.
    *
    * Pass an inline callback and dependencies. useCallback will return a memoized version of the
    * callback that only changes if one of the dependencies has changed. This is useful when passing
    * callbacks to optimized child components that rely on reference equality to prevent unnecessary
    * renders.
    *
    * @see
    *   https://reactjs.org/docs/hooks-reference.html#usecallback
    */
  @inline final def useCallbackWithDeps[D: Reusability, A](deps: => D)(callback: D => A)(
    implicit isCallbackArg: UseCallbackArg[A]
  ): HookResult[Reusable[A]] =
    UseCallback.withDeps(deps)(callback).toHookResult

  /**
    * Accepts a context object and returns the current context value for that context. The current
    * context value is determined by the value prop of the nearest `<MyContext.Provider>` above the
    * calling component in the tree.
    *
    * When the nearest `<MyContext.Provider>` above the component updates, this Hook will trigger a
    * rerender with the latest context value passed to that `MyContext` provider. Even if an ancestor
    * uses `React.memo` or `shouldComponentUpdate`, a rerender will still happen starting at the
    * component itself using `useContext`.
    *
    * A component calling `useContext` will always re-render when the context value changes. If
    * re-rendering the component is expensive, you can optimize it by using memoization.
    *
    * `useContext(MyContext)` only lets you read the context and subscribe to its changes. You still
    * need a `<MyContext.Provider>` above in the tree to provide the value for this context.
    *
    * @see
    *   https://reactjs.org/docs/hooks-reference.html#usecontext
    */
  @inline final def useContext[A](ctx: Context[A]): HookResult[A] =
    HookResult(UseContext.unsafeCreate(ctx))

  /**
    * Used to display a label for custom hooks in React DevTools.
    *
    * @see
    *   https://reactjs.org/docs/hooks-reference.html#usedebugvalue
    */
  @inline final def useDebugValue(desc: => Any): HookResult[Unit] =
    HookResult(UseDebugValue.unsafeCreate(desc))

  /**
    * The callback passed to useEffect will run after the render is committed to the screen. Think of
    * effects as an escape hatch from React’s purely functional world into the imperative world.
    *
    * By default, effects run after every completed render. If you'd only like to execute the effect
    * when your component is mounted, then use [[useEffectOnMount]]. If you'd only like to execute the
    * effect when certain values have changed, provide those certain values as the second argument.
    *
    * @see
    *   https://reactjs.org/docs/hooks-reference.html#useeffect
    */
  @inline final def useEffect[A](effect: A)(implicit isEffectArg: UseEffectArg[A]): HookResult[Unit] =
    HookResult(UseEffect.unsafeCreate(effect))

  /**
    * The callback passed to useEffect will run after the render is committed to the screen. Think of
    * effects as an escape hatch from React’s purely functional world into the imperative world.
    *
    * This will only execute the effect when your component is mounted.
    *
    * @see
    *   https://reactjs.org/docs/hooks-reference.html#useeffect
    */
  @inline final def useEffectOnMount[A](effect: A)(implicit isEffectArg: UseEffectArg[A]): HookResult[Unit] =
    HookResult(UseEffect.unsafeCreateOnMount(effect))

  /**
    * The callback passed to useEffect will run after the render is committed to the screen. Think of
    * effects as an escape hatch from React’s purely functional world into the imperative world.
    *
    * This will only execute the effect when values in the second argument, change.
    *
    * @see
    *   https://reactjs.org/docs/hooks-reference.html#useeffect
    */
  @inline final def useEffectWithDeps[D: Reusability, A](deps: => D)(effect: D => A)(
    implicit isEffectArg: UseEffectArg[A]
  ): HookResult[Unit] =
    ReusableEffect.useEffect(deps)(effect).toHookResult

  /**
    * The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations.
    * Use this to read layout from the DOM and synchronously re-render. Updates scheduled inside
    * useLayoutEffect will be flushed synchronously, before the browser has a chance to paint.
    *
    * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
    *
    * If you'd only like to execute the effect when your component is mounted, then use
    * [[useLayoutEffectOnMount]]. If you'd only like to execute the effect when certain values have
    * changed, provide those certain values as the second argument.
    *
    * @see
    *   https://reactjs.org/docs/hooks-reference.html#useLayoutEffect
    */
  @inline final def useLayoutEffect[A](effect: A)(implicit isEffectArg: UseEffectArg[A]): HookResult[Unit] =
    HookResult(UseEffect.unsafeCreateLayout(effect))

  /**
    * The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations.
    * Use this to read layout from the DOM and synchronously re-render. Updates scheduled inside
    * useLayoutEffect will be flushed synchronously, before the browser has a chance to paint.
    *
    * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
    *
    * This will only execute the effect when your component is mounted.
    *
    * @see
    *   https://reactjs.org/docs/hooks-reference.html#useLayoutEffect
    */
  @inline final def useLayoutEffectOnMount[A](effect: A)(implicit isEffectArg: UseEffectArg[A]): HookResult[Unit] =
    HookResult(UseEffect.unsafeCreateLayoutOnMount(effect))

  /**
    * The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations.
    * Use this to read layout from the DOM and synchronously re-render. Updates scheduled inside
    * useLayoutEffect will be flushed synchronously, before the browser has a chance to paint.
    *
    * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
    *
    * This will only execute the effect when values in the second argument, change.
    *
    * @see
    *   https://reactjs.org/docs/hooks-reference.html#useLayoutEffect
    */
  @inline final def useLayoutEffectWithDeps[D: Reusability, A](deps: => D)(effect: D => A)(
    implicit isEffectArg: UseEffectArg[A]
  ): HookResult[Unit] =
    ReusableEffect.useLayoutEffect(deps)(effect).toHookResult

  /**
    * Returns a memoized value.
    *
    * Pass a “create” function and any dependencies. useMemo will only recompute the memoized value
    * when one of the dependencies has changed. This optimization helps to avoid expensive calculations
    * on every render.
    *
    * Remember that the function passed to useMemo runs during rendering. Don’t do anything there that
    * you wouldn’t normally do while rendering. For example, side effects belong in [[useEffect]], not
    * useMemo.
    *
    * @see
    *   https://reactjs.org/docs/hooks-reference.html#usememo
    */
  @inline final def useMemo[D: Reusability, A](deps: => D)(create: D => A): HookResult[Reusable[A]] =
    UseMemo(deps)(create).toHookResult

  /**
    * An alternative to [[useState]]. Accepts a reducer of type `(state, action) => newState`, and
    * returns the current state paired with a dispatch method. (If you’re familiar with Redux, you
    * already know how this works.)
    *
    * useReducer is usually preferable to useState when you have complex state logic that involves
    * multiple sub-values or when the next state depends on the previous one. useReducer also lets you
    * optimize performance for components that trigger deep updates because you can pass dispatch down
    * instead of callbacks.
    *
    * @see
    *   https://reactjs.org/docs/hooks-reference.html#usereducer
    */
  @inline final def useReducer[S, A](reducer: (S, A) => S, initialState: => S): HookResult[UseReducer[S, A]] =
    HookResult(UseReducer.unsafeCreate(reducer, initialState))

  /** Create a mutable ref that will persist for the full lifetime of the component. */
  @inline final def useRef[A](initialValue: => A): HookResult[UseRef[A]] =
    HookResult(UseRef.unsafeCreate(initialValue))

  /**
    * Returns a stateful value, and a function to update it.
    *
    * During the initial render, the returned state is the same as the value passed as the first
    * argument (initialState).
    *
    * During subsequent re-renders, the first value returned by useState will always be the most recent
    * state after applying updates.
    */
  @inline final def useState[A](initial: => A): HookResult[UseState[A]] =
    HookResult(UseState.unsafeCreate(initial))
}