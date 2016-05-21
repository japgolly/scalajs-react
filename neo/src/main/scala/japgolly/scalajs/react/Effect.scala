package japgolly.scalajs.react

abstract class Effect[F[_]] {
  @inline def point[A](a: => A): F[A]
  @inline def pure[A](a: A): F[A]
}

object Effect {
  type Id[A] = A

  implicit object InstanceId extends Effect[Id] {
    @inline override def point[A](a: => A) = a
    @inline override def pure [A](a: A)    = a
  }

  implicit object InstanceCallback extends Effect[CallbackTo] {
    @inline override def point[A](a: => A) = CallbackTo(a)
    @inline override def pure[A](a: A)     = CallbackTo.pure(a)
  }
}

