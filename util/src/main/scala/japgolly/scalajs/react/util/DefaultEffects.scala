package japgolly.scalajs.react.util

trait DefaultEffects {

  type Async[A]
  implicit def async: Effect.Async[Async]

  type Sync[A]
  implicit def sync: Effect.Sync[Sync]
  val syncEmpty: Sync[Unit]

  implicit val semigroupSyncUnit: Semigroup[Sync[Unit]]
  val semigroupSyncOr: Semigroup[Sync[Boolean]]
}

object DefaultEffects extends DefaultEffects {
  override type Async[A]      = Effect.Async.Untyped[A]
  override implicit val async = Effect.Async.untyped

  override type Sync[A]       = Effect.Sync.Untyped[A]
  override implicit val sync  = Effect.Sync.untyped
  override val syncEmpty      = Effect.Sync.empty

  override implicit val semigroupSyncUnit: Semigroup[Sync[Unit]] =
    Semigroup((f, g) => sync.flatMap(f)(_ => g))

  override val semigroupSyncOr: Semigroup[Sync[Boolean]] =
    Semigroup((f, g) => sync.delay(f() || g()))
}
