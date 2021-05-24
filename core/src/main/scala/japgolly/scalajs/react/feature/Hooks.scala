/*
package japgolly.scalajs.react.feature

import japgolly.scalajs.react.{raw => Raw, React => _, _}
import japgolly.scalajs.react.internal.Box
import scala.scalajs.js

object Hooks {

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