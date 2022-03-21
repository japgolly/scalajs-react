package japgolly.scalajs.react.internal

trait Profunctor[F[_, _]] {
  def lmap[A, B, C](f: F[A, B])(m: C => A): F[C, B]
  def rmap[A, B, C](f: F[A, B])(m: B => C): F[A, C]

  def dimap[A, B, C, D](f: F[A, B])(l: C => A, r: B => D): F[C, D] =
    rmap(lmap(f)(l))(r)
}

object Profunctor {

  final class Ops[F[_, _], A, B](private val f: F[A, B]) extends AnyVal {
    @inline def lmap[C](m: C => A)(implicit p: Profunctor[F]): F[C, B] =
      p.lmap(f)(m)

    @inline def rmap[C](m: B => C)(implicit p: Profunctor[F]): F[A, C] =
      p.rmap(f)(m)

    @inline def dimap[C, D](l: C => A, r: B => D)(implicit p: Profunctor[F]): F[C, D] =
      p.dimap(f)(l, r)
  }

  object Ops {
    @inline implicit def toProfunctorOps[F[_, _], A, B](f: F[A, B]): Ops[F, A, B] =
      new Ops(f)
  }
}