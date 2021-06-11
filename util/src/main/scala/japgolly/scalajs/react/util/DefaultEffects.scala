package japgolly.scalajs.react.util

trait DefaultEffects {

  type Async[A]
  implicit def async: SafeEffect.Async[Async]
  implicit def asyncEndo: SafeEffect.Async.Trans[Async, Async]

  type Sync[A]
  implicit def sync: SafeEffect.Sync[Sync]
  val syncEmpty: Sync[Unit]

}

object DefaultEffects extends DefaultEffects {
  override type Async[A]          = SafeEffect.Async.Untyped[A]
  override implicit def async     = SafeEffect.Async.untyped
  override implicit val asyncEndo = SafeEffect.Async.Trans.id[Async]

  override type Sync[A]           = SafeEffect.Sync.Untyped[A]
  override implicit def sync      = SafeEffect.Sync.untyped
  override val syncEmpty          = SafeEffect.Sync.empty

}
