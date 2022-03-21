package japgolly.scalajs.react.util

import cats._
import cats.effect._
import japgolly.scalajs.react.ReactCats._
import japgolly.scalajs.react.util.EffectCatsEffect._

object ReactCatsEffectCompilationTest {

  Monoid[IO[Unit]]
  Monoid[SyncIO[Int]]
}
