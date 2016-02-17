package japgolly.scalajs.react.test

import japgolly.scalajs.react._
import japgolly.scalajs.react.experimental.StaticPropComponent

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

  def apply[S](render: (CompState.Access[S], S) => ReactElement) =
    ReactComponentB[S]("StatefulParent")
      .initialState_P(s => s)
      .renderS(($, s) => render($.accessCB, s))
      .build

  /**
   * Creates an initialisation value on first use which is then fed to all render calls.
   */
  def init[S, I](init: (CompState.Access[S], S) => I)(render: (I, CompState.Access[S], S) => ReactElement) = {
    var i: Option[I] = None
    apply[S] { ($, s) =>
      if (i.isEmpty)
        i = Some(init($, s))
      render(i.get, $, s)
    }
  }

  def staticPropComponent(c: StaticPropComponent)(sp: CompState.Access[c.DynamicProps] => c.StaticProps) =
    init(($: CompState.Access[c.DynamicProps], _: Any) => c(sp($)))((i, _, s) => i(s))
}
