package japgolly.scalajs.react.util

import japgolly.scalajs.react.callback._

object DefaultEffects extends DefaultEffectsApi {

  override type Async[A] = AsyncCallback[A]
  override type Sync [A] = CallbackTo[A]

  @inline override implicit val Async: Effect.asyncCallback.type = Effect.asyncCallback
  @inline override implicit val Sync: Effect.callback.type = Effect.callback

}
