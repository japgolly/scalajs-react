package japgolly.scalajs.react.util

trait DefaultEffects {

  type Async[A]
  implicit def async: Effect.Async[Async]

  type Sync[A]
  implicit def sync: Effect.Sync[Sync]
}

object DefaultEffects extends DefaultEffects {
  override type Async[A]      = Effect.Async.Untyped[A]
  override implicit val async = Effect.Async.untyped

  override type Sync[A]       = Effect.Sync.Untyped[A]
  override implicit val sync  = Effect.Sync.untyped
}
