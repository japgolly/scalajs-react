package japgolly.scalajs.react.extra

import scala.runtime.AbstractFunction1

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
sealed abstract class ReusableFn[-A, +B] extends AbstractFunction1[A, B] {
  private[extra] def reusable[AA <: A, BB >: B]: PartialFunction[AA ~=> BB, Boolean]

  def dimap[C, D](f: (A => B) => C => D): C ~=> D =
    ReusableFn(f(this))

  def map[C](f: B => C): A ~=> C =
    dimap(f.compose)

  def contramap[C](f: C => A): C ~=> B =
    dimap(f.andThen)
}

object ReusableFn {

  @inline implicit final class EndoOps[E, B](private val s: (E => E) ~=> B) extends AnyVal {
    def endoCall[I](f: E => I => E): I ~=> B =
      s.dimap(g => i => g(f(_)(i)))

    def endoZoom[I](f: (E, I) => E): I ~=> B =
      s.dimap(g => i => g(f(_, i)))

    def endoCall2[I: Reusability, J](f: E => (I, J) => E): I ~=> (J ~=> B) =
      ReusableFn((i: I, j: J) => s(f(_)(i, j)))

    def endoCall3[I: Reusability, J: Reusability, K](f: E => (I, J, K) => E): I ~=> (J ~=> (K ~=> B)) =
      ReusableFn((i: I, j: J, k: K) => s(f(_)(i, j, k)))
  }

  // ===================================================================================================================

  /**
   * The function itself is reevaluated each time it is used.
   */
  def byName[A, B](f: => (A => B)): A ~=> B =
    ReusableFn[A, B](a => f(a))

  def apply[Y, Z](f: Y => Z): Y ~=> Z =
    new Fn1(f)

  def apply[A: Reusability, Y, Z](f: (A, Y) => Z): A ~=> (Y ~=> Z) =
    new Fn2(f)

  def apply[A: Reusability, B: Reusability, Y, Z](f: (A, B, Y) => Z): A ~=> (B ~=> (Y ~=> Z)) =
    new Fn3(f)

  def apply[A: Reusability, B: Reusability, C: Reusability, Y, Z](f: (A, B, C, Y) => Z): A ~=> (B ~=> (C ~=> (Y ~=> Z))) =
    new Fn4(f)

  def apply[A: Reusability, B: Reusability, C: Reusability, D: Reusability, Y, Z](f: (A, B, C, D, Y) => Z): A ~=> (B ~=> (C ~=> (D ~=> (Y ~=> Z)))) =
    new Fn5(f)

  def apply[A: Reusability, B: Reusability, C: Reusability, D: Reusability, E: Reusability, Y, Z](f: (A, B, C, D, E, Y) => Z): A ~=> (B ~=> (C ~=> (D ~=> (E ~=> (Y ~=> Z))))) =
    new Fn6(f)

//  def renderComponent[P](c: ReactComponentC.ReqProps[P, _, _, TopNode]): P ~=> ReactElement =
//    ReusableFn(c(_: P))

//  @inline def apply[S]($: CompState.WriteAccess[S]) = new CompOps($)

//  final class CompOps[S](private val $: CompState.WriteAccess[S]) extends AnyVal {
//    // These look useless but avoid Scala type-inference issues
//
//    def modState: (S => S) ~=> Callback =
//      ReusableFn($ modState _)
//
//    def setState: S ~=> Callback =
//      ReusableFn($ setState _)
//  }

  implicit def reusability[A, B]: Reusability[ReusableFn[A, B]] =
    Reusability((x, y) => (x eq y) || x.reusable.applyOrElse(y, (_: ReusableFn[A, B]) => false))

  // ===================================================================================================================
  private type R[A] = Reusability[A]

  private class Fn1[-Y, +Z](val f: Y => Z) extends ReusableFn[Y, Z] {
    override def apply(a: Y) = f(a)
    override private[extra] def reusable[I <: Y, O >: Z] = { case x: Fn1[I, O] => f eq x.f }
  }

  private class Fn2[A: R, -Y, +Z](val f: (A, Y) => Z) extends ReusableFn[A, Y ~=> Z] {
    override def apply(a: A) = new Cur1(a, f)
    override private[extra] def reusable[I <: A, O >: (Y ~=> Z)] = { case x: Fn2[I, _, _] => f eq x.f }
  }

  private class Fn3[A: R, B: R, -Y, +Z](val f: (A, B, Y) => Z) extends ReusableFn[A, B ~=> (Y ~=> Z)] {
    private val c2 = cur2(f)
    override def apply(a: A) = new Cur1(a, c2)
    override private[extra] def reusable[I <: A, O >: (B ~=> (Y ~=> Z))] = { case x: Fn3[I, _, _, _] => f eq x.f }
  }

  private class Fn4[A: R, B: R, C: R, -Y, +Z](val f: (A, B, C, Y) => Z) extends ReusableFn[A, B ~=> (C ~=> (Y ~=> Z))] {
    private val c3 = cur3(f)
    private val c2 = cur2(c3)
    override def apply(a: A) = new Cur1(a, c2)
    override private[extra] def reusable[I <: A, O >: (B ~=> (C ~=> (Y ~=> Z)))] = { case x: Fn4[I, _, _, _, _] => f eq x.f }
  }

  private class Fn5[A: R, B: R, C: R, D: R, -Y, +Z](val f: (A, B, C, D, Y) => Z) extends ReusableFn[A, B ~=> (C ~=> (D ~=> (Y ~=> Z)))] {
    private val c4 = cur4(f)
    private val c3 = cur3(c4)
    private val c2 = cur2(c3)
    override def apply(a: A) = new Cur1(a, c2)
    override private[extra] def reusable[I <: A, O >: (B ~=> (C ~=> (D ~=> (Y ~=> Z))))] = { case x: Fn5[I, _, _, _, _, _] => f eq x.f }
  }

  private class Fn6[A: R, B: R, C: R, D: R, E: R, -Y, +Z](val f: (A, B, C, D, E, Y) => Z) extends ReusableFn[A, B ~=> (C ~=> (D ~=> (E ~=> (Y ~=> Z))))] {
    private val c5 = cur5(f)
    private val c4 = cur4(c5)
    private val c3 = cur3(c4)
    private val c2 = cur2(c3)
    override def apply(a: A) = new Cur1(a, c2)
    override private[extra] def reusable[I <: A, O >: (B ~=> (C ~=> (D ~=> (E ~=> (Y ~=> Z)))))] = { case x: Fn6[I, _, _, _, _, _, _] => f eq x.f }
  }

  private def cur2[A:R, B:R,                Y, Z](f: (A,B,      Y) => Z): (A,B      ) => (Y ~=> Z) = new Cur2(_,_,      f)
  private def cur3[A:R, B:R, C:R,           Y, Z](f: (A,B,C,    Y) => Z): (A,B,C    ) => (Y ~=> Z) = new Cur3(_,_,_,    f)
  private def cur4[A:R, B:R, C:R, D:R,      Y, Z](f: (A,B,C,D,  Y) => Z): (A,B,C,D  ) => (Y ~=> Z) = new Cur4(_,_,_,_,  f)
  private def cur5[A:R, B:R, C:R, D:R, E:R, Y, Z](f: (A,B,C,D,E,Y) => Z): (A,B,C,D,E) => (Y ~=> Z) = new Cur5(_,_,_,_,_,f)

  private class Cur1[A: R, -Y, +Z](val a: A, val f: (A, Y) => Z) extends ReusableFn[Y, Z] {
    override def apply(y: Y): Z = f(a, y)
    override private[extra] def reusable[I <: Y, O >: Z] = { case x: Cur1[A, _, _] => (f eq x.f) && (a ~=~ x.a) }
  }

  private class Cur2[A: R, B: R, -Y, +Z](val a: A, val b: B, val f: (A, B, Y) => Z) extends ReusableFn[Y, Z] {
    override def apply(y: Y): Z = f(a, b, y)
    override private[extra] def reusable[I <: Y, O >: Z] = { case x: Cur2[A, B, _, _] => (f eq x.f) && (a ~=~ x.a) && (b ~=~ x.b) }
  }

  private class Cur3[A: R, B: R, C: R, -Y, +Z](val a: A, val b: B, val c: C, val f: (A, B, C, Y) => Z) extends ReusableFn[Y, Z] {
    override def apply(y: Y): Z = f(a, b, c, y)
    override private[extra] def reusable[I <: Y, O >: Z] = { case x: Cur3[A, B, C, _, _] => (f eq x.f) && (a ~=~ x.a) && (b ~=~ x.b) && (c ~=~ x.c) }
  }

  private class Cur4[A: R, B: R, C: R, D: R, -Y, +Z](val a: A, val b: B, val c: C, val d: D, val f: (A, B, C, D, Y) => Z) extends ReusableFn[Y, Z] {
    override def apply(y: Y): Z = f(a, b, c, d, y)
    override private[extra] def reusable[I <: Y, O >: Z] = { case x: Cur4[A, B, C, D, _, _] => (f eq x.f) && (a ~=~ x.a) && (b ~=~ x.b) && (c ~=~ x.c) && (d ~=~ x.d) }
  }

  private class Cur5[A: R, B: R, C: R, D: R, E: R, -Y, +Z](val a: A, val b: B, val c: C, val d: D, val e: E, val f: (A, B, C, D, E, Y) => Z) extends ReusableFn[Y, Z] {
    override def apply(y: Y): Z = f(a, b, c, d, e, y)
    override private[extra] def reusable[I <: Y, O >: Z] = { case x: Cur5[A, B, C, D, E, _, _] => (f eq x.f) && (a ~=~ x.a) && (b ~=~ x.b) && (c ~=~ x.c) && (d ~=~ x.d) && (e ~=~ x.e) }
  }
}
