package japgolly.scalajs.react.core

import cats.Monad
import cats.syntax.all._

object StackSafety {

  /*
  type F[A] = Xxxx[A]
  "nestedFlatMapsInTailrecLoop"    - StackSafety.nestedFlatMapsInTailrecLoop[F]
  "nestedFlatMapsInNonTailrecLoop" - StackSafety.nestedFlatMapsInNonTailrecLoop[F]
   */

  private def n = 1000000

  def nestedFlatMapsInTailrecLoop[F[_]](implicit F: Monad[F]): F[Int] = {
    @scala.annotation.tailrec
    def sum(list: List[F[Int]])(acc: F[Int]): F[Int] =
      list match {
        case Nil          => acc
        case head :: tail => sum(tail)(head.flatMap(h => acc.map(_ + h)))
      }
    sum(List.fill(n)(F.pure(1)))(F.pure(0))
  }

  def nestedFlatMapsInNonTailrecLoop[F[_]](implicit F: Monad[F]): F[Int] = {
    def sum(list: List[F[Int]])(acc: F[Int]): F[Int] =
      list match {
        case Nil          => acc
        case head :: tail => head.flatMap(h => sum(tail)(acc.map(_ + h)))
      }
    sum(List.fill(n)(F.pure(1)))(F.pure(0))
  }

}
