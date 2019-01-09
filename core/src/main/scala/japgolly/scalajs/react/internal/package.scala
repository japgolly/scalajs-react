package japgolly.scalajs.react

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.Builder
import scala.util.{Failure, Success, Try}

package object internal {

  @inline implicit def toProfunctorOps[F[_, _], A, B](f: F[A, B])(implicit p: Profunctor[F]) =
    new Profunctor.Ops(f)(p)

  private[this] val identityFnInstance: Any => Any =
    a => a

  def identityFn[A]: A => A =
    identityFnInstance.asInstanceOf[A => A]

  def catchAll[A](a: => A): Try[A] =
    try Success(a)
    catch {case t: Throwable => Failure(t) }

  def intercalateTo[F[_], A](as: Iterator[A], sep: A)(implicit cbf: CanBuildFrom[Nothing, A, F[A]]): F[A] = {
    val b = cbf.apply()
    intercalateInto(b, as, sep)
    b.result()
  }

  def intercalateInto[A](b: Builder[A, _], it: Iterator[A], sep: A): Unit = {
    if (it.hasNext) {
      val first = it.next()
      b += first
      for (a <- it) {
        b += sep
        b += a
      }
    }
  }
}
