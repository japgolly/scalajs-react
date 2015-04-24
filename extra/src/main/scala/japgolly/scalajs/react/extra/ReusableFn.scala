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
  private[extra] def reusable: PartialFunction[ReusableFn[A, B], Boolean]

  import scalaz.Leibniz._

  def asVar(value: A)(implicit r: Reusable[A], ev: B === IO[Unit]): ReusableVar[A] =
    new ReusableVar(value, ev.subst[({type λ[X] = A ~=> X})#λ](this))(r)

  def asVarR(value: A, r: Reusable[A])(implicit ev: B === IO[Unit]): ReusableVar[A] =
    asVar(value)(r, ev)
}

object ReusableFn {

  @inline def apply[Y, Z](f: Y => Z): Fn1[Y, Z] =
    new Fn1(f)

  @inline def apply[A: Reusable, Y, Z](f: (A, Y) => Z): Fn2[A, Y, Z] =
    new Fn2(f)

  @inline def apply[A: Reusable, B: Reusable, Y, Z](f: (A, B, Y) => Z): Fn3[A, B, Y, Z] =
    new Fn3(f)

  @inline def apply[A: Reusable, B: Reusable, C: Reusable, Y, Z](f: (A, B, C, Y) => Z): Fn4[A, B, C, Y, Z] =
    new Fn4(f)

  @inline def apply[A: Reusable, B: Reusable, C: Reusable, D: Reusable, Y, Z](f: (A, B, C, D, Y) => Z): Fn5[A, B, C, D, Y, Z] =
    new Fn5(f)

  @inline def apply[A: Reusable, B: Reusable, C: Reusable, D: Reusable, E: Reusable, Y, Z](f: (A, B, C, D, E, Y) => Z): Fn6[A, B, C, D, E, Y, Z] =
    new Fn6(f)

  def modState[C[_]: CompStateAccess, S, A]($: C[S])(f: S => A => S): A ~=> Unit =
    ReusableFn(a => $.modState(s => f(s)(a)))

  def modStateIO[C[_]: CompStateAccess, S, A]($: C[S])(f: S => A => S): A ~=> IO[Unit] =
    ReusableFn(a => $.modStateIO(s => f(s)(a)))

  // ===================================================================================================================

  implicit def reusability[A, B]: Reusable[ReusableFn[A, B]] =
    Reusable.fn((x, y) => x.reusable.applyOrElse(y, (_: ReusableFn[A, B]) => false))

  final class Fn1[Y, Z](val f: Y => Z) extends ReusableFn[Y, Z] {
    override def apply(a: Y) = f(a)
    override private[extra] def reusable = { case x: Fn1[Y, Z] => f eq x.f }
  }

  final class Fn2[A: Reusable, Y, Z](val f: (A, Y) => Z) extends ReusableFn[A, Cur1[A, Y, Z]] {
    override def apply(a: A) = new Cur1(a, f)
    override private[extra] def reusable = { case x: Fn2[A, Y, Z] => f eq x.f }
  }

  final class Fn3[A: Reusable, B: Reusable, Y, Z](val f: (A, B, Y) => Z) extends ReusableFn[A, Cur12[A, B, Y, Z]] {
    private val c2 = (a: A, b: B) => new Cur2(a, b, f)
    override def apply(a: A) = new Cur1(a, c2)
    override private[extra] def reusable = { case x: Fn3[A, B, Y, Z] => f eq x.f }
  }

  final class Fn4[A: Reusable, B: Reusable, C: Reusable, Y, Z](val f: (A, B, C, Y) => Z) extends ReusableFn[A, Cur13[A, B, C, Y, Z]] {
    private val c3 = (a: A, b: B, c: C) => new Cur3(a, b, c, f)
    private val c2 = (a: A, b: B      ) => new Cur2(a, b,    c3)
    override def apply(a: A) = new Cur1(a, c2)
    override private[extra] def reusable = { case x: Fn4[A, B, C, Y, Z] => f eq x.f }
  }

  final class Fn5[A: Reusable, B: Reusable, C: Reusable, D: Reusable, Y, Z](val f: (A, B, C, D, Y) => Z) extends ReusableFn[A, Cur14[A, B, C, D, Y, Z]] {
    private val c4 = (a: A, b: B, c: C, d: D) => new Cur4(a, b, c, d, f)
    private val c3 = (a: A, b: B, c: C      ) => new Cur3(a, b, c,    c4)
    private val c2 = (a: A, b: B            ) => new Cur2(a, b,       c3)
    override def apply(a: A) = new Cur1(a, c2)
    override private[extra] def reusable = { case x: Fn5[A, B, C, D, Y, Z] => f eq x.f }
  }

  final class Fn6[A: Reusable, B: Reusable, C: Reusable, D: Reusable, E: Reusable, Y, Z](val f: (A, B, C, D, E, Y) => Z) extends ReusableFn[A, Cur15[A, B, C, D, E, Y, Z]] {
    private val c5 = (a: A, b: B, c: C, d: D, e: E) => new Cur5(a, b, c, d, e, f)
    private val c4 = (a: A, b: B, c: C, d: D      ) => new Cur4(a, b, c, d,    c5)
    private val c3 = (a: A, b: B, c: C            ) => new Cur3(a, b, c,       c4)
    private val c2 = (a: A, b: B                  ) => new Cur2(a, b,          c3)
    override def apply(a: A) = new Cur1(a, c2)
    override private[extra] def reusable = { case x: Fn6[A, B, C, D, E, Y, Z] => f eq x.f }
  }

  type Cur12[A, B, Y, Z] = Cur1[A, B, Cur2[A, B, Y, Z]]

  type Cur13[A, B, C, Y, Z] = Cur1[A, B,    Cur23[A, B, C, Y, Z]]
  type Cur23[A, B, C, Y, Z] = Cur2[A, B, C, Cur3 [A, B, C, Y, Z]]

  type Cur14[A, B, C, D, Y, Z] = Cur1[A, B,       Cur24[A, B, C, D, Y, Z]]
  type Cur24[A, B, C, D, Y, Z] = Cur2[A, B, C,    Cur34[A, B, C, D, Y, Z]]
  type Cur34[A, B, C, D, Y, Z] = Cur3[A, B, C, D, Cur4 [A, B, C, D, Y, Z]]

  type Cur15[A, B, C, D, E, Y, Z] = Cur1[A, B,          Cur25[A, B, C, D, E, Y, Z]]
  type Cur25[A, B, C, D, E, Y, Z] = Cur2[A, B, C,       Cur35[A, B, C, D, E, Y, Z]]
  type Cur35[A, B, C, D, E, Y, Z] = Cur3[A, B, C, D,    Cur45[A, B, C, D, E, Y, Z]]
  type Cur45[A, B, C, D, E, Y, Z] = Cur4[A, B, C, D, E, Cur5 [A, B, C, D, E, Y, Z]]

  final class Cur1[A: Reusable, Y, Z](val a: A, val f: (A, Y) => Z) extends ReusableFn[Y, Z] {
    override def apply(y: Y): Z = f(a, y)
    override private[extra] def reusable = { case x: Cur1[A, Y, Z] => (f eq x.f) && (a ~=~ x.a) }
  }

  final class Cur2[A: Reusable, B: Reusable, Y, Z](val a: A, val b: B, val f: (A, B, Y) => Z) extends ReusableFn[Y, Z] {
    override def apply(y: Y): Z = f(a, b, y)
    override private[extra] def reusable = { case x: Cur2[A, B, Y, Z] => (f eq x.f) && (a ~=~ x.a) && (b ~=~ x.b) }
  }

  final class Cur3[A: Reusable, B: Reusable, C: Reusable, Y, Z](val a: A, val b: B, val c: C, val f: (A, B, C, Y) => Z) extends ReusableFn[Y, Z] {
    override def apply(y: Y): Z = f(a, b, c, y)
    override private[extra] def reusable = { case x: Cur3[A, B, C, Y, Z] => (f eq x.f) && (a ~=~ x.a) && (b ~=~ x.b) && (c ~=~ x.c) }
  }

  final class Cur4[A: Reusable, B: Reusable, C: Reusable, D: Reusable, Y, Z](val a: A, val b: B, val c: C, val d: D, val f: (A, B, C, D, Y) => Z) extends ReusableFn[Y, Z] {
    override def apply(y: Y): Z = f(a, b, c, d, y)
    override private[extra] def reusable = { case x: Cur4[A, B, C, D, Y, Z] => (f eq x.f) && (a ~=~ x.a) && (b ~=~ x.b) && (c ~=~ x.c) && (d ~=~ x.d) }
  }

  final class Cur5[A: Reusable, B: Reusable, C: Reusable, D: Reusable, E: Reusable, Y, Z](val a: A, val b: B, val c: C, val d: D, val e: E, val f: (A, B, C, D, E, Y) => Z) extends ReusableFn[Y, Z] {
    override def apply(y: Y): Z = f(a, b, c, d, e, y)
    override private[extra] def reusable = { case x: Cur5[A, B, C, D, E, Y, Z] => (f eq x.f) && (a ~=~ x.a) && (b ~=~ x.b) && (c ~=~ x.c) && (d ~=~ x.d) && (e ~=~ x.e) }
  }
}
