package japgolly.scalajs.react.internal

import japgolly.scalajs.react.CallbackTo

abstract class Effect[F[_]] {
  @inline def point  [A]   (a: => A)              : F[A]
  @inline def pure   [A]   (a: A)                 : F[A]
  @inline def map    [A, B](a: F[A])(f: A => B)   : F[B]
  @inline def flatMap[A, B](a: F[A])(f: A => F[B]): F[B]
  @inline def extract[A]   (a: => F[A])           : () => A
}

object Effect {
  type Id[A] = A

  implicit object InstanceId extends Effect[Id] {
    @inline override def point  [A]   (a: => A)         = a
    @inline override def pure   [A]   (a: A)            = a
    @inline override def map    [A, B](a: A)(f: A => B) = f(a)
    @inline override def flatMap[A, B](a: A)(f: A => B) = f(a)
    @inline override def extract[A]   (a: => A)         = () => a
  }

  implicit object InstanceCallback extends Effect[CallbackTo] {
    @inline override def point  [A]   (a: => A)                                 = CallbackTo(a)
    @inline override def pure   [A]   (a: A)                                    = CallbackTo.pure(a)
    @inline override def map    [A, B](a: CallbackTo[A])(f: A => B)             = a map f
    @inline override def flatMap[A, B](a: CallbackTo[A])(f: A => CallbackTo[B]) = a flatMap f
    @inline override def extract[A]   (a: => CallbackTo[A])                     = a.toScalaFn
  }

  // ===================================================================================================================

  class Trans[F[_], G[_]](val from: Effect[F], val to: Effect[G]) {
    def apply[A](f: F[A]): G[A] = {
      val fn = from.extract(f)
      to.point(fn())
    }

    def compose[H[_]](t: G Trans H)(implicit ev: Trans[F, F] =:= Trans[F, H] = null): F Trans H =
      if (ev eq null)
        new Trans(from, t.to)
      else
        ev(Trans.id(from))
  }

  object Trans {
    final class Id[F[_]](F: Effect[F]) extends Trans[F, F](F, F) {
      override def apply[A](f: F[A]) = f
    }

    @inline def id[F[_]](F: Effect[F]): Id[F] =
      new Id(F)

    def apply[F[_], G[_]](implicit F: Effect[F], G: Effect[G], ev: Trans[F, F] =:= Trans[F, G] = null): F Trans G =
      if (ev eq null)
        new Trans(F, G)
      else
        ev(id(F))

    implicit val IdToCallback = Trans[Effect.Id, CallbackTo]
    implicit val CallbackToId = Trans[CallbackTo, Effect.Id]
  }
}

