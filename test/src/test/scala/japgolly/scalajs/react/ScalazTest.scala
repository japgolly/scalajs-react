package japgolly.scalajs.react

import japgolly.scalajs.react.test.ReactTestUtils
import utest._
import scalaz.StateT
import scalaz.effect.IO
import ScalazReact._

/**
 * Scala's type inference can be pretty weak sometimes.
 * Successful compilation will suffice as proof for most of these tests.
 */
object ScalazTest extends TestSuite {

  val tests = TestSuite {

    'inference {
      import TestUtil.Inference._

      val reactSIO: ReactST[IO, S, Int] = ReactS retM IO(3)

      "runState(s.liftS)"   - test[StateT[M,S,A]              ](s => c.runState(s.liftS) ).expect[CallbackTo[A]]
      "_runState(f.liftS)"  - test[B => StateT[M,S,A]         ](s => c._runState(s.liftS)).expect[B => CallbackTo[A]]
      "BackendScope ops"    - test[BackendScope[Unit, S]      ](_ runState reactSIO      ).expect[CallbackTo[Int]]
      "ComponentScopeM ops" - test[ComponentScopeM[U, S, U, N]](_ runState reactSIO      ).expect[CallbackTo[Int]]
      "ReactComponentM ops" - test[ReactComponentM[U, S, U, N]](_ runState reactSIO      ).expect[CallbackTo[Int]]
    }

    'runState {
      val c = ReactTestUtils.renderIntoDocument(CoreTest.SI())
      assert(c.state == 123)
      val f = (_:Int) * 2
      c.runState(ReactS.mod(f)).runNow()
      assert(c.state == 246)
    }
  }
}
