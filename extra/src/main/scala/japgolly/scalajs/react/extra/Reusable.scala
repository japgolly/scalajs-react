package japgolly.scalajs.react.extra

import scala.reflect.ClassTag

final class Reusable[+A] private[Reusable](valueFn: () => A,
                                           private[Reusable] val root: Any,
                                           val isReusable: Reusable[Any] => Boolean) {
  override def toString = s"Reusable($value)"
  override def hashCode = value.##

  def value: A =
    valueFn()

  /** WARNING: This does not affect reusability.
    * Only the initial (pre-mapped) values matter when considering reusability.
    *
    * If you have two reusable values and map them differently, even though the mapped values differ they will still be
    * considered reusable. Any differences as a result of second the mapping will be discarded.
    */
  def map[B](f: A => B): Reusable[B] = {
    lazy val b = f(valueFn())
    new Reusable[B](() => b, root, isReusable)
  }
}

object Reusable {

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

  def const[A](a: A, reuse: Boolean): Reusable[A] =
    root(a, _ => reuse)

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

  /** Compare using universal equality (Scala's == operator). */
  def by_==[A](a: A): Reusable[A] =
    root(a, a == _.root)

  /** Compare by reference and if different, compare using universal equality (Scala's == operator). */
  def byRefOr_==[A <: AnyRef](a: A): Reusable[A] =
    root(a, _.root match {
      case b: AnyRef => (a eq b) || (a == b)
      case _         => false
    })

  private[this] val reusabilityInstance =
    Reusability[Reusable[Any]]((x, y) => x.isReusable(y) && y.isReusable(x))

  implicit def reusableReusability[A]: Reusability[Reusable[A]] =
    reusabilityInstance.narrow
}

