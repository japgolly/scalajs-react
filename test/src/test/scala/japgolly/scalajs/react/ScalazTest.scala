package japgolly.scalajs.react

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

  val tests = TestSuite {
    "runState(s.liftS)"   - test[StateT[M,S,A]     ](s => c.runState(s.liftS)  ).expect[IO[A]]
    "_runState(f._liftS)" - test[B => StateT[M,S,A]](s => c._runState(s._liftS)).expect[B => IO[A]]
  }
}
