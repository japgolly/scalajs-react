package japgolly.scalajs.react.hooks

import japgolly.scalajs.react.component.{Js => JsComponent, Scala => ScalaComponent}
import japgolly.scalajs.react.feature.Context
import japgolly.scalajs.react.hooks.Hooks._
import japgolly.scalajs.react.vdom.TopNode
import japgolly.scalajs.react.{Callback, CallbackTo, CtorType, Ref, Reusability, Reusable}
import scala.reflect.ClassTag
import scala.scalajs.js

object Api {

  trait AbstractStep {
    type Self
    type Next[A]
  }

  trait SubsequentStep[_Ctx, _CtxFn[_]] extends AbstractStep {
    final type Ctx = _Ctx
    final type CtxFn[A] = _CtxFn[A]
    def squash[A]: CtxFn[A] => (Ctx => A)
  }

  trait DynamicNextStep[A] {
    type OneOf[S, N]
    def apply[I](s: AbstractStep, i: I)(self: I => s.Self, next: I => s.Next[A]): OneOf[s.Self, s.Next[A]]
  }
  sealed trait DynamicNextStepLowPri {
    final type Next[A] = DynamicNextStep[A] { type OneOf[S, N] = N }
    final implicit def next[A]: Next[A] =
      new DynamicNextStep[A] {
        override type OneOf[S, N] = N
        override def apply[I](s: AbstractStep, i: I)(self: I => s.Self, next: I => s.Next[A]) = next(i)
      }
  }
  object DynamicNextStep extends DynamicNextStepLowPri {
    type Self[A] = DynamicNextStep[A] { type OneOf[S, N] = S }
    def self[A]: Self[A] =
      new DynamicNextStep[A] {
        override type OneOf[S, N] = S
        override def apply[I](s: AbstractStep, i: I)(self: I => s.Self, next: I => s.Next[A]) = self(i)
      }
    @inline implicit def unit: Self[Unit] = self
  }

  // ===================================================================================================================
  // API 1: X / (Ctx => X)

  trait Primary[Ctx, _Step <: AbstractStep] {
    final type Step = _Step

    protected def self(f: Ctx => Any)(implicit step: Step): step.Self
    protected def next[H](f: Ctx => H)(implicit step: Step): step.Next[H]

    /** Use a custom hook */
    final def custom[I, O](hook: CustomHook[I, O])(implicit step: Step, a: CustomHook.Arg[Ctx, I], d: DynamicNextStep[O]): d.OneOf[step.Self, step.Next[O]] =
      d(step, (ctx: Ctx) => hook.unsafeInit(a.convert(ctx)))(self(_), next(_))

    /** Use a custom hook */
    final def customBy[O](hook: Ctx => CustomHook[Unit, O])(implicit step: Step, d: DynamicNextStep[O]): d.OneOf[step.Self, step.Next[O]] =
      d(step, (ctx: Ctx) => hook(ctx).unsafeInit(()))(self(_), next(_))

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
    final def localVal[A](a: => A)(implicit step: Step): step.Next[A] =
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
    final def unchecked[A](f: => A)(implicit step: Step, d: DynamicNextStep[A]): d.OneOf[step.Self, step.Next[A]] =
      uncheckedBy(_ => f)

    /** Provides you with a means to do whatever you want without the static guarantees that the normal DSL provides.
      * It's up to you to ensure you don't vioalte React's hook rules.
      */
    final def uncheckedBy[A](f: Ctx => A)(implicit step: Step, d: DynamicNextStep[A]): d.OneOf[step.Self, step.Next[A]] =
      d(step, f)(self(_), next(_))

    /** Returns a memoized callback.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecallback
      */
    final def useCallback[A](callback: A)(implicit a: UseCallbackArg[A], step: Step): step.Next[Reusable[A]] =
      useCallbackBy(_ => callback)

    /** Returns a memoized callback.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecallback
      */
    final def useCallbackBy[A](callback: Ctx => A)(implicit a: UseCallbackArg[A], step: Step): step.Next[Reusable[A]] =
      customBy(ctx => UseCallback(callback(ctx)))

    /** Returns a memoized callback.
      *
      * Pass an inline callback and dependencies. useCallback will return a memoized version of the callback that only
      * changes if one of the dependencies has changed. This is useful when passing callbacks to optimized child
      * components that rely on reference equality to prevent unnecessary renders.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecallback
      */
    final def useCallbackWithDeps[A, D](callback: => A, deps: => D)(implicit a: UseCallbackArg[A], r: Reusability[D], step: Step): step.Next[Reusable[A]] =
      useCallbackWithDepsBy(_ => callback, _ => deps)

    /** Returns a memoized callback.
      *
      * Pass an inline callback and dependencies. useCallback will return a memoized version of the callback that only
      * changes if one of the dependencies has changed. This is useful when passing callbacks to optimized child
      * components that rely on reference equality to prevent unnecessary renders.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecallback
      */
    final def useCallbackWithDepsBy[A, D](callback: Ctx => A, deps: Ctx => D)(implicit a: UseCallbackArg[A], r: Reusability[D], step: Step): step.Next[Reusable[A]] =
      customBy(ctx => UseCallback(callback(ctx), deps(ctx)))

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
    final def useEffect[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
      useEffectBy(_ => effect)

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
    final def useEffectBy[A](init: Ctx => CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
      self(ctx => UseEffect.unsafeCreate(init(ctx)))

    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * This will only execute the effect when your component is mounted.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    final def useEffectOnMount[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
      useEffectOnMountBy(_ => effect)

    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * This will only execute the effect when your component is mounted.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    final def useEffectOnMountBy[A](effect: Ctx => CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
      self(ctx => UseEffect.unsafeCreateOnMount(effect(ctx)))

    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * This will only execute the effect when values in the second argument, change.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    final def useEffectWithDeps[A, D](effect: CallbackTo[A], deps: => D)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self =
      custom(ReusableEffect.useEffect(effect, deps))

    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * This will only execute the effect when values in the second argument, change.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    final def useEffectWithDepsBy[A, D](effect: Ctx => CallbackTo[A], deps: Ctx => D)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self =
      customBy(ctx => ReusableEffect.useEffect(effect(ctx), deps(ctx)))

    /** When invoked, forces a re-render of your component. */
    final def useForceUpdate(implicit step: Step): step.Next[Reusable[Callback]] =
      custom(CustomHook.useForceUpdate)

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
    final def useLayoutEffect[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
      useLayoutEffectBy(_ => effect)

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
    final def useLayoutEffectBy[A](init: Ctx => CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
      self(ctx => UseEffect.unsafeCreateLayout(init(ctx)))

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
    final def useLayoutEffectOnMount[A](effect: CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
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
    final def useLayoutEffectOnMountBy[A](effect: Ctx => CallbackTo[A])(implicit a: UseEffectArg[A], step: Step): step.Self =
      self(ctx => UseEffect.unsafeCreateLayoutOnMount(effect(ctx)))

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
    final def useLayoutEffectWithDeps[A, D](effect: CallbackTo[A], deps: => D)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self =
      custom(ReusableEffect.useLayoutEffect(effect, deps))

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
    final def useLayoutEffectWithDepsBy[A, D](effect: Ctx => CallbackTo[A], deps: Ctx => D)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self =
      customBy(ctx => ReusableEffect.useLayoutEffect(effect(ctx), deps(ctx)))

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
    final def useMemo[A, D](create: => A, deps: => D)(implicit r: Reusability[D], step: Step): step.Next[Reusable[A]] =
      useMemoBy(_ => create, _ => deps)

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
    final def useMemoBy[A, D](create: Ctx => A, deps: Ctx => D)(implicit r: Reusability[D], step: Step): step.Next[Reusable[A]] =
      customBy(ctx => UseMemo(create(ctx), deps(ctx)))

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
    final def useReducer[S, A](reducer: (S, A) => S, initialState: => S)(implicit step: Step): step.Next[UseReducer[S, A]] =
      useReducerBy(_ => reducer, _ => initialState)

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
    final def useReducerBy[S, A](reducer: Ctx => (S, A) => S, initialState: Ctx => S)(implicit step: Step): step.Next[UseReducer[S, A]] =
      next(ctx => UseReducer.unsafeCreate(reducer(ctx), initialState(ctx)))

    /** Create a mutable ref that will persist for the full lifetime of the component. */
    final def useRefToAnyVdom(implicit step: Step): step.Next[Ref.ToAnyVdom] =
      next(_ => UseRef.unsafeCreateToAnyVdom())

    /** Create a mutable ref that will persist for the full lifetime of the component. */
    final def useRefToVdom[N <: TopNode: ClassTag](implicit step: Step): step.Next[Ref.ToVdom[N]] =
      next(_ => UseRef.unsafeCreateToVdom[N]())

    /** Create a mutable ref that will persist for the full lifetime of the component. */
    final def useRefToScalaComponent[P, S, B](implicit step: Step): step.Next[Ref.ToScalaComponent[P, S, B]] =
      next(_ => UseRef.unsafeCreateToScalaComponent[P, S, B]())

    /** Create a mutable ref that will persist for the full lifetime of the component. */
    final def useRefToScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]]
        (c: ScalaComponent.Component[P, S, B, CT])
        (implicit step: Step): step.Next[Ref.WithScalaComponent[P, S, B, CT]] =
      next(_ => UseRef.unsafeCreateToScalaComponent(c))

    /** Create a mutable ref that will persist for the full lifetime of the component. */
    final def useRefToJsComponent[P <: js.Object, S <: js.Object](implicit step: Step): step.Next[Ref.ToJsComponent[P, S, JsComponent.RawMounted[P, S]]] =
      next(_ => UseRef.unsafeCreateToJsComponent[P, S]())

    /** Create a mutable ref that will persist for the full lifetime of the component. */
    final def useRefToJsComponentWithMountedFacade[P <: js.Object, S <: js.Object, F <: js.Object](implicit step: Step): step.Next[Ref.ToJsComponent[P, S, JsComponent.RawMounted[P, S] with F]] =
      next(_ => UseRef.unsafeCreateToJsComponentWithMountedFacade[P, S, F]())

    /** Create a mutable ref that will persist for the full lifetime of the component. */
    final def useRefToJsComponent[F[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]]
        (a: Ref.WithJsComponentArg[F, P1, S1, CT1, R, P0, S0])(implicit step: Step)
        : step.Next[Ref.WithJsComponent[F, P1, S1, CT1, R, P0, S0]] =
      next(_ => UseRef.unsafeCreateToJsComponent(a))

    /** Create a mutable ref that will persist for the full lifetime of the component. */
    final def useRef[A](initialValue: => A)(implicit step: Step): step.Next[UseRef[A]] =
      useRefBy(_ => initialValue)

    /** Create a mutable ref that will persist for the full lifetime of the component. */
    final def useRefBy[A](initialValue: Ctx => A)(implicit step: Step): step.Next[UseRef[A]] =
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

    /** Returns a stateful value, and a function to update it.
      *
      * During the initial render, the returned state is the same as the value passed as the first argument
      * (initialState).
      *
      * During subsequent re-renders, the first value returned by useState will always be the most recent state after
      * applying updates.
      */
    final def useStateWithReuse[S: ClassTag: Reusability](initialState: => S)(implicit step: Step): step.Next[UseStateWithReuse[S]] =
      useStateWithReuseBy(_ => initialState)

    /** Returns a stateful value, and a function to update it.
      *
      * During the initial render, the returned state is the same as the value passed as the first argument
      * (initialState).
      *
      * During subsequent re-renders, the first value returned by useState will always be the most recent state after
      * applying updates.
      */
    final def useStateWithReuseBy[S: ClassTag: Reusability](initialState: Ctx => S)(implicit step: Step): step.Next[UseStateWithReuse[S]] =
      next(ctx => UseStateWithReuse.unsafeCreate(initialState(ctx)))
  }

  // ===================================================================================================================
  // API 2: (H1, H2, ..., Hn) => X

  trait Secondary[Ctx, CtxFn[_], _Step <: SubsequentStep[Ctx, CtxFn]] extends Primary[Ctx, _Step] {

    /** Use a custom hook */
    final def customBy[O](hook: CtxFn[CustomHook[Unit, O]])(implicit step: Step, d: DynamicNextStep[O]): d.OneOf[step.Self, step.Next[O]] =
      customBy(step.squash(hook)(_))

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
    final def uncheckedBy[A](f: CtxFn[A])(implicit step: Step, d: DynamicNextStep[A]): d.OneOf[step.Self, step.Next[A]] =
      uncheckedBy(step.squash(f)(_))

    /** Returns a memoized callback.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecallback
      */
    final def useCallbackBy[A](callback: CtxFn[A])(implicit a: UseCallbackArg[A], step: Step): step.Next[Reusable[A]] =
      useCallbackBy(step.squash(callback)(_))

    /** Returns a memoized callback.
      *
      * Pass an inline callback and dependencies. useCallback will return a memoized version of the callback that only
      * changes if one of the dependencies has changed. This is useful when passing callbacks to optimized child
      * components that rely on reference equality to prevent unnecessary renders.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecallback
      */
    final def useCallbackWithDepsBy[A, D](callback: CtxFn[A], deps: CtxFn[D])(implicit a: UseCallbackArg[A], r: Reusability[D], step: Step): step.Next[Reusable[A]] =
      useCallbackWithDepsBy(step.squash(callback)(_), step.squash(deps)(_))

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
    final def useEffectBy[A](init: CtxFn[CallbackTo[A]])(implicit a: UseEffectArg[A], step: Step): step.Self =
      useEffectBy(step.squash(init)(_))

    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * This will only execute the effect when values in the second argument, change.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    final def useEffectWithDepsBy[A, D](effect: CtxFn[CallbackTo[A]], deps: CtxFn[D])(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self =
      useEffectWithDepsBy(step.squash(effect)(_), step.squash(deps)(_))

    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * This will only execute the effect when your component is mounted.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    final def useEffectOnMountBy[A](effect: CtxFn[CallbackTo[A]])(implicit a: UseEffectArg[A], step: Step): step.Self =
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
    final def useLayoutEffectBy[A](init: CtxFn[CallbackTo[A]])(implicit a: UseEffectArg[A], step: Step): step.Self =
      useLayoutEffectBy(step.squash(init)(_))

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
    final def useLayoutEffectWithDepsBy[A, D](effect: CtxFn[CallbackTo[A]], deps: CtxFn[D])(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self =
      useLayoutEffectWithDepsBy(step.squash(effect)(_), step.squash(deps)(_))

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
    final def useLayoutEffectOnMountBy[A](effect: CtxFn[CallbackTo[A]])(implicit a: UseEffectArg[A], step: Step): step.Self =
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
    final def useMemoBy[A, D](create: CtxFn[A], deps: CtxFn[D])(implicit r: Reusability[D], step: Step): step.Next[Reusable[A]] =
      useMemoBy(step.squash(create)(_), step.squash(deps)(_))

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
    final def useReducerBy[S, A](reducer: CtxFn[(S, A) => S], initialState: CtxFn[S])(implicit step: Step): step.Next[UseReducer[S, A]] =
      useReducerBy(step.squash(reducer)(_), step.squash(initialState)(_))

    /** Create a mutable ref that will persist for the full lifetime of the component. */
    final def useRefBy[A](f: CtxFn[A])(implicit step: Step): step.Next[UseRef[A]] =
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

    /** Returns a stateful value, and a function to update it.
      *
      * During the initial render, the returned state is the same as the value passed as the first argument
      * (initialState).
      *
      * During subsequent re-renders, the first value returned by useState will always be the most recent state after
      * applying updates.
      */
    final def useStateWithReuseBy[S: ClassTag: Reusability](initialState: CtxFn[S])(implicit step: Step): step.Next[UseStateWithReuse[S]] =
      useStateWithReuseBy(step.squash(initialState)(_))
  }
}
