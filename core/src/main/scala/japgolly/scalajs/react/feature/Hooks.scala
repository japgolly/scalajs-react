package japgolly.scalajs.react.feature

import japgolly.scalajs.react.{raw => Raw, React => _, _}
import japgolly.scalajs.react.internal.{Box, OptionLike}
import scala.annotation.implicitNotFound
import scala.scalajs.js

object Hooks {

  type CustomHook[+A] = Dsl => A

  // ===================================================================================================================

  object Dsl {
    // TODO: Explain unsafety
    val unsafeGet: Dsl =
      new Dsl

    @implicitNotFound(
      "You're attempting to provide a CallbackTo[${A}] to the useEffect family of hooks."
      + "\n  - To specify a basic effect, provide a Callback (protip: try adding .void to your callback)."
      + "\n  - To specify an effect and a clean-up effect, provide a CallbackTo[Callback] where the Callback you return is the clean-up effect."
      + "\nSee https://reactjs.org/docs/hooks-reference.html#useeffect"
    )
    final case class EffectArg[A](toJs: CallbackTo[A] => Raw.React.UseEffectArg)

    object EffectArg {
      implicit val unit: EffectArg[Unit] =
        apply(_.toJsFn)

      def byCallback[A](f: A => js.UndefOr[js.Function0[Any]]): EffectArg[A] =
        apply(_.map(f).toJsFn)

      implicit val callback: EffectArg[Callback] =
        byCallback(_.toJsFn)

      implicit def optionalCallback[O[_]](implicit O: OptionLike[O]): EffectArg[O[Callback]] =
        byCallback(O.toJsUndefOr(_).map(_.toJsFn))
    }
  }

  final class Dsl private() {
    import Dsl._

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
      val initialStateFn: js.Function0[Box[S]] = () => Box(initialState)
      val orig = Raw.React.useState[Box[S]](initialStateFn)
      val originalSetState = Reusable.byRef(orig._2)
      // TODO: use useEffect for StateSnapshot
      UseState[Box[S]](originalSetState, orig)
        .xmap(_.unbox)(Box.apply)
    }

    def useStateSnapshot[S](initialState: => S)(): StateSnapshot[S] = {
      // Boxing is required because React's useState uses reflection to distinguish between {set,mod}State.
      val initialStateFn: js.Function0[Box[S]] = () => Box(initialState)
      val orig = Raw.React.useState[Box[S]](initialStateFn)
      val originalSetState = Reusable.byRef(orig._2)
      // TODO: use useEffect for StateSnapshot
      UseState[Box[S]](originalSetState, orig)
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

  // TODO: Integrate with StateSnapshot and friends
  final case class UseState[S](originalSetState: Reusable[Raw.React.UseStateSetter[_]], raw: Raw.React.UseState[S]) {

    @inline def state: S =
      raw._1

    def setState(s: S): Reusable[Callback] =
      originalSetState.withValue(Callback(raw._2(s)))

    def modState(f: S => S): Reusable[Callback] =
      originalSetState.withValue(Callback(modStateRaw(f)))

    /** TODO: No reusability here */
    // TODO: Hooks uses (S => Callback) instead of ((Option[S], Callback) => Callback)
    // def stateSnapshot: StateSnapshot[S] =
    //   StateSnapshot(state).

    // object withReuse {
    //   def stateSnapshot(implicit r: Reusability[S]): StateSnapshot[S] =
    //     StateSnapshot(state).
    // }

    @inline private def modStateRaw(f: js.Function1[S, S]): Unit =
      raw._2(f)

    /** WARNING: This does not affect the setState callback reusability. */
    private def newSetStateJs[T](givenValue: T => Unit,
                                 givenFn   : js.Function1[T, T] => Unit,
                                ): Raw.React.UseStateSetter[T] =
      tOrFn => {
        if (js.typeOf(tOrFn) == "function") {
          val mod = tOrFn.asInstanceOf[js.Function1[T, T]]
          givenFn(mod)
        } else {
          val t = tOrFn.asInstanceOf[T]
          givenValue(t)
        }
      }

    /** WARNING: This does not affect the setState callback reusability. */
    def xmap[T](f: S => T)(g: T => S): UseState[T] = {
      val newSetState = newSetStateJs[T](
        givenValue = t => raw._2(g(t)),
        givenFn    = m => raw._2((s => g(m(f(s)))): js.Function1[S, S]),
      )
      UseState(originalSetState, js.Tuple2(f(state), newSetState))
    }

    /** WARNING: This does not affect the setState callback reusability. */
    def withReusability(implicit r: Reusability[S]): UseState[S] = {
      val givenValue: S => Unit = next =>
        if (r.updateNeeded(state, next))
          raw._2(next)

      val givenFn: js.Function1[S, S] => Unit = f =>
        modStateRaw { cur =>
          val next = f(cur)
          if (r.updateNeeded(cur, next))
            next
          else
            cur // returning the exact same state as was given is how to abort an update (see hooks.js)
        }

      val newSetState = newSetStateJs(givenValue, givenFn)

      UseState(originalSetState, js.Tuple2(state, newSetState))
    }
  }

  // ===================================================================================================================

  // TODO: Integrate with StateSnapshot and friends?
  final case class UseReducer[S, A](raw: Raw.React.UseReducer[S, A]) {

    @inline def state: S =
      raw._1

    def dispatch(a: A): Callback =
      Callback(raw._2(a))

    def map[T](f: S => T): UseReducer[T, A] =
      UseReducer(js.Tuple2(f(state), raw._2))

    def contramap[B](f: B => A): UseReducer[S, B] = {
      val newDispatch: js.Function1[B, Unit] = b => raw._2(f(b))
      UseReducer(js.Tuple2(state, newDispatch))
    }

    @inline def widen[T >: S]: UseReducer[T, A] =
      UseReducer(raw)

    @inline def narrow[B <: A]: UseReducer[S, B] =
      UseReducer(raw)
  }

}
