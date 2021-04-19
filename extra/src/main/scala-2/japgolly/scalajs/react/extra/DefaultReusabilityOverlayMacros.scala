package japgolly.scalajs.react.extra

import japgolly.scalajs.react._

trait DefaultReusabilityOverlayMacros {
  import DefaultReusabilityOverlay.{Options, defaults}

  /** When you're in dev-mode (i.e. `fastOptJS`), this overrides [[Reusability.shouldComponentUpdate]] to use overlays.
    */
  final def overrideGloballyInDev(options: Options = defaults): Unit =
    ScalaJsReactConfig.DevOnly.overrideReusability(
      new ScalaJsReactConfig.DevOnly.ReusabilityOverride {
        override def apply[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot] =
          ReusabilityOverlay.install(options)
      }
    )

}
