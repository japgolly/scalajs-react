package japgolly.scalajs.react.internal

import japgolly.scalajs.react.CallbackTo

abstract class Effect[F[_]] {
  @inline def point  [A]   (a: => A)              : F[A]
  @inline def pure   [A]   (a: A)                 : F[A]
  @inline def map    [A, B](a: F[A])(f: A => B)   : F[B]
  @inline def flatMap[A, B](a: F[A])(f: A => F[B]): F[B]
}

object Effect {
  type Id[A] = A

  implicit object InstanceId extends Effect[Id] {
    @inline override def point  [A]   (a: => A)         = a
    @inline override def pure   [A]   (a: A)            = a
    @inline override def map    [A, B](a: A)(f: A => B) = f(a)
    @inline override def flatMap[A, B](a: A)(f: A => B) = f(a)
  }

  implicit object InstanceCallback extends Effect[CallbackTo] {
    @inline override def point  [A]   (a: => A)                                 = CallbackTo(a)
    @inline override def pure   [A]   (a: A)                                    = CallbackTo.pure(a)
    @inline override def map    [A, B](a: CallbackTo[A])(f: A => B)             = a map f
    @inline override def flatMap[A, B](a: CallbackTo[A])(f: A => CallbackTo[B]) = a flatMap f
  }
}

