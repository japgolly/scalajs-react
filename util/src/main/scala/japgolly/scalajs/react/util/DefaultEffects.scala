package japgolly.scalajs.react.util

trait DefaultEffectsApi {

  type Async[A]
  implicit def Async: Effect.Async[Async]

  type Sync[A]
  implicit def Sync: Effect.Sync[Sync]
}

object DefaultEffects extends DefaultEffectsApi {
  override type Async[A] = Effect.Async.Untyped[A]
  override type Sync [A] = Effect.Sync.Untyped[A]

  @noinline override implicit def Async = Effect.Async.untyped
  @noinline override implicit def Sync  = Effect.Sync.untyped
}
