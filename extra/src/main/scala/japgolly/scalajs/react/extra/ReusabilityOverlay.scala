package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.internal.Box
import scala.scalajs.js

/**
 * Heavily inspired by https://github.com/redsunsoft/react-render-visualizer
 */
object ReusabilityOverlay {
  type Comp = ScalaComponent.MountedImpure[Any, Any, Any]

  private val key = "reusabilityOverlay"

  /** When you're in dev-mode (i.e. `fastOptJS`), this overrides [[Reusability.shouldComponentUpdate]] to use overlays.
    */
  @inline def overrideGloballyInDev(): Unit =
    DefaultReusabilityOverlay.overrideGloballyInDev()

  def install[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot]: ScalaComponent.Config[P, C, S, B, U, U] =
    install(DefaultReusabilityOverlay.defaults)

  def install[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot]
      (newOverlay: ScalaComponent.MountedImpure[P, S, B] => ReusabilityOverlay): ScalaComponent.Config[P, C, S, B, U, U] = {

    // Store the overlay stats on each instance
    def get(raw: ScalaComponent.RawMounted[P, S, B]): ReusabilityOverlay = {
      def $ = raw.asInstanceOf[js.Dynamic]
      $.selectDynamic(key).asInstanceOf[js.UndefOr[Box[ReusabilityOverlay]]].fold {
        val o = newOverlay(ScalaComponent.mountRaw(raw))
        $.updateDynamic(key)(Box(o))
        o
      }(_.unbox)
    }

    Reusability.shouldComponentUpdateAnd[P, C, S, B, U] { r =>
      val overlay = get(r.mounted.js.raw)
      if (r.update) {
        def fmt(update: Boolean, name: String, va: Any, vb: Any) =
          if (!update)
            ""
          else {
            var a = va.toString
            var b = vb.toString
            if (a.contains(' ') || b.contains(' ')) {
              a = "【" + a + "】"
              b = "【" + b + "】"
            }
            if (a.contains('\n') || a.length > 50 || b.length > 50)
              s"$name update:\n  BEFORE: $a\n   AFTER: $b"
            else
              s"$name update: $a ⇒ $b"
          }
        val sep = if (r.updateProps && r.updateState) "\n" else ""
        val reason = fmt(r.updateProps, "Props", r.currentProps, r.nextProps) + sep +
                     fmt(r.updateState, "State", r.currentState, r.nextState)
        overlay logBad reason
      }
      else
        overlay.logGood
    } andThen (_
      .componentDidMount(i => get(i.raw).onMount)
      .componentWillUnmount(i => get(i.raw).onUnmount)
    )
  }
}

trait ReusabilityOverlay {
  def onMount  : Callback
  def onUnmount: Callback
  val logGood  : Callback
  def logBad(reason: String): Callback
}
