package japgolly.scalajs.react

import japgolly.scalajs.react.test.ReactTestUtils
import utest._
import scalaz.{~>, StateT, Monad}
import scalaz.effect.IO
import ScalazReact._

/**
 * Scala's type inference can be pretty weak sometimes.
 * Successful compilation will suffice as proof for most of these tests.
 */
object ScalazTest extends TestSuite {

  def test[A] = new {
    def apply[B](f: A => B) = new {
      def expect[C](implicit ev: B =:= C): Unit = ()
    }
  }

  trait M[A]
  implicit val mMonad = null.asInstanceOf[Monad[M] with (M ~> IO)]
  trait S
  trait A
  trait B
  val c = null.asInstanceOf[ComponentScopeM[Unit, S, Unit]]
  type U = Unit
  type N = TopNode

  val tests = TestSuite {
    'inference {
      "runState(s.liftS)"   - test[StateT[M,S,A]              ](s => c.runState(s.liftS) ).expect[IO[A]]
      "_runState(f.liftS)"  - test[B => StateT[M,S,A]         ](s => c._runState(s.liftS)).expect[B => IO[A]]
      "BackendScope ops"    - test[BackendScope[Unit, S]      ](_.modStateIO(identity)   ).expect[IO[Unit]]
      "ComponentScopeM ops" - test[ComponentScopeM[U, S, U]   ](_.modStateIO(identity)   ).expect[IO[Unit]]
      "ReactComponentM ops" - test[ReactComponentM[U, S, U, N]](_.modStateIO(identity)   ).expect[IO[Unit]]
    }
    'runState {
      val c = ReactTestUtils.renderIntoDocument(CoreTest.SI())
      assert(c.state == 123)
      val f = (_:Int) * 2
      c.runState(ReactS.mod(f)).unsafePerformIO()
      assert(c.state == 246)
    }
  }
}
