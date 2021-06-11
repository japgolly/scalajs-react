package japgolly.scalajs.react.util

trait Monad[F[_]] {
  def delay  [A]   (a: => A)               : F[A]
  def pure   [A]   (a: A)                  : F[A]
  def map    [A, B](fa: F[A])(f: A => B)   : F[B]
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
}
