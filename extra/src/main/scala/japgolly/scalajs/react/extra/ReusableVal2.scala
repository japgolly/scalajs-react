package japgolly.scalajs.react.extra

class ReusableVal2[A, S](a: () => A, val src: S)(implicit val reusability: Reusability[S]) {
  lazy val value: A = a()
}

object ReusableVal2 {
  implicit def reusability[A, S]: Reusability[ReusableVal2[A, S]] =
    Reusability.internal((_: ReusableVal2[A, S]).src)(_.reusability)

  implicit def autoValue[A, B](r: ReusableVal2[A, B]): A =
    r.value

  @inline def apply[A, S: Reusability](a: => A, src: S): ReusableVal2[A, S] =
    new ReusableVal2(() => a, src)

  def function[A: Reusability, B](a: A)(f: A => B): ReusableVal2[B, A] =
    apply(f(a), a)
}