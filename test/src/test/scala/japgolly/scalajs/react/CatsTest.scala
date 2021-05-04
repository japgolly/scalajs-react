package japgolly.scalajs.react

import cats.data.StateT
import cats.{Id, Monad, ~>}
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

      assertType[Int].is[Int]

      implicit val mMonad = null.asInstanceOf[Monad[M] with (M ~> CallbackTo)]

      val retVal: Id[Int] = 3
      val reactSId: ReactST[Id, S, Int] = ReactS retM retVal

      "runState(s.liftS)"   - assertType[StateT[M,S,A]                        ](s => bs.runState(s.liftS)  ).is[CallbackTo[A]]
      "runStateFn(f.liftS)" - assertType[B => StateT[M,S,A]                   ](s => bs.runStateFn(s.liftS)).is[B => CallbackTo[A]]
      "BackendScope"        - assertType[BackendScope[Unit, S]                ](_.runState(reactSId)       ).is[CallbackTo[Int]]
      "RenderScope"         - assertType[Render                               ](_.runState(reactSId)       ).is[CallbackTo[Int]]
      "ScalaMountedId"      - assertType[ScalaComponent.MountedImpure[U, S, U]](_.runState(reactSId)       ).is[Int]
      "ScalaMountedCB"      - assertType[ScalaComponent.MountedPure  [U, S, U]](_.runState(reactSId)       ).is[CallbackTo[Int]]
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
