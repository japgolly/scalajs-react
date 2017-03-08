package japgolly.scalajs.react.internal

trait Profunctor[F[_, _]] {
  def lmap[A, B, C](f: F[A, B])(m: C => A): F[C, B]
  def rmap[A, B, C](f: F[A, B])(m: B => C): F[A, C]
  def dimap[A, B, C, D](f: F[A, B])(l: C => A, r: B => D): F[C, D] =
    rmap(lmap(f)(l))(r)
}

object Profunctor {
  final class Ops[F[_, _], A, B](f: F[A, B])(implicit p: Profunctor[F]) {
    def lmap[C](m: C => A): F[C, B] =
      p.lmap(f)(m)

    def rmap[C](m: B => C): F[A, C] =
      p.rmap(f)(m)

    def dimap[C, D](l: C => A, r: B => D): F[C, D] =
      p.dimap(f)(l, r)
  }
}