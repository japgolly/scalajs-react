package japgolly.scalajs.react

import cats._
import japgolly.scalajs.react.ReactCats._
import japgolly.scalajs.react.util._

object ReactCatsCompilationTest {

  def monadThrowA [F[_]](implicit F: Effect.Async[F]                    ): MonadThrow[F] = implicitly
  def monadThrowAS[F[_]](implicit F: Effect.Async[F] with Effect.Sync[F]): MonadThrow[F] = implicitly
  def monadThrowS [F[_]](implicit F: Effect.Sync[F]                     ): MonadThrow[F] = implicitly
  def monadThrowSA[F[_]](implicit F: Effect.Sync[F] with Effect.Async[F]): MonadThrow[F] = implicitly

  def monoidA [F[_], A](implicit F: Effect.Async[F]                    , A: Monoid[A]): Monoid[F[A]] = implicitly
  def monoidAS[F[_], A](implicit F: Effect.Async[F] with Effect.Sync[F], A: Monoid[A]): Monoid[F[A]] = implicitly
  def monoidS [F[_], A](implicit F: Effect.Sync[F]                     , A: Monoid[A]): Monoid[F[A]] = implicitly
  def monoidSA[F[_], A](implicit F: Effect.Sync[F] with Effect.Async[F], A: Monoid[A]): Monoid[F[A]] = implicitly
}
