package japgolly.scalajs.react.extra

/**
 * A value explicitly being marked as reusable.
 *
 * Usually reusability is determined by type (ie. via an implicit `Reusability[A]` available for an `A`).
 * Instead, this type promises that reusability will be explicitly provided with each value.
 */
class ReusableVal[A](val value: A, val reusability: Reusability[A]) {
  override def toString = s"ReusableVal($value, $reusability)"
  override def hashCode = value.##
  override def equals(o: Any) = o match {
    case t: ReusableVal[A] => value == t.value
    case _                 => false
  }
}

object ReusableVal {

  def apply[A](value: A)(reusability: Reusability[A]): ReusableVal[A] =
    new ReusableVal(value, reusability)

  def byRef[A <: AnyRef](value: A): ReusableVal[A] =
    new ReusableVal(value, Reusability.byRef)

  implicit def reusability[A]: Reusability[ReusableVal[A]] =
    Reusability.fn((a, b) => a.reusability.test(a, b) && b.reusability.test(a, b))

  @inline implicit def autoValue[A](r: ReusableVal[A]): A =
    r.value
}