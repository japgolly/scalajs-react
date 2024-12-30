package japgolly.scalajs.react.facade

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

/** 
  * @since React 18.3.0 / scalajs-react 3.0.0
  */
@js.native
trait Testing extends js.Object {
  /** When writing UI tests, tasks like rendering, user events, or data fetching can be considered as "units" of
    * interaction with a user interface. React provides a helper called act() that makes sure all updates related to
    * these "units" have been processed and applied to the DOM before you make any assertions:
    *
    * {{{
    *   act(() => {
    *     // render components
    *   });
    *   // make assertions
    * }}}
    *
    * This helps make your tests run closer to what real users would experience when using your application.
    */
  final def act(body: js.Function0[Any]): js.Thenable[Unit] = js.native

/** When writing UI tests, tasks like rendering, user events, or data fetching can be considered as "units" of
  * interaction with a user interface. React provides a helper called act() that makes sure all updates related to
  * these "units" have been processed and applied to the DOM before you make any assertions:
  *
  * {{{
  *   await act(async () => {
  *     // render components
  *   });
  *   // make assertions
  * }}}
  *
  * This helps make your tests run closer to what real users would experience when using your application.
  */
  @JSName("act")
  final def actAsync[A](body: js.Function0[js.Thenable[A]]): js.Thenable[A] = js.native
}
