package japgolly.scalajs.react.util

trait EffectTrans[T[_[_]]] {

  protected def trans[F[_], G[_], A](from: T[F], to: T[G], f: => F[A]): G[A]

  class Trans[F[_], G[_]](final val from: T[F], final val to: T[G]) {
    def apply[A](f: => F[A]): G[A] =
      trans(from, to, f)

    def compose[H[_]](t: Trans[G, H])(implicit ev: Trans[F, F] <:< Trans[F, H] = null): Trans[F, H] =
      if (ev eq null)
        new Trans(from, t.to)
      else
        ev(Trans.id(from))
  }

  object Trans {
    final class Id[F[_]](F: T[F]) extends Trans[F, F](F, F) {
      override def apply[A](f: => F[A]): F[A] = f
    }

    def id[F[_]](implicit F: T[F]): Id[F] =
      new Id(F)

    def apply[F[_], G[_]](implicit F: T[F], G: T[G], ev: Trans[F, F] =:= Trans[F, G] = null): Trans[F, G] =
      if (ev eq null)
        new Trans(F, G)
      else
        ev(id(F))
  }

}
