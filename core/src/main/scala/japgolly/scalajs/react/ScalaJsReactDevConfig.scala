package japgolly.scalajs.react

import scala.scalajs.LinkingInfo.productionMode

/** Special development-mode config that only applies in `fastOptJS`, but is ignored and removed from `fullOptJS`.
  */
object ScalaJsReactDevConfig {

  trait ReusabilityOverride {
    def apply[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot]: ScalaComponent.Config[P, C, S, B, U, U]
  }

  private[react] var reusabilityOverride: Option[ReusabilityOverride] =
    None

  /** Calls to [[Reusability.shouldComponentUpdate]] can be overridden to use the provided logic.
    *
    * Rather than call this directly yourself, you probably want to call `ReusabilityOverlay.overrideGloballyInDev()`
    * instead.
    */
  def overrideReusability(o: => ReusabilityOverride): Unit =
    if (productionMode) () else reusabilityOverride = Some(o)

  def removeReusabilityOverride(): Unit =
    if (productionMode) () else reusabilityOverride = None
}
