package japgolly.scalajs.react

import japgolly.microlibs.compiletime.*
import japgolly.scalajs.react.internal.CompileTimeConfig

trait ScalaJsReactConfig {
  def automaticComponentName(name: String): String
  def modifyComponentName(name: String): String
  def reusabilityOverride: ScalaJsReactConfig.ReusabilityOverride
}

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
  // Finally-resolved Instance

  object Instance extends ScalaJsReactConfig {
    import japgolly.scalajs.react.internal.{ScalaJsReactConfigProxy => P}
    override transparent inline def automaticComponentName(name: String) = P.automaticComponentName(name)
    override transparent inline def modifyComponentName   (name: String) = P.modifyComponentName(name)
    override transparent inline def reusabilityOverride                  = P.reusabilityOverride
  }

  // ===================================================================================================================
  // Defaults

  trait Defaults extends ScalaJsReactConfig {
    override def automaticComponentName(name: String) = Defaults.automaticComponentName(name)
    override def modifyComponentName   (name: String) = Defaults.modifyComponentName(name)
    override def reusabilityOverride                  = Defaults.reusabilityOverride
  }

  object Defaults extends ScalaJsReactConfig {
    import Util.ComponentName.*
    import scala.scalajs.LinkingInfo.developmentMode

    override transparent inline def automaticComponentName(name: String) =
      inline CompileTimeConfig.getTrimLowerCaseNonBlank(KeyCompNameAuto) match {
        case Some("blank") => ""
        case Some("short") => stripPath(stripComponentSuffix(name))
        case Some("full")  => stripComponentSuffix(name)
        case None          => stripComponentSuffix(name)
        case Some(x)       =>
          InlineUtils.warn(s"Invalid value for $KeyCompNameAuto: $x.\nValid values are: full | short | blank.")
          stripComponentSuffix(name)
      }

    override transparent inline def modifyComponentName(name: String): String =
      inline CompileTimeConfig.getTrimLowerCaseNonBlank(KeyCompNameAll) match {
        case Some("blank") => ""
        case Some("allow") => name
        case None          => name
        case Some(x)       =>
          InlineUtils.warn(s"Invalid value for $KeyCompNameAll: $x.\nValid values are: allow | blank.")
          name
      }

    private[react] var reusabilityOverrideInDev: ReusabilityOverride =
      if developmentMode then
        ReusabilityOverride.default
      else
        null

    override inline def reusabilityOverride: ReusabilityOverride =
      if developmentMode then
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
    inline def overrideReusabilityInDev(inline f: ReusabilityOverride): Unit =
      if developmentMode then
        reusabilityOverrideInDev = f
      else
        ()
  }

  // ===================================================================================================================

  inline val KeyConfigClass  = "japgolly.scalajs.react.config.class"
  inline val KeyCompNameAuto = "japgolly.scalajs.react.component.names.implicit"
  inline val KeyCompNameAll  = "japgolly.scalajs.react.component.names.all"

  object Util {
    object ComponentName {
      import TransparentInlineUtils.*

      transparent inline def stripComponentSuffix(name: String): String =
        replaceFirst(name, "(?i)\\.?comp(?:onent)?$", "")

      transparent inline def stripPath(name: String): String =
        replaceFirst(name, "^.+\\.", "")
    }
  }

  // TODO: move into tests
  private class ProofThatDefaultsAreComplete extends Defaults
}
