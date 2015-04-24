package japgolly.scalajs.react.extra

import monocle.Lens
import scalaz.effect.IO
import japgolly.scalajs.react._, ScalazReact._

/**
 * Provides a component with safe R/W access to an external variable.
 *
 * Use [[ReusableVar]] for a [[Reusable]] version of this.
 */
final class ExternalVar[A](val value: A, val set: A => IO[Unit]) {

  def mod(f: A => A): IO[Unit] =
    set(f(value))

  def setL[B](l: Lens[A, B]): B => IO[Unit] =
    b => set(l.set(b)(value))

  def modL[B](l: Lens[A, B])(f: B => B): IO[Unit] =
    set(l.modify(f)(value))
}


object ExternalVar {
  @inline def apply[A](value: A)(set: A => IO[Unit]): ExternalVar[A] =
    new ExternalVar(value, set)

  @inline def state[S]($: ComponentStateFocus[S]): ExternalVar[S] =
    new ExternalVar($.state, $.setStateIO(_))
}
