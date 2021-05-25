package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.feature.Context
import japgolly.scalajs.react.hooks.Hooks.{UseCallbackArg, UseMemo, _}
import japgolly.scalajs.react.{CallbackTo, Ref, Reusability, Reusable, raw => Raw}

object Api {

  trait Step {
    type Self
    type Next[A]
  }

  trait SubsequentStep[_Ctx, _CtxFn[_]] extends Step {
    final type Ctx = _Ctx
    final type CtxFn[A] = _CtxFn[A]
    def squash[A]: CtxFn[A] => (Ctx => A)
  }

  final class Var[A](initialValue: A) {
    var value: A =
      initialValue
  }

  // ===================================================================================================================
  // API 1: X / (Ctx => X)

  trait Primary[Ctx, _Step <: Step] {
    final type Step = _Step

    protected def self(f: Ctx => Any)(implicit step: Step): step.Self
    protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H]

    /** Use a custom hook */
    final def custom[I, O](hook: CustomHook[I, O])(implicit step: Step, a: CustomHook.Arg[Ctx, I]): step.Next[O] =
      next(ctx => hook.unsafeInit(a.convert(ctx)))

    /** Use a custom hook */
    final def custom[O](hook: Ctx => CustomHook[Unit, O])(implicit step: Step): step.Next[O] =
      next(hook(_).unsafeInit(()))

    /** Create a new local `lazy val` on each render. */
    final def localLazyVal[A](a: => A)(implicit step: Step): step.Next[() => A] =
      localLazyValBy(_ => a)

    /** Create a new local `lazy val` on each render. */
    final def localLazyValBy[A](f: Ctx => A)(implicit step: Step): step.Next[() => A] =
      next { ctx =>
        lazy val a: A = f(ctx)
        val result = () => a // TODO: Report Scala bug
        result
      }

    /** Create a new local `val` on each render. */
    final def localVal[A](a: A)(implicit step: Step): step.Next[A] =
      next(_ => a)

    /** Create a new local `val` on each render. */
    final def localValBy[A](f: Ctx => A)(implicit step: Step): step.Next[A] =
      next(f)

    /** Create a new local `var` on each render. */
    final def localVar[A](a: => A)(implicit step: Step): step.Next[Var[A]] =
      localVarBy(_ => a)

    /** Create a new local `var` on each render. */
    final def localVarBy[A](f: Ctx => A)(implicit step: Step): step.Next[Var[A]] =
      next(ctx => new Var(f(ctx)))

    /** Provides you with a means to do whatever you want without the static guarantees that the normal DSL provides.
      * It's up to you to ensure you don't vioalte React's hook rules.
      */
    final def unchecked[A](f: Ctx => A)(implicit step: Step): step.Next[A] =
      next(f)

    /** Provides you with a means to do whatever you want without the static guarantees that the normal DSL provides.
      * It's up to you to ensure you don't vioalte React's hook rules.
      */
    final def unchecked_(f: Ctx => Any)(implicit step: Step): step.Self =
      self(f)

    /** Returns a memoized callback.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecallback
      */
    final def useCallback[A](callback: A)(implicit a: UseCallbackArg[A], step: Step): step.Next[Reusable[A]] =
      useCallbackBy(_ => (_: UseCallbackInline)(callback))

    /** Returns a memoized callback.
      *
      * Pass an inline callback and dependencies. useCallback will return a memoized version of the callback that only
      * changes if one of the dependencies has changed. This is useful when passing callbacks to optimized child
      * components that rely on reference equality to prevent unnecessary renders.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecallback
      */
    final def useCallback[A, D](callback: A, deps: D)(implicit a: UseCallbackArg[A], r: Reusability[D], step: Step): step.Next[Reusable[A]] =
      useCallbackBy(_ => (_: UseCallbackInline)(callback, deps))

    /** Returns a memoized callback.
      *
      * Pass an inline callback and dependencies. useCallback will return a memoized version of the callback that only
      * changes if one of the dependencies has changed. This is useful when passing callbacks to optimized child
      * components that rely on reference equality to prevent unnecessary renders.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecallback
      */
    final def useCallbackBy[A](f: Ctx => UseCallbackInline => HookCreated[Reusable[A]])(implicit step: Step): step.Next[Reusable[A]] =
      next(f(_)(new UseCallbackInline).result)

    /** Accepts a context object and returns the current context value for that context. The current context value is
      * determined by the value prop of the nearest `<MyContext.Provider>` above the calling component in the tree.
      *
      * When the nearest `<MyContext.Provider>` above the component updates, this Hook will trigger a rerender with the
      * latest context value passed to that `MyContext` provider. Even if an ancestor uses `React.memo` or
      * `shouldComponentUpdate`, a rerender will still happen starting at the component itself using `useContext`.
      *
      * A component calling `useContext` will always re-render when the context value changes. If re-rendering the
      * component is expensive, you can optimize it by using memoization.
      *
      * `useContext(MyContext)` only lets you read the context and subscribe to its changes. You still need a
      * `<MyContext.Provider>` above in the tree to provide the value for this context.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecontext
      */
    final def useContext[A](ctx: Context[A])(implicit step: Step): step.Next[A] =
      useContextBy(_ => ctx)

    /** Accepts a context object and returns the current context value for that context. The current context value is
      * determined by the value prop of the nearest `<MyContext.Provider>` above the calling component in the tree.
      *
      * When the nearest `<MyContext.Provider>` above the component updates, this Hook will trigger a rerender with the
      * latest context value passed to that `MyContext` provider. Even if an ancestor uses `React.memo` or
      * `shouldComponentUpdate`, a rerender will still happen starting at the component itself using `useContext`.
      *
      * A component calling `useContext` will always re-render when the context value changes. If re-rendering the
      * component is expensive, you can optimize it by using memoization.
      *
      * `useContext(MyContext)` only lets you read the context and subscribe to its changes. You still need a
      * `<MyContext.Provider>` above in the tree to provide the value for this context.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecontext
      */
    final def useContextBy[A](f: Ctx => Context[A])(implicit step: Step): step.Next[A] =
      next(ctx => UseContext.unsafeCreate(f(ctx)))

    /** Used to display a label for custom hooks in React DevTools.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usedebugvalue
      */
    final def useDebugValue(desc: => Any)(implicit step: Step): step.Self =
      useDebugValueBy(_ => desc)

    /** Used to display a label for custom hooks in React DevTools.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usedebugvalue
      */
    final def useDebugValueBy(desc: Ctx => Any)(implicit step: Step): step.Self =
      self(ctx => UseDebugValue.unsafeCreate(desc(ctx)))

    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * By default, effects run after every completed render.
      * If you'd only like to execute the effect when your component is mounted, then use [[useEffectOnMount]].
      * If you'd only like to execute the effect when certain values have changed, provide those certain values as
      * the second argument.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    final def useEffect[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Next[Unit] =
      next(_ => UseEffect.unsafeCreate(effect))

    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * This will only execute the effect when values in the second argument, change.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    final def useEffect[A, D](effect: CallbackTo[A], deps: D)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Next[Unit] =
      custom(ReusableEffect.useEffect(effect, deps))

    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * By default, effects run after every completed render.
      * If you'd only like to execute the effect when your component is mounted, then use [[useEffectOnMount]].
      * If you'd only like to execute the effect when certain values have changed, provide those certain values as
      * the second argument.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    final def useEffectBy(init: Ctx => UseEffectInline => HookCreated[Unit])(implicit step: Step): step.Next[Unit] =
      next(init(_)(new UseEffectInline).result)

    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * This will only execute the effect when your component is mounted.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    final def useEffectOnMount[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Next[Unit] =
      useEffectOnMountBy(_ => effect)

    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * This will only execute the effect when your component is mounted.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    final def useEffectOnMountBy[A](effect: Ctx => CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Next[Unit] =
      next(ctx => UseEffect.unsafeCreateOnMount(effect(ctx)))

    /** The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations. Use this to
      * read layout from the DOM and synchronously re-render. Updates scheduled inside useLayoutEffect will be flushed
      * synchronously, before the browser has a chance to paint.
      *
      * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
      *
      * If you'd only like to execute the effect when your component is mounted, then use [[useLayoutEffectOnMount]].
      * If you'd only like to execute the effect when certain values have changed, provide those certain values as
      * the second argument.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useLayoutEffect
      */
    final def useLayoutEffect[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Next[Unit] =
      next(_ => UseEffect.unsafeCreateLayout(effect))

    /** The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations. Use this to
      * read layout from the DOM and synchronously re-render. Updates scheduled inside useLayoutEffect will be flushed
      * synchronously, before the browser has a chance to paint.
      *
      * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
      *
      * This will only execute the effect when values in the second argument, change.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useLayoutEffect
      */
    final def useLayoutEffect[A, D](effect: CallbackTo[A], deps: D)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Next[Unit] =
      custom(ReusableEffect.useLayoutEffect(effect, deps))

    /** The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations. Use this to
      * read layout from the DOM and synchronously re-render. Updates scheduled inside useLayoutEffect will be flushed
      * synchronously, before the browser has a chance to paint.
      *
      * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
      *
      * If you'd only like to execute the effect when your component is mounted, then use [[useLayoutEffectOnMount]].
      * If you'd only like to execute the effect when certain values have changed, provide those certain values as
      * the second argument.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useLayoutEffect
      */
    final def useLayoutEffectBy(init: Ctx => UseLayoutEffectInline => HookCreated[Unit])(implicit step: Step): step.Next[Unit] =
      next(init(_)(new UseLayoutEffectInline).result)

    /** The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations. Use this to
      * read layout from the DOM and synchronously re-render. Updates scheduled inside useLayoutEffect will be flushed
      * synchronously, before the browser has a chance to paint.
      *
      * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
      *
      * This will only execute the effect when your component is mounted.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useLayoutEffect
      */
    final def useLayoutEffectOnMount[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Next[Unit] =
      useLayoutEffectOnMountBy(_ => effect)

    /** The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations. Use this to
      * read layout from the DOM and synchronously re-render. Updates scheduled inside useLayoutEffect will be flushed
      * synchronously, before the browser has a chance to paint.
      *
      * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
      *
      * This will only execute the effect when your component is mounted.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useLayoutEffect
      */
    final def useLayoutEffectOnMountBy[A](effect: Ctx => CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Next[Unit] =
      next(ctx => UseEffect.unsafeCreateLayoutOnMount(effect(ctx)))

    /** Returns a memoized value.
      *
      * Pass a “create” function and any dependencies. useMemo will only recompute the memoized value when one
      * of the dependencies has changed. This optimization helps to avoid expensive calculations on every render.
      *
      * Remember that the function passed to useMemo runs during rendering. Don’t do anything there that you wouldn’t
      * normally do while rendering. For example, side effects belong in [[useEffect]], not useMemo.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usememo
      */
    final def useMemo[A, D](create: => A, deps: D)(implicit r: Reusability[D], step: Step): step.Next[A] =
      custom(UseMemo(create, deps).hook)

    /** Returns a memoized value.
      *
      * Pass a “create” function and any dependencies. useMemo will only recompute the memoized value when one
      * of the dependencies has changed. This optimization helps to avoid expensive calculations on every render.
      *
      * Remember that the function passed to useMemo runs during rendering. Don’t do anything there that you wouldn’t
      * normally do while rendering. For example, side effects belong in [[useEffect]], not useMemo.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usememo
      */
    final def useMemoBy[A](f: Ctx => UseMemo.type => UseMemo[A])(implicit step: Step): step.Next[A] =
      custom((ctx: Ctx) => f(ctx)(UseMemo).hook)

    /** An alternative to [[useState]]. Accepts a reducer of type `(state, action) => newState`, and returns the
      * current state paired with a dispatch method.
      * (If you’re familiar with Redux, you already know how this works.)
      *
      * useReducer is usually preferable to useState when you have complex state logic that involves multiple
      * sub-values or when the next state depends on the previous one. useReducer also lets you optimize performance
      * for components that trigger deep updates because you can pass dispatch down instead of callbacks.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usereducer
      */
    final def useReducer[S, A](reducer: (S, A) => S, initialArg: S)(implicit step: Step): step.Next[UseReducer[S, A]] =
      next(_ => UseReducer.unsafeCreate(reducer, initialArg))

    /** An alternative to [[useState]]. Accepts a reducer of type `(state, action) => newState`, and returns the
      * current state paired with a dispatch method.
      * (If you’re familiar with Redux, you already know how this works.)
      *
      * useReducer is usually preferable to useState when you have complex state logic that involves multiple
      * sub-values or when the next state depends on the previous one. useReducer also lets you optimize performance
      * for components that trigger deep updates because you can pass dispatch down instead of callbacks.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usereducer
      */
    final def useReducer[I, S, A](reducer: (S, A) => S, initialArg: I, init: I => S)(implicit step: Step): step.Next[UseReducer[S, A]] =
      next(_ => UseReducer.unsafeCreate(reducer, initialArg, init))

    /** An alternative to [[useState]]. Accepts a reducer of type `(state, action) => newState`, and returns the
      * current state paired with a dispatch method.
      * (If you’re familiar with Redux, you already know how this works.)
      *
      * useReducer is usually preferable to useState when you have complex state logic that involves multiple
      * sub-values or when the next state depends on the previous one. useReducer also lets you optimize performance
      * for components that trigger deep updates because you can pass dispatch down instead of callbacks.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usereducer
      */
    final def useReducerBy[S, A](init: Ctx => UseReducerInline => HookCreated[UseReducer[S, A]])(implicit step: Step): step.Next[UseReducer[S, A]] =
      next(init(_)(new UseReducerInline).result)

    /** Create a mutable ref that will persist for the full lifetime of the component. */
    final def useRef[A](implicit step: Step): step.Next[Ref.Simple[A]] =
      next(_ => UseRef.unsafeCreate[A]())

    /** Create a mutable ref that will persist for the full lifetime of the component. */
    final def useRef[A](initialValue: => A)(implicit step: Step): step.Next[Raw.React.RefHandle[A]] =
      useRefBy(_ => initialValue)

    /** Create a mutable ref that will persist for the full lifetime of the component. */
    final def useRefBy[A](initialValue: Ctx => A)(implicit step: Step): step.Next[Raw.React.RefHandle[A]] =
      next(ctx => UseRef.unsafeCreate(initialValue(ctx)))

    /** Returns a stateful value, and a function to update it.
      *
      * During the initial render, the returned state is the same as the value passed as the first argument
      * (initialState).
      *
      * During subsequent re-renders, the first value returned by useState will always be the most recent state after
      * applying updates.
      */
    final def useState[S](initialState: => S)(implicit step: Step): step.Next[UseState[S]] =
      useStateBy(_ => initialState)

    /** Returns a stateful value, and a function to update it.
      *
      * During the initial render, the returned state is the same as the value passed as the first argument
      * (initialState).
      *
      * During subsequent re-renders, the first value returned by useState will always be the most recent state after
      * applying updates.
      */
    final def useStateBy[S](initialState: Ctx => S)(implicit step: Step): step.Next[UseState[S]] =
      next(ctx => UseState.unsafeCreate(initialState(ctx)))
  }

  // ===================================================================================================================
  // API 2: (H1, H2, ..., Hn) => X

  trait Secondary[Ctx, CtxFn[_], _Step <: SubsequentStep[Ctx, CtxFn]] extends Primary[Ctx, _Step] {

    /** Use a custom hook */
    final def custom[O](hook: CtxFn[CustomHook[Unit, O]])(implicit step: Step): step.Next[O] =
      custom(step.squash(hook)(_))

    /** Create a new local `lazy val` on each render. */
    final def localLazyValBy[A](f: CtxFn[A])(implicit step: Step): step.Next[() => A] =
      localLazyValBy(step.squash(f)(_))

    /** Create a new local `val` on each render. */
    final def localValBy[A](f: CtxFn[A])(implicit step: Step): step.Next[A] =
      localValBy(step.squash(f)(_))

    /** Create a new local `var` on each render. */
    final def localVarBy[A](f: CtxFn[A])(implicit step: Step): step.Next[Var[A]] =
      localVarBy(step.squash(f)(_))

    /** Provides you with a means to do whatever you want without the static guarantees that the normal DSL provides.
      * It's up to you to ensure you don't vioalte React's hook rules.
      */
    final def unchecked[A](f: CtxFn[A])(implicit step: Step): step.Next[A] =
      unchecked(step.squash(f)(_))

    /** Provides you with a means to do whatever you want without the static guarantees that the normal DSL provides.
      * It's up to you to ensure you don't vioalte React's hook rules.
      */
    final def unchecked_(f: CtxFn[Any])(implicit step: Step): step.Self =
      unchecked_(step.squash(f)(_))

    /** Returns a memoized callback.
      *
      * Pass an inline callback and dependencies. useCallback will return a memoized version of the callback that only
      * changes if one of the dependencies has changed. This is useful when passing callbacks to optimized child
      * components that rely on reference equality to prevent unnecessary renders.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecallback
      */
    final def useCallbackBy[A](f: CtxFn[UseCallbackInline => HookCreated[Reusable[A]]])(implicit step: Step): step.Next[Reusable[A]] =
      useCallbackBy(step.squash(f)(_))

    /** Accepts a context object and returns the current context value for that context. The current context value is
      * determined by the value prop of the nearest `<MyContext.Provider>` above the calling component in the tree.
      *
      * When the nearest `<MyContext.Provider>` above the component updates, this Hook will trigger a rerender with the
      * latest context value passed to that `MyContext` provider. Even if an ancestor uses `React.memo` or
      * `shouldComponentUpdate`, a rerender will still happen starting at the component itself using `useContext`.
      *
      * A component calling `useContext` will always re-render when the context value changes. If re-rendering the
      * component is expensive, you can optimize it by using memoization.
      *
      * `useContext(MyContext)` only lets you read the context and subscribe to its changes. You still need a
      * `<MyContext.Provider>` above in the tree to provide the value for this context.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecontext
      */
    final def useContextBy[A](f: CtxFn[Context[A]])(implicit step: Step): step.Next[A] =
      useContextBy(step.squash(f)(_))

    /** Used to display a label for custom hooks in React DevTools.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usedebugvalue
      */
    final def useDebugValueBy(f: CtxFn[Any])(implicit step: Step): step.Self =
      useDebugValueBy(step.squash(f)(_))

    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * By default, effects run after every completed render.
      * If you'd only like to execute the effect when your component is mounted, then use [[useEffectOnMount]].
      * If you'd only like to execute the effect when certain values have changed, provide those certain values as
      * the second argument.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    final def useEffectBy(init: CtxFn[UseEffectInline => HookCreated[Unit]])(implicit step: Step): step.Next[Unit] =
      useEffectBy(step.squash(init)(_))

    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * This will only execute the effect when your component is mounted.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    final def useEffectOnMountBy[A](effect: CtxFn[CallbackTo[A]])(implicit a: UseEffectArg[A], step: Step): step.Next[Unit] =
      useEffectOnMountBy(step.squash(effect)(_))

    /** The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations. Use this to
      * read layout from the DOM and synchronously re-render. Updates scheduled inside useLayoutEffect will be flushed
      * synchronously, before the browser has a chance to paint.
      *
      * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
      *
      * If you'd only like to execute the effect when your component is mounted, then use [[useLayoutEffectOnMount]].
      * If you'd only like to execute the effect when certain values have changed, provide those certain values as
      * the second argument.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useLayoutEffect
      */
    final def useLayoutEffectBy(init: CtxFn[UseLayoutEffectInline => HookCreated[Unit]])(implicit step: Step): step.Next[Unit] =
      useLayoutEffectBy(step.squash(init)(_))

    /** The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations. Use this to
      * read layout from the DOM and synchronously re-render. Updates scheduled inside useLayoutEffect will be flushed
      * synchronously, before the browser has a chance to paint.
      *
      * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
      *
      * This will only execute the effect when your component is mounted.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useLayoutEffect
      */
    final def useLayoutEffectOnMountBy[A](effect: CtxFn[CallbackTo[A]])(implicit a: UseEffectArg[A], step: Step): step.Next[Unit] =
      useLayoutEffectOnMountBy(step.squash(effect)(_))

    /** Returns a memoized value.
      *
      * Pass a “create” function and any dependencies. useMemo will only recompute the memoized value when one
      * of the dependencies has changed. This optimization helps to avoid expensive calculations on every render.
      *
      * Remember that the function passed to useMemo runs during rendering. Don’t do anything there that you wouldn’t
      * normally do while rendering. For example, side effects belong in [[useEffect]], not useMemo.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usememo
      */
    final def useMemoBy[A](f: CtxFn[UseMemo.type => UseMemo[A]])(implicit step: Step): step.Next[A] =
      useMemoBy(step.squash(f)(_))

    /** An alternative to [[useState]]. Accepts a reducer of type `(state, action) => newState`, and returns the
      * current state paired with a dispatch method.
      * (If you’re familiar with Redux, you already know how this works.)
      *
      * useReducer is usually preferable to useState when you have complex state logic that involves multiple
      * sub-values or when the next state depends on the previous one. useReducer also lets you optimize performance
      * for components that trigger deep updates because you can pass dispatch down instead of callbacks.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usereducer
      */
    final def useReducerBy[S, A](init: CtxFn[UseReducerInline => HookCreated[UseReducer[S, A]]])(implicit step: Step): step.Next[UseReducer[S, A]] =
      useReducerBy(step.squash(init)(_))

    /** Create a mutable ref that will persist for the full lifetime of the component. */
    final def useRefBy[A](f: CtxFn[A])(implicit step: Step): step.Next[Raw.React.RefHandle[A]] =
      useRefBy(step.squash(f)(_))

    /** Returns a stateful value, and a function to update it.
      *
      * During the initial render, the returned state is the same as the value passed as the first argument
      * (initialState).
      *
      * During subsequent re-renders, the first value returned by useState will always be the most recent state after
      * applying updates.
      */
    final def useStateBy[S](initialState: CtxFn[S])(implicit step: Step): step.Next[UseState[S]] =
      useStateBy(step.squash(initialState)(_))
  }

  // ===================================================================================================================
  // Inline API

  final class HookCreated[A] private[Api] (val result: A)

  trait Inline {
    private var calls = 0
    protected def wrap[A](a: A): HookCreated[A] = {
      if (calls > 0)
        throw new RuntimeException("Hook already created.")
      calls += 1
      new HookCreated(a)
    }
  }

  final class UseCallbackInline extends Inline {

    /** Returns a memoized callback.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecallback
      */
    def apply[A](callback: A)(implicit a: UseCallbackArg[A]): HookCreated[Reusable[A]] =
      wrap(a.fromJs(Raw.React.useCallback(a.toJs(callback))))

    /** Returns a memoized callback.
      *
      * Pass an inline callback and dependencies. useCallback will return a memoized version of the callback that only
      * changes if one of the dependencies has changed. This is useful when passing callbacks to optimized child
      * components that rely on reference equality to prevent unnecessary renders.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecallback
      */
    def apply[A, D](callback: => A, deps: D)(implicit a: UseCallbackArg[A], r: Reusability[D]): HookCreated[Reusable[A]] =
      apply(UseMemo(callback, deps).hook.unsafeInit(()))
  }

  final class UseEffectInline extends Inline {
    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * By default, effects run after every completed render.
      * If you'd only like to execute the effect when your component is mounted, then use [[useEffectOnMount]].
      * If you'd only like to execute the effect when certain values have changed, provide those certain values as
      * the second argument.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    def apply[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A]): HookCreated[Unit] =
      wrap(UseEffect.unsafeCreate(effect))

    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * This will only execute the effect when values in the second argument, change.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    def apply[A, D](effect: CallbackTo[A], deps: D)(implicit a: UseEffectArg[A], r: Reusability[D]): HookCreated[Unit] =
      wrap(ReusableEffect.useEffect(effect, deps).unsafeInit(()))
  }

  final class UseLayoutEffectInline extends Inline {
    /** The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations. Use this to
      * read layout from the DOM and synchronously re-render. Updates scheduled inside useLayoutEffect will be flushed
      * synchronously, before the browser has a chance to paint.
      *
      * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
      *
      * If you'd only like to execute the effect when your component is mounted, then use [[useLayoutEffectOnMount]].
      * If you'd only like to execute the effect when certain values have changed, provide those certain values as
      * the second argument.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useLayoutEffect
      */
    def apply[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A]): HookCreated[Unit] =
      wrap(UseEffect.unsafeCreateLayout(effect))

    /** The signature is identical to [[useEffect]], but it fires synchronously after all DOM mutations. Use this to
      * read layout from the DOM and synchronously re-render. Updates scheduled inside useLayoutEffect will be flushed
      * synchronously, before the browser has a chance to paint.
      *
      * Prefer the standard [[useEffect]] when possible to avoid blocking visual updates.
      *
      * This will only execute the effect when values in the second argument, change.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useLayoutEffect
      */
    def apply[A, D](effect: CallbackTo[A], deps: D)(implicit a: UseEffectArg[A], r: Reusability[D]): HookCreated[Unit] =
      wrap(ReusableEffect.useLayoutEffect(effect, deps).unsafeInit(()))
  }

  final class UseReducerInline extends Inline {
    /** An alternative to [[useState]]. Accepts a reducer of type `(state, action) => newState`, and returns the
      * current state paired with a dispatch method.
      * (If you’re familiar with Redux, you already know how this works.)
      *
      * useReducer is usually preferable to useState when you have complex state logic that involves multiple
      * sub-values or when the next state depends on the previous one. useReducer also lets you optimize performance
      * for components that trigger deep updates because you can pass dispatch down instead of callbacks.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usereducer
      */
    def apply[S, A](reducer: (S, A) => S, initialArg: S): HookCreated[UseReducer[S, A]] =
      wrap(UseReducer.unsafeCreate(reducer, initialArg))

    /** An alternative to [[useState]]. Accepts a reducer of type `(state, action) => newState`, and returns the
      * current state paired with a dispatch method.
      * (If you’re familiar with Redux, you already know how this works.)
      *
      * useReducer is usually preferable to useState when you have complex state logic that involves multiple
      * sub-values or when the next state depends on the previous one. useReducer also lets you optimize performance
      * for components that trigger deep updates because you can pass dispatch down instead of callbacks.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usereducer
      */
    def apply[I, S, A](reducer: (S, A) => S, initialArg: I, init: I => S): HookCreated[UseReducer[S, A]] =
      wrap(UseReducer.unsafeCreate(reducer, initialArg, init))
  }

}
