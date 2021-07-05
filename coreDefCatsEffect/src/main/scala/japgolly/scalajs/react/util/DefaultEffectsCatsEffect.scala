package japgolly.scalajs.react.util

import cats.effect._

// DefaultEffects

sealed trait DefaultEffectsLowPri extends DefaultEffectsApiLowPri {
  override final type Async[A] = IO[A]
  @inline override final implicit val Async: EffectCatsEffect.io.type = EffectCatsEffect.io
}

object DefaultEffects extends DefaultEffectsLowPri with DefaultEffectsApi {
  override type Sync [A] = SyncIO[A]
  @inline override implicit val Sync: EffectCatsEffect.syncIO.type = EffectCatsEffect.syncIO
}

// Effect highest-priority

abstract class EffectFallbacks extends EffectFallbacks1 {
  override implicit def syncIO: Effect.Sync [SyncIO] = EffectCatsEffect.syncIO
  override implicit def io    : Effect.Async[IO    ] = EffectCatsEffect.io
}