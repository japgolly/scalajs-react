package japgolly.scalajs.react

import scala.scalajs.LinkingInfo.productionMode

/** Reusability specific to Scala 2.x */
trait ReusabilityForScala2 {

  /** When you're in dev-mode (i.e. `fastOptJS`), this globally disables [[Reusability.shouldComponentUpdate]].
    */
  def disableGloballyInDev(): Unit =
    ScalaJsReactConfig.DevOnly.overrideReusability(
      new ScalaJsReactConfig.DevOnly.ReusabilityOverride {
        override def apply[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot] =
          identity
      }
    )

  def shouldComponentUpdate[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot]: ScalaComponent.Config[P, C, S, B, U, U] = {
    val default: ScalaComponent.Config[P, C, S, B, U, U] =
      _.shouldComponentUpdatePure(i =>
        (i.currentProps ~/~ i.nextProps) || (i.currentState ~/~ i.nextState))

    if (productionMode)
      default
    else
      ScalaJsReactConfig.DevOnly.reusabilityOverride match {
        case Some(o) => o.apply
        case None    => default
      }
  }

}
