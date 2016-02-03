package japgolly.scalajs.react.test

import japgolly.scalajs.react._

/**
 * A stateful component you can wrap around a component you want to test.
 *
 * Scenarios in which this might be useful:
 *   - Testing props changes. (`.setProps` has been deprecated and this is clearer and safer that re-rendering.)
 *   - Testing a component which uses a parent's `CompState.Access`, `CompState.WriteAccess` or similar.
 *
 * @since 0.10.5
 */
object StatefulParent {

  def apply[S](f: (CompState.Access[S], S) => ReactElement) =
    ReactComponentB[S]("StatefulParent")
      .initialState_P(s => s)
      .renderS(($, s) => f($.accessCB, s))
      .build
}
