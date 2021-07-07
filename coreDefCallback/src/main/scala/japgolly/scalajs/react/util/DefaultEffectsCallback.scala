package japgolly.scalajs.react.util

import japgolly.scalajs.react.callback._

// DefaultEffects

sealed trait DefaultEffectsLowPri extends DefaultEffectsApiLowPri {
  override final type Async[A] = AsyncCallback[A]
  @inline override final implicit val Async: EffectCallback.asyncCallback.type = EffectCallback.asyncCallback
}

object DefaultEffects extends DefaultEffectsLowPri with DefaultEffectsApi {
  override type Sync[A] = CallbackTo[A]
  @inline override implicit val Sync: EffectCallback.callback.type = EffectCallback.callback
}

// Effect highest-priority

abstract class EffectFallbacks extends EffectFallbacks1 {
  override implicit def callback     : Effect.Sync [CallbackTo   ] = EffectCallback.callback
  override implicit def asyncCallback: Effect.Async[AsyncCallback] = EffectCallback.asyncCallback
}
