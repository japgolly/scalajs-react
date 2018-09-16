package japgolly.scalajs.react

package object internal {

  @inline implicit def toProfunctorOps[F[_, _], A, B](f: F[A, B])(implicit p: Profunctor[F]) =
    new Profunctor.Ops(f)(p)

  private[this] val identityFnInstance: Any => Any =
    a => a

  def identityFn[A]: A => A =
    identityFnInstance.asInstanceOf[A => A]
}
