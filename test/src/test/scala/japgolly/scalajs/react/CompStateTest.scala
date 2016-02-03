package japgolly.scalajs.react

import scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.concurrent.Future
import monocle.macros.Lenses
import vdom.prefix_<^._
import extra._
import MonocleReact._
import test.ReactTestUtils

/**
 * Successful compilation here is the test.
 */
object CompStateCompilationTest {

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

// =====================================================================================================================
import utest._

object CompStateTest extends TestSuite {

  lazy val X = ReactComponentB[Unit]("X")
    .initialState(1)
    .render_S(s => <.div("state = ", s))
    .buildU

  override def tests = TestSuite {
    val x = ReactTestUtils renderIntoDocument X()

    'direct {
      var i = 10
      val ff = x.future.modState(_ + 1, Callback(i -= 7))
      val f: Future[Unit] = ff
      f.map { _ =>
        assert(x.state == 2)
        assert(i == 3)
      }
    }

    'callback {
      var i = 90
      val cbb = x.accessCB.future.modState(_ + 1, Callback(i -= 5))
      val cb: CallbackTo[Future[Unit]] = cbb
      // Look, it's repeatable
      for {
        _ <- cb.runNow()
        _ <- cb.runNow()
      } yield {
        assert(x.state == 3)
        assert(i == 80)
      }
    }
  }
}
