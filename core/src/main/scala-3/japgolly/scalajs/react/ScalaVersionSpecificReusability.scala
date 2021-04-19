package japgolly.scalajs.react

import scala.collection.immutable.ArraySeq

/** Reusability specific to Scala 3 */
trait ScalaVersionSpecificReusability {
  import Reusability.*

  final implicit def arraySeq[A: Reusability]: Reusability[ArraySeq[A]] =
    byRef[ArraySeq[A]] || indexedSeq[ArraySeq, A]

  // ===================================================================================================================

  /** When you're in dev-mode (i.e. `fastOptJS`), this globally disables [[Reusability.shouldComponentUpdate]].
    */
  inline def disableGloballyInDev(): Unit =
    ScalaJsReactConfig.Defaults.unsafeOverrideReusabilityInDev(
      ScalaJsReactConfig.ReusabilityOverride.ignore)

  inline def shouldComponentUpdate[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot]: ScalaComponent.Config[P, C, S, B, U, U] =
    ScalaJsReactConfig.Instance.reusabilityOverride.apply[P, C, S, B, U]

}
