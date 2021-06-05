package japgolly.scalajs.react

/** Reusability specific to Scala 2.x */
trait ReusabilityForScala2 {

  /** When you're in dev-mode (i.e. `fastOptJS`), this globally disables [[Reusability.shouldComponentUpdate]].
    */
  final def disableGloballyInDev(): Unit =
    ScalaJsReactConfig.Defaults.unsafeOverrideReusabilityInDev(
      ScalaJsReactConfig.ReusabilityOverride.ignore)

  @inline final def shouldComponentUpdate[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot]: ScalaComponent.Config[P, C, S, B, U, U] =
    ScalaJsReactConfig.Instance.reusabilityOverride.apply[P, C, S, B, U]

}
