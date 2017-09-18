package japgolly.scalajs.react

import scalaz.{Monad, StateT, ~>}
import scalaz.effect.IO

import utest._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._

import ScalazReact._

/**
 * Scala's type inference can be pretty weak sometimes.
 * Successful compilation will suffice as proof for most of these tests.
 */
object ScalazTest extends TestSuite {

  lazy val SI = ScalaComponent.builder[Unit]("SI")
    .initialState(123)
    .render(T => <.input(^.value := T.state.toString))
    .build

  val tests = Tests {

    'inference {
      import japgolly.scalajs.react.test.InferenceUtil._

      implicit val mMonad = null.asInstanceOf[Monad[M] with (M ~> CallbackTo)]

      val reactSIO: ReactST[IO, S, Int] = ReactS retM IO(3)

      "runState(s.liftS)"   - test[StateT[M,S,A]                        ](s => bs.runState(s.liftS)  ).expect[CallbackTo[A]]
      "runStateFn(f.liftS)" - test[B => StateT[M,S,A]                   ](s => bs.runStateFn(s.liftS)).expect[B => CallbackTo[A]]
      "BackendScope"        - test[BackendScope[Unit, S]                ](_.runState(reactSIO)       ).expect[CallbackTo[Int]]
      "RenderScope"         - test[Render                               ](_.runState(reactSIO)       ).expect[CallbackTo[Int]]
      "ScalaMountedId"      - test[ScalaComponent.MountedImpure[U, S, U]](_.runState(reactSIO)       ).expect[Int]
      "ScalaMountedCB"      - test[ScalaComponent.MountedPure  [U, S, U]](_.runState(reactSIO)       ).expect[CallbackTo[Int]]
    }

    'runState {
      val c = ReactTestUtils.renderIntoDocument(SI())
      assertEq(c.state, 123)
      val f = (_: Int) * 2
      c.runState(ReactS.mod(f))
      assertEq(c.state, 246)
    }

  }
}
