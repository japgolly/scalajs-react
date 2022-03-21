package japgolly.scalajs.react.util

/** This is a separate because both Sync and Async extend Dispatch, and there needs to be an
  * unambiguous Dispatch instance even if both the Sync and Async types are the same.
  */
trait DefaultEffectsApiLowPri {
  type Async[A]
  implicit def Async: Effect.Async[Async]
}

trait DefaultEffectsApi extends DefaultEffectsApiLowPri {
  type Sync[A]
  implicit def Sync: Effect.Sync[Sync]
}
