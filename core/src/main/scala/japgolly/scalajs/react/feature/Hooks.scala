package japgolly.scalajs.react.feature

import japgolly.scalajs.react.{raw => Raw, React => _, _}
import japgolly.scalajs.react.internal.Box
import scala.scalajs.js

object Hooks {

  type CustomHook[+A] = Dsl => A

  // ===================================================================================================================

  object Dsl {
    // TODO: Explain unsafety
    val unsafeGet: Dsl =
      new Dsl
  }

  final class Dsl private() {
    import japgolly.scalajs.react.hooks.Hooks._

    /** Returns a stateful value, and a function to update it.
      *
      * During the initial render, the returned state is the same as the value passed as the first argument
      * (initialState).
      *
      * During subsequent re-renders, the first value returned by useState will always be the most recent state after
      * applying updates.
      */
    def useState[S](initialState: => S): UseState[S] = {
      // Boxing is required because React's useState uses reflection to distinguish between {set,mod}State.
      val initialStateFn   = (() => Box(initialState)): js.Function0[Box[S]]
      val originalResult   = Raw.React.useState[Box[S]](initialStateFn)
      val originalSetState = Reusable.byRef(originalResult._2)
      UseState[Box[S]](originalResult)(originalSetState)
        .xmap(_.unbox)(Box.apply)
    }

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
    def useReducer[S, A](reducer: (S, A) => S, initialArg: S): UseReducer[S, A] =
      UseReducer(Raw.React.useReducer[S, A](reducer, initialArg))

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
    def useReducer[I, S, A](reducer: (S, A) => S, initialArg: I, init: I => S): UseReducer[S, A] =
      UseReducer(Raw.React.useReducer[I, S, A](reducer, initialArg, init))

    private val noEffect: Raw.React.UseEffectArg =
      () => ()

    private def effectHookReuse[A, D](e: CallbackTo[A], deps: D)(implicit p: EffectArg[A], r: Reusability[D]): Raw.React.UseEffectArg = {
      val prevDeps = useState(deps)
      if (r.updateNeeded(prevDeps.state, deps))
        p.toJs(e.finallyRun(prevDeps.setState(deps)))
      else
        noEffect
    }

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
    def useEffect[A](effect: CallbackTo[A])(implicit p: EffectArg[A]): Unit =
      Raw.React.useEffect(p.toJs(effect))

    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * This will only execute the effect when your component is mounted.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    def useEffectOnMount[A](effect: CallbackTo[A])(implicit p: EffectArg[A]): Unit =
      Raw.React.useEffect(p.toJs(effect), new js.Array[js.Any])

    /** The callback passed to useEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * This will only execute the effect when values in the second argument, change.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useeffect
      */
    def useEffect[A, D](effect: CallbackTo[A], deps: D)(implicit x: EffectArg[A], r: Reusability[D]): Unit =
      Raw.React.useEffect(effectHookReuse(effect, deps))

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
    def useLayoutEffect[A](effect: CallbackTo[A])(implicit p: EffectArg[A]): Unit =
      Raw.React.useLayoutEffect(p.toJs(effect))

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
    def useLayoutEffectOnMount[A](effect: CallbackTo[A])(implicit p: EffectArg[A]): Unit =
      Raw.React.useLayoutEffect(p.toJs(effect), new js.Array[js.Any])

    /** The callback passed to useLayoutEffect will run after the render is committed to the screen. Think of effects as an
      * escape hatch from React’s purely functional world into the imperative world.
      *
      * This will only execute the effect when values in the second argument, change.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#useLayoutEffect
      */
    def useLayoutEffect[A, D](effect: CallbackTo[A], deps: D)(implicit x: EffectArg[A], r: Reusability[D]): Unit =
      Raw.React.useLayoutEffect(effectHookReuse(effect, deps))

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
    def useMemo[A, D](create: => A, deps: D)(implicit r: Reusability[D]): A = {
      val prevRev  = useState(0)
      val prevDeps = useState(deps)

      var rev = prevRev.state
      if (r.updateNeeded(prevDeps.state, deps)) {
        rev += 1
        prevRev.setState(rev).runNow()
        prevDeps.setState(deps).runNow()
      }

      Raw.React.useMemo(() => create, js.Array[js.Any](rev))
    }

    /** Returns a memoized callback.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecallback
      */
    def useCallback(c: Callback): Reusable[Callback] =
      Reusable.callbackByRef(
        Callback.fromJsFn(
          Raw.React.useCallback(
            c.toJsFn)))

    /** Returns a memoized callback.
      *
      * Pass an inline callback and dependencies. useCallback will return a memoized version of the callback that only
      * changes if one of the dependencies has changed. This is useful when passing callbacks to optimized child
      * components that rely on reference equality to prevent unnecessary renders.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usecallback
      */
    def useCallback[D](callback: Callback, deps: D)(implicit r: Reusability[D]): Reusable[Callback] =
      useCallback(
        CallbackTo.fromJsFn(
          effectHookReuse(callback, deps)
        ).void
      )

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
    def useContext[A](ctx: Context[A]): A = {
      val rawValue = Raw.React.useContext(ctx.raw)
      ctx.jsRepr.fromJs(rawValue)
    }

    /** Used to display a label for custom hooks in React DevTools.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usedebugvalue
      */
    def useDebugValue(desc: => Any): Unit =
      Raw.React.useDebugValue[Null](null, _ => desc.asInstanceOf[js.Any])
  }

  // ===================================================================================================================

}
