package japgolly.scalajs.react.util

import japgolly.scalajs.react.callback._

sealed trait DefaultEffectsLowPri extends DefaultEffectsApiLowPri {
  override final type Async[A] = AsyncCallback[A]
  @inline override final implicit val Async: Effect.asyncCallback.type = Effect.asyncCallback
}

object DefaultEffects extends DefaultEffectsLowPri with DefaultEffectsApi {
  override type Sync [A] = CallbackTo[A]
  @inline override implicit val Sync: Effect.callback.type = Effect.callback
}
