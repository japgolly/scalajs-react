package japgolly.scalajs.react.extra

import japgolly.scalajs.react._

/**
 * Provides a component with safe R/W access to an external variable.
 *
 * Use [[ReusableVar]] for a reusable version of this.
 */
final class ExternalVar[A](val value: A, val set: A => Callback) {

  def mod(f: A => A): Callback =
    set(f(value))

  // Zoom is dangerously deceptive here as it appears to work but will often override the non-zoomed subset of A's state.
  // Use the zoom methods on ComponentScopes directly for a reliable function.
  //
  // def zoomL[B](l: Lens[A, B]): ExternalVar[B] =
  //   ExternalVar(l get value)(b => set(l.set(b)(value)))
}


object ExternalVar {
  @inline def apply[A](value: A)(set: A => Callback): ExternalVar[A] =
    new ExternalVar(value, set)

  @inline def state[S]($: CompStateFocus[S]): ExternalVar[S] =
    new ExternalVar($.state, $.setState(_))
}
