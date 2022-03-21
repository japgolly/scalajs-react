package japgolly.scalajs.react.util

sealed trait DefaultEffectsLowPri extends DefaultEffectsApiLowPri {
  override final type Async[A] = Effect.Async.Untyped[A]
  @noinline override final implicit def Async: Effect.Async[Async] = ???
}

object DefaultEffects extends DefaultEffectsLowPri with DefaultEffectsApi {
  override type Sync[A] = Effect.Sync.Untyped[A]
  @noinline override implicit def Sync: Effect.Sync[Sync] = ???
}
