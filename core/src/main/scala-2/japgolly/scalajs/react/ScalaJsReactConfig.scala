package japgolly.scalajs.react

import japgolly.scalajs.react.internal.ScalaJsReactConfigMacros
import scala.scalajs.LinkingInfo.developmentMode

object ScalaJsReactConfig {

  trait ReusabilityOverride {
    def apply[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot]: ScalaComponent.Config[P, C, S, B, U, U]
  }

  object ReusabilityOverride {

    val default: ReusabilityOverride = new ReusabilityOverride {
      override def apply[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot] =
        _.shouldComponentUpdatePure(i => (i.currentProps ~/~ i.nextProps) || (i.currentState ~/~ i.nextState))
    }

    /** Don't configure shouldComponentUpdate */
    def ignore: ReusabilityOverride = new ReusabilityOverride {
      override def apply[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot] =
        internal.identityFn
    }
  }

  // ===================================================================================================================

  // This is here for compatability with the Scala 3 version of scalajs-react
  @inline def Instance = Defaults

  object Defaults {

    def automaticComponentName(displayName: String): String =
      macro ScalaJsReactConfigMacros.automaticComponentName

    def modifyComponentName(displayName: String): String =
      macro ScalaJsReactConfigMacros.modifyComponentName

    private[react] var reusabilityOverrideInDev: ReusabilityOverride =
      if (developmentMode)
        ReusabilityOverride.default
      else
        null

    @inline def reusabilityOverride: ReusabilityOverride =
      if (developmentMode)
        reusabilityOverrideInDev
      else
        ReusabilityOverride.default

    /** Calls to [[Reusability.shouldComponentUpdate]] can be overridden to use the provided logic, provided we're in
      * dev-mode (i.e. fastOptJS instead of fullOptJS).
      *
      * Rather than call this directly yourself, you probably want to call one of the following instead:
      *
      * - `ReusabilityOverlay.overrideGloballyInDev()`
      * - `Reusability.disableGloballyInDev()`
      */
    def unsafeOverrideReusabilityInDev(f: => ReusabilityOverride): Unit =
      if (developmentMode)
        reusabilityOverrideInDev = f
      else
        ()
  }
}
