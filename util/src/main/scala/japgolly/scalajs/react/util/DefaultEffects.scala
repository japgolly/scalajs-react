package japgolly.scalajs.react.util

trait DefaultEffectsApi {

  type Async[A]
  implicit def Async: Effect.Async[Async]

  type Sync[A]
  implicit def Sync: Effect.Sync[Sync]
}
