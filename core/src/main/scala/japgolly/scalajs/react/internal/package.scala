package japgolly.scalajs.react

import scala.scalajs.js.|

package object internal {

  def jsNullToOption[A](an: A | Null): Option[A] =
    Option(an.asInstanceOf[A])

  def optionToJsNull[A](oa: Option[A]): A | Null =
    oa match {
      case Some(a) => a
      case None    => null
    }

  @inline implicit def toProfunctorOps[F[_, _], A, B](f: F[A, B])(implicit p: Profunctor[F]) =
    new Profunctor.Ops(f)(p)

  private[this] val identityFnInstance: Any => Any =
    a => a

  def identityFn[A]: A => A =
    identityFnInstance.asInstanceOf[A => A]
}
