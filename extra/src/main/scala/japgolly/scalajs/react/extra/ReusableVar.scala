package japgolly.scalajs.react.extra

import monocle._
import scalaz.effect.IO
import japgolly.scalajs.react._, ScalazReact._

/**
 * Reusable version of [[ExternalVar]].
 */
final class ReusableVar[A](val value: A, val set: A ~=> IO[Unit])(implicit val reusability: Reusability[A]) {

  override def toString =
    s"ReusableVar($value, $set)"

  def mod(f: A => A): IO[Unit] =
    set(f(value))

  def setL[B](l: Lens[A, B]): B => IO[Unit] =
    b => set(l.set(b)(value))

  def modL[B](l: Lens[A, B])(f: B => B): IO[Unit] =
    set(l.modify(f)(value))

  // Zoom is dangerously deceptive here as it appears to work but will often override the non-zoomed subset of A's state.
  // Use the zoom methods on ComponentScopes directly for a reliable function.
  //
  // def zoomL[B: Reusability](l: Lens[A, B]): ReusableVar[B] =
  //   ReusableVar(l get value)(set.dimap(s => b => s(l.set(b)(value))))
  //
  // def extZoomL[B](l: Lens[A, B]): ExternalVar[B] =
  //   ExternalVar(l get value)(b => set(l.set(b)(value)))

  def toExternalVar: ExternalVar[A] =
    ExternalVar(value)(set)
}

object ReusableVar {
  @inline def apply[A: Reusability](value: A)(set: A ~=> IO[Unit]): ReusableVar[A] =
    new ReusableVar(value, set)

  @inline def state[S: Reusability]($: CompStateFocus[S]): ReusableVar[S] =
    new ReusableVar($.state, ReusableFn($).setStateIO)

  implicit def reusability[A]: Reusability[ReusableVar[A]] =
    Reusability.fn((a, b) => (a.set ~=~ b.set) && b.reusability.test(a.value, b.value))
}
