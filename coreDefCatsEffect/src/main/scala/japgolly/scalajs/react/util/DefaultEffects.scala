package japgolly.scalajs.react.util

import cats.effect._

sealed trait DefaultEffectsLowPri extends DefaultEffectsApiLowPri {
  override final type Async[A] = IO[A]
  @inline override final implicit val Async: Effect.io.type = Effect.io
}

object DefaultEffects extends DefaultEffectsLowPri with DefaultEffectsApi {
  override type Sync [A] = SyncIO[A]
  @inline override implicit val Sync: Effect.syncIO.type = Effect.syncIO
}
