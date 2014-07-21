package japgolly.scalajs.react

import org.scalajs.dom
import vdom.ReactVDom._

import scalaz._
import Scalaz.Id
import scalaz.effect.IO

object ScalazReact {

  sealed trait ExecUnsafe[M[_]] {
    def execUnsafe[A](m: M[A]): A
  }
  implicit object ExecUnsafeId extends ExecUnsafe[Id] {
    override def execUnsafe[A](m: Id[A]): A = m
  }
  implicit object ExecUnsafeIO extends ExecUnsafe[IO] {
    override def execUnsafe[A](m: IO[A]): A = m.unsafePerformIO()
  }

  implicit final class SzRExt_Attr(val a: Attr) extends AnyVal {

    def ~~>(io: IO[Unit]) =
      a --> io.unsafePerformIO()

    def ~~>[E <: dom.Node](eventHandler: SyntheticEvent[E] => IO[Unit]) =
      a.==>[E](eventHandler(_).unsafePerformIO())
  }

  implicit final class SzRExt_C_SS[S](val u: ComponentScope_SS[S]) extends AnyVal {

    def setStateIO(s: S): IO[Unit] =
      IO(u.setState(s))

    def setStateIO(s: S, callback: IO[Unit]): IO[Unit] =
      IO(u.setState(s, callback.unsafePerformIO()))

    def modStateIO(f: S => S): IO[Unit] =
      IO(u.modState(f))

    def modStateIO(f: S => S, callback: IO[Unit]): IO[Unit] =
      IO(u.modState(f, callback.unsafePerformIO()))

    def runStateIO[M[_]](m: StateT[M, S, Unit])(implicit M: ExecUnsafe[M]): IO[Unit] =
      modStateIO(s => M.execUnsafe(m(s))._1)

    def runStateIO[M[_]](m: StateT[M, S, Unit], callback: IO[Unit])(implicit M: ExecUnsafe[M]): IO[Unit] =
      modStateIO(s => M.execUnsafe(m(s))._1, callback)

    def runStateIOC[M[_]](m: StateT[M, S, Unit])(implicit M: ExecUnsafe[M]): IO[Unit] => IO[Unit] =
      runStateIO(m, _)

    def runStateIOF[I, M[_]](f: I => StateT[M, S, Unit])(implicit M: ExecUnsafe[M]): I => IO[Unit] =
      i => runStateIO(f(i))

    def runStateIOF[I, M[_]](f: I => StateT[M, S, Unit], callback: I => IO[Unit])(implicit M: ExecUnsafe[M]): I => IO[Unit] =
      i => runStateIO(f(i), callback(i))

    def runStateIOF[I, M[_]](f: I => StateT[M, S, Unit], callback: IO[Unit])(implicit M: ExecUnsafe[M]): I => IO[Unit] =
      i => runStateIO(f(i), callback)
  }

  implicit final class SzRExt_C_M(val u: ComponentScope_M) extends AnyVal {
    def forceUpdateIO = IO(u.forceUpdate())
  }

  implicit final class SzRExt_SEvent[N <: dom.Node](val e: SyntheticEvent[N]) extends AnyVal {
    /**
     * Stops the default action of an element from happening.
     * For example: Prevent a submit button from submitting a form Prevent a link from following the URL
     */
    def preventDefaultIO = IO(e.preventDefault())
    /**
     * Stops the bubbling of an event to parent elements, preventing any parent event handlers from being execUnsafeuted.
     */
    def stopPropagationIO = IO(e.stopPropagation())
  }

}