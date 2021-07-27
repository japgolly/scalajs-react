package japgolly.scalajs.react.util

import cats.effect._
import japgolly.scalajs.react.callback._

// DefaultEffects

sealed trait DefaultEffectsLowPri extends DefaultEffectsApiLowPri {
  override final type Async[A] = IO[A]
  
  @inline override final implicit lazy val Async: EffectCatsEffect.io.type = EffectCatsEffect.io
}

object DefaultEffects extends DefaultEffectsLowPri with DefaultEffectsApi {
  override type Sync[A] = CallbackTo[A]
  @inline override implicit val Sync: EffectCallback.callback.type = EffectCallback.callback
}

// Effect highest-priority

abstract class EffectFallbacks extends EffectFallbacks1 {
  override implicit def callback : Effect.Sync [CallbackTo] = EffectCallback.callback
  override implicit def io       : Effect.Async[IO        ] = EffectCatsEffect.io
}
