package japgolly.scalajs.react.internal

import japgolly.scalajs.react.CallbackTo

abstract class Effect[F[_]] {
  def point  [A]   (a: => A)              : F[A]
  def pure   [A]   (a: A)                 : F[A]
  def map    [A, B](a: F[A])(f: A => B)   : F[B]
  def flatMap[A, B](a: F[A])(f: A => F[B]): F[B]
  def extract[A]   (a: => F[A])           : () => A
}

object Effect {
  type Id[A] = A

  implicit val idInstance: Effect[Id] = new Effect[Id] {
    override def point  [A]   (a: => A)         = a
    override def pure   [A]   (a: A)            = a
    override def map    [A, B](a: A)(f: A => B) = f(a)
    override def flatMap[A, B](a: A)(f: A => B) = f(a)
    override def extract[A]   (a: => A)         = () => a
  }

  implicit val callbackInstance: Effect[CallbackTo] = new Effect[CallbackTo] {
    override def point  [A]   (a: => A)                                 = CallbackTo(a)
    override def pure   [A]   (a: A)                                    = CallbackTo.pure(a)
    override def map    [A, B](a: CallbackTo[A])(f: A => B)             = a map f
    override def flatMap[A, B](a: CallbackTo[A])(f: A => CallbackTo[B]) = a flatMap f
    override def extract[A]   (a: => CallbackTo[A])                     = a.toScalaFn
  }

  // ===================================================================================================================

  class Trans[F[_], G[_]](final val from: Effect[F], final val to: Effect[G]) {
    def apply[A](f: => F[A]): G[A] = {
      val fn = from.extract(f)
      to.point(fn())
    }

    def compose[H[_]](t: G Trans H)(implicit ev: Trans[F, F] <:< Trans[F, H] = null): F Trans H =
      if (ev eq null)
        new Trans(from, t.to)
      else
        ev(Trans.id(from))
  }

  object Trans {
    final class Id[F[_]](F: Effect[F]) extends Trans[F, F](F, F) {
      override def apply[A](f: => F[A]): F[A] = f
    }

    def id[F[_]](implicit F: Effect[F]): Id[F] =
      new Id(F)

    def apply[F[_], G[_]](implicit F: Effect[F], G: Effect[G], ev: Trans[F, F] =:= Trans[F, G] = null): F Trans G =
      if (ev eq null)
        new Trans(F, G)
      else
        ev(id(F))

    implicit val endoId       = Trans.id[Effect.Id]
    implicit val endoCallback = Trans.id[CallbackTo]
    implicit val idToCallback = Trans[Effect.Id, CallbackTo]
    implicit val callbackToId = Trans[CallbackTo, Effect.Id]
  }
}

