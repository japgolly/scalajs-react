package japgolly.scalajs.react

import scala.reflect.ClassTag

/**
  * A value that has been explicitly paired with a (potentially ad-hoc) [[Reusability]] instance.
  *
  * @tparam A The type of value.
  * @since 1.0.0
  */
final class Reusable[+A] private[Reusable](lazyValue: () => A,
                                           private[Reusable] val root: Any,
                                           val isReusable: Reusable[Any] => Boolean) {
  override def toString = s"Reusable($value)"
  override def hashCode = value.##

  def value: A =
    // This always returns the same value and is actually referentially transparent and safe.
    // Doing this avoids applying modifications from .map in the event that this is reusable and
    // the value is never read.
    lazyValue()

  /** WARNING: This does not affect reusability.
    * Only the initial (pre-mapped) values matter when considering reusability.
    *
    * If you have two reusable values and map them differently, even though the mapped values differ they will still be
    * considered reusable. Any differences as a result of second the mapping will be discarded.
    */
  def map[B](f: A => B): Reusable[B] = {
    lazy val b = f(lazyValue())
    new Reusable[B](() => b, root, isReusable)
  }

  def withValue[B](b: B): Reusable[B] =
    new Reusable[B](() => b, root, isReusable)

  def withLazyValue[B](b: => B): Reusable[B] =
    map(_ => b)

  /** Create a new `Reusable[B]` that is reusable so long as this `Reusable[A]` and the `Reusable[A => B]` are. */
  def ap[B](rf: Reusable[A => B]): Reusable[B] =
    Reusable.ap(this, rf)((a, f) => f(a))

  /** Create a `Reusable[(A, B)]` that is reusable so long as this `Reusable[A]` and the `Reusable[B]` are. */
  def tuple[B](rb: Reusable[B]): Reusable[(A, B)] =
    Reusable.ap(this, rb)((_, _))
}

object Reusable {

  @inline implicit def autoValue[A](r: Reusable[A]): A =
    r.value

  private def root[A](a: A, isReusable: Reusable[Any] => Boolean): Reusable[A] =
    new Reusable[A](() => a, a, isReusable)

  def apply[A: ClassTag](a: A)(reuse: (A, A) => Boolean): Reusable[A] =
    root(a, _.root match {
      case b: A => reuse(a, b)
      case _    => false
    })

  def implicitly[A: ClassTag : Reusability](a: A): Reusable[A] =
    explicitly(a)(Predef.implicitly)

  def explicitly[A: ClassTag](a: A)(r: Reusability[A]): Reusable[A] =
    apply(a)(r.test)

  /** Use constant reusability (i.e. always-reuse or never-reuse) */
  def const[A](a: A, isReusable: Boolean): Reusable[A] =
    root(a, _ => isReusable)

  def always[A](a: A): Reusable[A] =
    const(a, true)

  def never[A](a: A): Reusable[A] =
    const(a, false)

  /** Compare by reference. Reuse if both values are the same instance. */
  def byRef[A <: AnyRef](a: A): Reusable[A] =
    root(a, _.root match {
      case b: AnyRef => a eq b
      case _         => false
    })

  /** Compare by reference through an isomorphism. Reuse if both values are the same instance. */
  def byRefIso[A, B <: AnyRef](a: A)(iso: A => B): Reusable[A] = {
    val b = iso(a)
    new Reusable[A](() => a, b, _.root match {
      case x: AnyRef => b eq x
      case _         => false
    })
  }

  /** Compare using universal equality (Scala's == operator). */
  def by_==[A](a: A): Reusable[A] =
    root(a, a == _.root)

  /** Compare by reference and if different, compare using universal equality (Scala's == operator). */
  def byRefOr_==[A <: AnyRef](a: A): Reusable[A] =
    root(a, _.root match {
      case b: AnyRef => (a eq b) || (a == b)
      case _         => false
    })

  /** Create a new `Reusable[C]` that is reusable so long as `Reusable[A]` and `Reusable[B]` are. */
  def ap[A, B, C](ra: Reusable[A], rb: Reusable[B])(f: (A, B) => C): Reusable[C] =
    implicitly((ra, rb)).map(x => f(x._1, x._2))

  private[this] val reusabilityInstance =
    Reusability[Reusable[Any]]((x, y) => x.isReusable(y) && y.isReusable(x))

  implicit def reusableReusability[A]: Reusability[Reusable[A]] =
    reusabilityInstance.narrow

  // ===================================================================================================================

  def callbackByRef[A](c: CallbackTo[A]): Reusable[CallbackTo[A]] =
    byRefIso(c)(_.underlyingRepr)

  def callbackOptionByRef[A](c: CallbackOption[A]): Reusable[CallbackOption[A]] =
    byRefIso(c)(_.underlyingRepr)

  /**
   * A function that facilitates stability and reuse.
   *
   * In effective usage of React, callbacks are passed around as component properties.
   * Due to the ease of function creation in Scala it is often the case that functions are created inline and thus
   * provide no means of determining whether a component can safely skip its update.
   * This exists as a solution.
   *
   * @since 0.9.0
   */
  object fn {
    import FnInternals._

    def apply[Y, Z](f: Y => Z): Y ~=> Z =
      Reusable.implicitly(new Fn1(f))

    def apply[A: Reusability, Y, Z](f: (A, Y) => Z): A ~=> (Y ~=> Z) =
      Reusable.implicitly(new Fn2(f))

    def apply[A: Reusability, B: Reusability, Y, Z](f: (A, B, Y) => Z): A ~=> (B ~=> (Y ~=> Z)) =
      Reusable.implicitly(new Fn3(f))

    def apply[A: Reusability, B: Reusability, C: Reusability, Y, Z](f: (A, B, C, Y) => Z): A ~=> (B ~=> (C ~=> (Y ~=> Z))) =
      Reusable.implicitly(new Fn4(f))

    def apply[A: Reusability, B: Reusability, C: Reusability, D: Reusability, Y, Z](f: (A, B, C, D, Y) => Z): A ~=> (B ~=> (C ~=> (D ~=> (Y ~=> Z)))) =
      Reusable.implicitly(new Fn5(f))

    def apply[A: Reusability, B: Reusability, C: Reusability, D: Reusability, E: Reusability, Y, Z](f: (A, B, C, D, E, Y) => Z): A ~=> (B ~=> (C ~=> (D ~=> (E ~=> (Y ~=> Z))))) =
      Reusable.implicitly(new Fn6(f))

    def state[I, S](i: I)(implicit t: StateAccessor.WritePure[I, S]) = new StateAccessWriteOps(i)(t)
    final class StateAccessWriteOps[I, S](i: I)(implicit t: StateAccessor.WritePure[I, S]) {

      def setStateFn: Reusable[SetStateFnPure[S]] =
        Reusable.byRef(t(i).toSetStateFn)

      def modStateFn: Reusable[ModStateFnPure[S]] =
        Reusable.byRef(t(i).toModStateFn)

      def mod: (S => S) ~=> Callback =
        Reusable.fn(t(i).modState)

      def modOption: (S => Option[S]) ~=> Callback =
        Reusable.fn(t(i).modStateOption)

      def set: S ~=> Callback =
        Reusable.fn(t(i).setState)

      def setOption: Option[S] ~=> Callback =
        Reusable.fn(t(i).setStateOption)

      def modCB: Reusable[((S => S), Callback) => Callback] =
        Reusable.byRef(t(i).modState)

      def modOptionCB: Reusable[((S => Option[S]), Callback) => Callback] =
        Reusable.byRef(t(i).modStateOption)

      def setCB: Reusable[(S, Callback) => Callback] =
        Reusable.byRef(t(i).setState)

      def setOptionCB: Reusable[(Option[S], Callback) => Callback] =
        Reusable.byRef(t(i).setStateOption)
    }
  }

  // ===================================================================================================================

  private object FnInternals {
    private type R[A] = Reusability[A]
    type ReusableFn[-A, +B] = scala.runtime.AbstractFunction1[A, B]

    // -------------------------------------------------------------------------

    class Fn1[-Y, +Z](val f: Y => Z) extends ReusableFn[Y, Z] {
      override def apply(a: Y) = f(a)
    }

    private val _reusabilityFn1: Reusability[Fn1[_, _]] =
      Reusability((x, y) => (x eq y) || (x.f eq y.f))

    implicit def reusabilityFn1[Y, Z]: Reusability[Fn1[Y, Z]] =
      _reusabilityFn1.narrow

    // -------------------------------------------------------------------------

    class Fn2[A: R, -Y, +Z](val f: (A, Y) => Z) extends ReusableFn[A, Y ~=> Z] {
      override def apply(a: A) = Reusable.implicitly(new Cur2(a, f))
    }

    class Cur2[A, -Y, +Z](val a: A, val f: (A, Y) => Z) extends ReusableFn[Y, Z] {
      override def apply(y: Y): Z = f(a, y)
    }

    private val _reusabilityFn2: Reusability[Fn2[_, _, _]] =
      Reusability((x, y) => (x eq y) || (x.f eq y.f))

    implicit def reusabilityFn2[A, Y, Z]: Reusability[Fn2[A, Y, Z]] =
      _reusabilityFn2.narrow

    implicit def reusabilityCur2[A: R, Y, Z]: Reusability[Cur2[A, Y, Z]] =
      Reusability((x, y) => (x eq y) || ((x.f eq y.f) && (x.a ~=~ y.a)))

    // -------------------------------------------------------------------------

    class Fn3[A: R, B: R, -Y, +Z](val f: (A, B, Y) => Z) extends ReusableFn[A, B ~=> (Y ~=> Z)] {
      private val c2 = cur3(f)
      override def apply(a: A) = Reusable.implicitly(new Cur2(a, c2))
    }

    def cur3[A: R, B: R, Y, Z](f: (A, B, Y) => Z): (A, B) => (Y ~=> Z) =
      (a, b) => Reusable.implicitly(new Cur3(a, b, f))

    class Cur3[A, B, -Y, +Z](val a: A, val b: B, val f: (A, B, Y) => Z) extends ReusableFn[Y, Z] {
      override def apply(y: Y): Z = f(a, b, y)
    }

    private val _reusabilityFn3: Reusability[Fn3[_, _, _, _]] =
      Reusability((x, y) => (x eq y) || (x.f eq y.f))

    implicit def reusabilityFn3[A, B, Y, Z]: Reusability[Fn3[A, B, Y, Z]] =
      _reusabilityFn3.narrow

    implicit def reusabilityCur3[A: R, B: R, Y, Z]: Reusability[Cur3[A, B, Y, Z]] =
      Reusability((x, y) => (x eq y) || ((x.f eq y.f) && (x.a ~=~ y.a) && (x.b ~=~ y.b)))

    // -------------------------------------------------------------------------

    class Fn4[A: R, B: R, C: R, -Y, +Z](val f: (A, B, C, Y) => Z) extends ReusableFn[A, B ~=> (C ~=> (Y ~=> Z))] {
      private val c3 = cur4(f)
      private val c2 = cur3(c3)
      override def apply(a: A) = Reusable.implicitly(new Cur2(a, c2))
    }

    def cur4[A: R, B: R, C: R, Y, Z](f: (A, B, C, Y) => Z): (A, B, C) => (Y ~=> Z) =
      (a, b, c) => Reusable.implicitly(new Cur4(a, b, c, f))

    class Cur4[A, B, C, -Y, +Z](val a: A, val b: B, val c: C, val f: (A, B, C, Y) => Z) extends ReusableFn[Y, Z] {
      override def apply(y: Y): Z = f(a, b, c, y)
    }

    implicit def reusabilityFn4[A, B, C, Y, Z]: Reusability[Fn4[A, B, C, Y, Z]] =
      Reusability((x, y) => (x eq y) || (x.f eq y.f))

    implicit def reusabilityCur4[A: R, B: R, C: R, Y, Z]: Reusability[Cur4[A, B, C, Y, Z]] =
      Reusability((x, y) => (x eq y) || ((x.f eq y.f) && (x.a ~=~ y.a) && (x.b ~=~ y.b) && (x.c ~=~ y.c)))

    // -------------------------------------------------------------------------

    class Fn5[A: R, B: R, C: R, D: R, -Y, +Z](val f: (A, B, C, D, Y) => Z) extends ReusableFn[A, B ~=> (C ~=> (D ~=> (Y ~=> Z)))] {
      private val c4 = cur5(f)
      private val c3 = cur4(c4)
      private val c2 = cur3(c3)
      override def apply(a: A) = Reusable.implicitly(new Cur2(a, c2))
    }

    def cur5[A: R, B: R, C: R, D: R, Y, Z](f: (A, B, C, D, Y) => Z): (A, B, C, D) => (Y ~=> Z) =
      (a, b, c, d) => Reusable.implicitly(new Cur5(a, b, c, d, f))

    class Cur5[A, B, C, D, -Y, +Z](val a: A, val b: B, val c: C, val d: D, val f: (A, B, C, D, Y) => Z) extends ReusableFn[Y, Z] {
      override def apply(y: Y): Z = f(a, b, c, d, y)
    }

    implicit def reusabilityFn5[A, B, C, D, Y, Z]: Reusability[Fn5[A, B, C, D, Y, Z]] =
      Reusability((x, y) => (x eq y) || (x.f eq y.f))

    implicit def reusabilityCur5[A: R, B: R, C: R, D: R, Y, Z]: Reusability[Cur5[A, B, C, D, Y, Z]] =
      Reusability((x, y) => (x eq y) || ((x.f eq y.f) && (x.a ~=~ y.a) && (x.b ~=~ y.b) && (x.c ~=~ y.c) && (x.d ~=~ y.d)))

    // -------------------------------------------------------------------------

    class Fn6[A: R, B: R, C: R, D: R, E: R, -Y, +Z](val f: (A, B, C, D, E, Y) => Z) extends ReusableFn[A, B ~=> (C ~=> (D ~=> (E ~=> (Y ~=> Z))))] {
      private val c5 = cur6(f)
      private val c4 = cur5(c5)
      private val c3 = cur4(c4)
      private val c2 = cur3(c3)
      override def apply(a: A) = Reusable.implicitly(new Cur2(a, c2))
    }

    def cur6[A: R, B: R, C: R, D: R, E: R, Y, Z](f: (A, B, C, D, E, Y) => Z): (A, B, C, D, E) => (Y ~=> Z) =
      (a, b, c, d, e) => Reusable.implicitly(new Cur6(a, b, c, d, e, f))

    class Cur6[A, B, C, D, E, -Y, +Z](val a: A, val b: B, val c: C, val d: D, val e: E, val f: (A, B, C, D, E, Y) => Z) extends ReusableFn[Y, Z] {
      override def apply(y: Y): Z = f(a, b, c, d, e, y)
    }

    implicit def reusabilityFn6[A, B, C, D, E, Y, Z]: Reusability[Fn6[A, B, C, D, E, Y, Z]] =
      Reusability((x, y) => (x eq y) || (x.f eq y.f))

    implicit def reusabilityCur6[A: R, B: R, C: R, D: R, E: R, Y, Z]: Reusability[Cur6[A, B, C, D, E, Y, Z]] =
      Reusability((x, y) => (x eq y) || ((x.f eq y.f) && (x.a ~=~ y.a) && (x.b ~=~ y.b) && (x.c ~=~ y.c) && (x.d ~=~ y.d) && (x.e ~=~ y.e)))
  }
}
