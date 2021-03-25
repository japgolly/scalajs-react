package japgolly.scalajs.react.internal

import scala.collection.compat._
import scala.collection.mutable.Builder
import scala.util.{Failure, Success, Try}

object Util {

  def catchAll[A](a: => A): Try[A] =
    try Success(a)
    catch {case t: Throwable => Failure(t) }

  def intercalateTo[F[_], A](as: Iterator[A], sep: A)(implicit cbf: Factory[A, F[A]]): F[A] = {
    val b = cbf.newBuilder
    intercalateInto(b, as, sep)
    b.result()
  }

  def intercalateInto[A](b: Builder[A, _], it: Iterator[A], sep: A): Unit =
    if (it.hasNext) {
      val first = it.next()
      b += first
      for (a <- it) {
        b += sep
        b += a
      }
    }

}
