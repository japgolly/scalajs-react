package japgolly.scalajs.react.core

import cats.Monad
import cats.syntax.all._
import japgolly.scalajs.react.{AsyncCallback, CallbackTo}

object StackSafety {

  /*
  type F[A] = Xxxx[A]
  "nestedFlatMapsInTailrecLoop"    - StackSafety.nestedFlatMapsInTailrecLoop[F]
  "nestedFlatMapsInNonTailrecLoop" - StackSafety.nestedFlatMapsInNonTailrecLoop[F]
   */

  final case class Eval[F[_]](run: F[Unit] => Unit)

  object Eval {
    implicit val evalCallback: Eval[CallbackTo] = Eval(_.runNow())
    implicit val evalAsyncCallback: Eval[AsyncCallback] = Eval(_.toCallback.runNow())
  }

  private def n = 50000

  def nestedFlatMapsInTailrecLoop[F[_]](implicit F: Monad[F], eval: Eval[F]): Unit = {
    @scala.annotation.tailrec
    def sum(list: List[F[Int]])(acc: F[Int]): F[Int] =
      list match {
        case Nil          => acc
        case head :: tail => sum(tail)(head.flatMap(h => acc.map(_ + h)))
      }
    val f = sum(List.fill(n)(F.pure(1)))(F.pure(0))
    eval.run(F.map(f)(_ => ()))
    ()
  }

  def nestedFlatMapsInNonTailrecLoop[F[_]](implicit F: Monad[F], eval: Eval[F]): Unit = {
    def sum(list: List[F[Int]])(acc: F[Int]): F[Int] =
      list match {
        case Nil          => acc
        case head :: tail => head.flatMap(h => sum(tail)(acc.map(_ + h)))
      }
    val f = sum(List.fill(n)(F.pure(1)))(F.pure(0))
    eval.run(F.map(f)(_ => ()))
    ()
  }

}
