package japgolly.scalajs.react

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
}
