package japgolly.scalajs.react.util

import japgolly.scalajs.react.callback._

object DefaultEffects extends DefaultEffectsApi {

  override type Async[A] = AsyncCallback[A]
  override type Sync [A] = CallbackTo[A]

  @inline override implicit def Async = Effect.asyncCallback
  @inline override implicit def Sync  = Effect.callback

}
