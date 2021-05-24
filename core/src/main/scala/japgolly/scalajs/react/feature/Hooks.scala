/*
package japgolly.scalajs.react.feature

import japgolly.scalajs.react.{raw => Raw, React => _, _}
import japgolly.scalajs.react.internal.Box
import scala.scalajs.js

object Hooks {

  // ===================================================================================================================

    /** Used to display a label for custom hooks in React DevTools.
      *
      * @see https://reactjs.org/docs/hooks-reference.html#usedebugvalue
      */
    def useDebugValue(desc: => Any): Unit =
      Raw.React.useDebugValue[Null](null, _ => desc.asInstanceOf[js.Any])
  }

  // ===================================================================================================================

trait Hooks {
  def useCallback   (c: Callback): Reusable[Callback]
  def useCallback[D](c: Callback, deps: D): Reusable[Callback]
  def useCallback   (c: Ctx => UseCallbackInline): Reusable[Callback]

  def useCallback1[A]   (c: A => Callback): Reusable[A => Callback]
  def useCallback1[A, D](c: A => Callback, deps: D): Reusable[A => Callback]
  def useCallback1      (c: Ctx => UseCallbackInline1): Reusable[Callback]
}

*/
