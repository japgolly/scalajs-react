package japgolly.scalajs.react.feature

import japgolly.scalajs.react.{raw => Raw, React => _, _}
import japgolly.scalajs.react.internal.Box
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

      implicit val callback: EffectArg[Callback] =
        apply(_.map(_.toJsFn).toJsFn)
    }
  }

  final class Dsl private() {
    import Dsl._

    def useState[S](initialState: S): UseState[S] =
      // Boxing is required because React's useState uses reflection to distinguish between {set,mod}State.
      UseState[Box[S]](Raw.React.useState(Box(initialState)))
        .xmap(_.unbox)(Box.apply)

    def useStateLazily[S](initialState: => S): UseState[S] = {
      // Boxing is required because React's useState uses reflection to distinguish between {set,mod}State.
      val initialStateFn: js.Function0[Box[S]] = () => Box(initialState)
      UseState[Box[S]](Raw.React.useState(initialStateFn))
        .xmap(_.unbox)(Box.apply)
    }

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
      * Pass a “create” function and an array of dependencies. useMemo will only recompute the memoized value when one
      * of the dependencies has changed. This optimization helps to avoid expensive calculations on every render.
      *
      * Remember that the function passed to useMemo runs during rendering. Don’t do anything there that you wouldn’t
      * normally do while rendering. For example, side effects belong in [[useEffect]], not useMemo.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usememo
      */
    def useMemo[A, D](create: => A, deps: D)(implicit r: Reusability[D]): A = {
      val currentA = useStateLazily(create)
      val prevDeps = useState(deps)

      // TODO: NOPE! THIS WONT WORK: If no array is provided, a new value will be computed on every render.

      Raw.React.useMemo(() =>
        if (r.updateNeeded(prevDeps.state, deps)) {
          val a = create // execute this first in case of failure
          prevDeps.setState(deps).runNow()
          currentA.setState(a).runNow()
          a
        } else
          currentA.state
      )
    }

  }

  // ===================================================================================================================

  // TODO: Integrate with StateSnapshot and friends
  final case class UseState[S](raw: Raw.React.UseState[S]) {

    @inline def state: S =
      raw._1

    def setState(s: S): Callback =
      Callback(raw._2(s))

    def modState(f: S => S): Callback =
      Callback(modStateRaw(f))

    @inline private def modStateRaw(f: js.Function1[S, S]): Unit =
      raw._2(f)

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

    def xmap[T](f: S => T)(g: T => S): UseState[T] = {
      val newSetState = newSetStateJs[T](
        givenValue = t => raw._2(g(t)),
        givenFn    = m => raw._2((s => g(m(f(s)))): js.Function1[S, S]),
      )
      UseState(js.Tuple2(f(state), newSetState))
    }

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

      UseState(js.Tuple2(state, newSetState))
    }
  }


}
