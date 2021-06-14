package japgolly.scalajs.react.util

object DefaultEffects extends DefaultEffectsApi {
  override type Async[A] = Effect.Async.Untyped[A]
  override type Sync [A] = Effect.Sync.Untyped[A]

  @noinline override implicit def Async: Effect.Async[Async] = ???
  @noinline override implicit def Sync: Effect.Sync[Sync] = ???
}
