package japgolly.scalajs.react.extra

import scala.runtime.AbstractFunction1
import scalaz.effect.IO
import japgolly.scalajs.react._
import japgolly.scalajs.react.ScalazReact._

/**
 * A function that facilitates stability and reuse.
 *
 * In effective usage of React, callbacks are passed around as component properties.
 * Due to the ease of function creation in Scala if it often the case that functions are created inline and thus
 * provide no means of determining whether a component can safely skip its update.
 * This class exists as a solution.
 *
 * @since 0.9.0
 */
trait ReusableFn[A, B] extends AbstractFunction1[A, B] {
  def reusable: PartialFunction[ReusableFn[A, B], Boolean]

  import scalaz.Leibniz._

  def asVar(value: A)(implicit r: Reusable[A], ev: B === IO[Unit]): ReusableVar[A] =
    new ReusableVar(value, ev.subst[({type λ[X] = A ~=> X})#λ](this))(r)

  def asVarR(value: A, r: Reusable[A])(implicit ev: B === IO[Unit]): ReusableVar[A] =
    asVar(value)(r, ev)
}

object ReusableFn {
  def apply[A, Z](f: A => Z): Fn1[A, Z] =
    new Fn1(f)

  def apply[A: Reusable, B, Z](f: (A, B) => Z): Fn2[A, B, Z] =
    new Fn2(f)

  def modState[C[_]: CompStateAccess, S, A]($: C[S])(f: S => A => S): A ~=> Unit =
    ReusableFn(a => $.modState(s => f(s)(a)))

  def modStateIO[C[_]: CompStateAccess, S, A]($: C[S])(f: S => A => S): A ~=> IO[Unit] =
    ReusableFn(a => $.modStateIO(s => f(s)(a)))

  // ===================================================================================================================

  implicit def reusability[A, B]: Reusable[ReusableFn[A, B]] =
    Reusable.fn((x, y) => x.reusable.applyOrElse(y, (_: ReusableFn[A, B]) => false))

  final class Fn1[A, Z](val f: A => Z) extends ReusableFn[A, Z] {
    override def apply(a: A) = f(a)
    override def reusable = { case x: Fn1[A, Z] => f eq x.f }
  }

  final class Fn2[A: Reusable, Y, Z](val f: (A, Y) => Z) extends ReusableFn[A, Cur1[A, Y, Z]] {
    override def apply(a: A) = new Cur1(a, f)
    override def reusable = { case x: Fn2[A, Y, Z] => f eq x.f }
  }

  final class Fn3[A: Reusable, B: Reusable, Y, Z](val f: (A, B, Y) => Z) extends ReusableFn[A, Cur1[A, B, Cur2[A, B, Y, Z]]] {
    val g = (a: A, b: B) => new Cur2(a, b, f)
    override def apply(a: A) = new Cur1(a, g)
    override def reusable = { case x: Fn3[A, B, Y, Z] => f eq x.f }
  }

  final class Cur1[A: Reusable, Y, Z](val a: A, val f: (A, Y) => Z) extends ReusableFn[Y, Z] {
    override def apply(y: Y): Z = f(a, y)
    override def reusable = { case x: Cur1[A, Y, Z] => (f eq x.f) && (a ~=~ x.a) }
  }

  final class Cur2[A: Reusable, B: Reusable, Y, Z](val a: A, val b: B, val f: (A, B, Y) => Z) extends ReusableFn[Y, Z] {
    override def apply(y: Y): Z = f(a, b, y)
    override def reusable = { case x: Cur2[A, B, Y, Z] => (f eq x.f) && (a ~=~ x.a) && (b ~=~ x.b) }
  }

//  final class Cur3[A: Reusable, B: Reusable, C: Reusable, Y, Z](val a: A, val b: B, val c: C, val f: (A, B, C, Y) => Z) extends ReusableFn[Y, Z] {
//    override def apply(y: Y): Z = f(a, b, c, y)
//    override def reusable = { case x: Cur3[A, B, C, Y, Z] => (f eq x.f) && (a ~=~ x.a) && (b ~=~ x.b) && (c ~=~ x.c) }
//  }
}
