/*
package japgolly.scalajs.react.feature

import japgolly.scalajs.react.{raw => Raw, React => _, _}
import japgolly.scalajs.react.internal.Box
import scala.scalajs.js

object Hooks {

  // ===================================================================================================================

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

  // ===================================================================================================================

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

  // ===================================================================================================================

    /** Used to display a label for custom hooks in React DevTools.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usedebugvalue
      */
    def useDebugValue(desc: => Any): Unit =
      Raw.React.useDebugValue[Null](null, _ => desc.asInstanceOf[js.Any])
  }

  // ===================================================================================================================
*/