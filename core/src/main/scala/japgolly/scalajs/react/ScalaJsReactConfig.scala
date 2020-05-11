package japgolly.scalajs.react

import java.util.regex.Pattern
import scala.annotation.elidable
import scala.scalajs.LinkingInfo.productionMode

/** Global scalajs-react config.
  *
  * It's a little horrible in that we're using global variables *and* you need to configure them in your `main` method
  * *before* any data/components are loaded that these settings. But it's a decent trade-off in this case.
  */
object ScalaJsReactConfig {

  /** Config that only applies in `fastOptJS`, It is ignored and removed from `fullOptJS`. */
  object DevOnly {

    // ===================================================================================================================
    // Overriding behaviour of Reusability.shouldComponentUpdate

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

  // ===================================================================================================================
  // Automatic component names

  private[react] var componentNameModifier: String => String = {
    @elidable(elidable.INFO)
    def initialValue: String => String = {
      val regex = Pattern.compile("\\.?comp(?:onent)?$", Pattern.CASE_INSENSITIVE)
      regex.matcher(_).replaceFirst("")
    }
    initialValue
  }

  @elidable(elidable.INFO)
  def componentNameModifierSet(f: String => String): Unit =
    componentNameModifier = f

  @elidable(elidable.INFO)
  def componentNameModifierAppend(f: String => String): Unit =
    componentNameModifier = f compose componentNameModifier

  @elidable(elidable.INFO)
  def componentNameModifierPrepend(f: String => String): Unit =
    componentNameModifier = componentNameModifier compose f
}
