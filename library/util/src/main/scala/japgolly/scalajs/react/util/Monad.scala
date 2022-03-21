package japgolly.scalajs.react.util

trait Monad[F[_]] {
  def pure   [A]   (a: A)                  : F[A]
  def map    [A, B](fa: F[A])(f: A => B)   : F[B]
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

  def tailrec[A, B](a: A)(f: A => F[Either[A, B]]): F[B]

  def chain[A, B](fa: F[A], fb: F[B]): F[B] =
    flatMap(fa)(_ => fb)

  @inline final def chain[A, B, C](fa: F[A], fb: F[B], fc: F[C]): F[C] =
    chain(chain(fa, fb), fc)

  @inline final def chain[A, B, C, D](fa: F[A], fb: F[B], fc: F[C], fd: F[D]): F[D] =
    chain(chain(fa, fb, fc), fd)
}
