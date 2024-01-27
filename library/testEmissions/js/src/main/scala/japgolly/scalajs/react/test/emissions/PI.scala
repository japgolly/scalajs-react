package japgolly.scalajs.react.test.emissions

import japgolly.scalajs.react._

final case class PI(pi: Int) {
  def unary_- : PI = PI(-pi)
  def *(n: Int): PI = PI(pi * n)
  def +(n: Int): PI = PI(pi + n)
  def +(n: PI): PI = PI(pi + n.pi)
}

object PI {
  implicit val reusability: Reusability[PI] =
    Reusability.by[PI, Int](_.pi >> 1)
}