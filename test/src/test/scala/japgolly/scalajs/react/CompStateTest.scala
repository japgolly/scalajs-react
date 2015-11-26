package japgolly.scalajs.react

import monocle.macros.Lenses
import extra._
import MonocleReact._

/**
 * Successful compilation here is the test.
 */
object CompStateTest {

  @Lenses case class State1(int: Int)
  @Lenses case class State2(state1: State1)
  @Lenses case class State3(state2: State2)

  val state3_Int = State3.state2 ^|-> State2.state1 ^|-> State1.int

  def test[S](a: CompState.Access[S]) = a

  class Backend($: BackendScope[Unit, State3]) {

    test($)

    test($ zoomL State3.state2)
    test($ zoomL State3.state2 zoomL State2.state1)
    test($ zoomL state3_Int).setState(6)

    def render(state: State3) = {

      ExternalVar.s$(state, $)
      ReusableVar.s$(state3_Int get state, $ zoomL state3_Int)

      ExternalVar.at(state3_Int)(state, $)
      ReusableVar.at(state3_Int)(state, $)
    }
  }

  ReactComponentB[Unit]("").initialState(State3(???))
    .renderS { ($, state) =>

      ExternalVar.s$(state, $)
      ReusableVar.s$(state3_Int get state, $ zoomL state3_Int)

      ExternalVar.at(state3_Int)(state, $)
      ReusableVar.at(state3_Int)(state, $)

      ???
    }
}
