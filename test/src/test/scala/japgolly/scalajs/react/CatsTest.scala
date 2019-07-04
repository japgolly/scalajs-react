package japgolly.scalajs.react

import cats.{Monad, Id, ~>}
import cats.data.StateT
import cats.implicits._

import japgolly.scalajs.react.CatsReact._
import japgolly.scalajs.react.test.ReactTestUtils
import japgolly.scalajs.react.test.TestUtil._
import japgolly.scalajs.react.vdom.html_<^._
import utest._

/**
 * Scala's type inference can be pretty weak sometimes.
 * Successful compilation will suffice as proof for most of these tests.
 */
object CatsTest extends TestSuite {

  lazy val SI = ScalaComponent.builder[Unit]("SI")
    .initialState(123)
    .render(T => <.input(^.value := T.state.toString))
    .build

  val tests = Tests {

    "inference" - {
      import japgolly.scalajs.react.test.InferenceUtil._

      implicit val mMonad = null.asInstanceOf[Monad[M] with (M ~> CallbackTo)]

      val retVal: Id[Int] = 3
      val reactSId: ReactST[Id, S, Int] = ReactS retM retVal

      "runState(s.liftS)"   - test[StateT[M,S,A]                        ](s => bs.runState(s.liftS)  ).expect[CallbackTo[A]]
      "runStateFn(f.liftS)" - test[B => StateT[M,S,A]                   ](s => bs.runStateFn(s.liftS)).expect[B => CallbackTo[A]]
      "BackendScope"        - test[BackendScope[Unit, S]                ](_.runState(reactSId)       ).expect[CallbackTo[Int]]
      "RenderScope"         - test[Render                               ](_.runState(reactSId)       ).expect[CallbackTo[Int]]
      "ScalaMountedId"      - test[ScalaComponent.MountedImpure[U, S, U]](_.runState(reactSId)       ).expect[Int]
      "ScalaMountedCB"      - test[ScalaComponent.MountedPure  [U, S, U]](_.runState(reactSId)       ).expect[CallbackTo[Int]]
    }

    "runState" - {
      val c = ReactTestUtils.renderIntoDocument(SI())
      assertEq(c.state, 123)
      val f = (_: Int) * 2
      c.runState(ReactS.mod(f))
      assertEq(c.state, 246)
    }

  }
}
