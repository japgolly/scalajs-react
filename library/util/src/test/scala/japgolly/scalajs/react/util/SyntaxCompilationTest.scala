package japgolly.scalajs.react.util

import japgolly.scalajs.react.util.syntax._

trait SyntaxCompilationTest {

  def sync[F[_]: Effect.Sync](f: F[Int]) =
    for {
      i <- f
      j <- f
    } yield {
      f.reset.dispatch()
      val k = f.runSync()
      i + j + k
    }

  def async[F[_]: Effect.Async](f: F[Int]) =
    for {
      i <- f
      j <- f
    } yield {
      val _ = f.toJsPromise
      i + j
    }

  def dispatch[F[_]: Effect.Dispatch](f: F[Int]) =
    for {
      i <- f
      j <- f
    } yield {
      val _ = f.dispatch()
      i + j
    }

}
