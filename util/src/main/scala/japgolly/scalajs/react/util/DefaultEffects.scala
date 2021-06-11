package japgolly.scalajs.react.util

trait DefaultEffects {
  type Async[_]
  implicit def async: SafeEffect.Async[Async]
  implicit def asyncEndo: SafeEffect.Async.Trans[Async, Async]

  type Sync[_]
  implicit def sync: SafeEffect.Sync[Sync]
}

object DefaultEffects extends DefaultEffects {
  override type Async[A]          = SafeEffect.Async.Untyped[A]
  override implicit def async     = SafeEffect.Async.untyped
  override implicit val asyncEndo = SafeEffect.Async.Trans.id[Async]

  override type Sync[A]           = SafeEffect.Sync.Untyped[A]
  override implicit def sync      = SafeEffect.Sync.untyped
}
