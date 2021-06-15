package japgolly.scalajs.react.extra.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.internal.CoreGeneral._
import japgolly.scalajs.react.internal.EffectUtil
import japgolly.scalajs.react.util.DefaultEffects.Sync
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html.Input

/**
 * Checkbox that can have three states: Checked, Unchecked, Indeterminate.
 *
 * @since 0.11.0
 */
object TriStateCheckbox {

  sealed abstract class State extends Product with Serializable {
    final def nextDeterminate: Determinate =
      this match {
        case Checked       => Unchecked
        case Indeterminate
           | Unchecked     => Checked
      }

    final def nextGrow: State =
      this match {
        case Checked       => Unchecked
        case Indeterminate => Checked
        case Unchecked     => Indeterminate
      }

    final def nextShrink: State =
      this match {
        case Checked       => Indeterminate
        case Indeterminate => Unchecked
        case Unchecked     => Checked
      }
  }

  sealed abstract class Determinate extends State

  case object Checked       extends Determinate
  case object Unchecked     extends Determinate
  case object Indeterminate extends State

  final case class Props(state       : State,
                         setNextState: Sync[Unit],
                         disabled    : Boolean = false) {
    @inline def render: VdomElement = Component(this)
  }

  private def render($: ScalaComponent.MountedPure[Props, Unit, Unit], p: Props) = {
    val setNext = Sync.flatMap($.props)(p => if (p.disabled) Sync.empty else p.setNextState) // Only access .setNextState inside Sync for Reusability
    <.input.checkbox(
      ^.disabled := p.disabled,
      TagMod.unless(p.disabled)(eventHandlers(setNext)))
  }

  /**
   * Clicking or pressing space = change.
   */
  def eventHandlers(onChange: Sync[Unit]): TagMod = {
    def handleKey(e: ReactKeyboardEventFromHtml): Sync[Unit] = {
      EffectUtil.asEventDefault_(e)(
        EffectUtil.keyCodeSwitch(e) { case KeyCode.Space => onChange }
      )
    }
    TagMod(
      ^.onClick   --> onChange,
      ^.onKeyDown ==> handleKey)
  }

  private def updateDom[P, S, B]($: ScalaComponent.MountedImpure[P, S, B], nextProps: Props): Sync[Unit] = {
    val s = nextProps.state
    Sync.delay {
      $.getDOMNode.toElement.map(_.domCast[Input]).foreach { d =>
        d.checked       = s == Checked
        d.indeterminate = s == Indeterminate
      }
    }
  }

  implicit val reusabilityState: Reusability[State] =
    Reusability.by_==

  implicit val reusabilityProps: Reusability[Props] =
    Reusability.caseClassExcept("setNextState") // .setNextState is never accessed outside of a Sync[Unit]

  val Component = ScalaComponent.builder[Props]("TriStateCheckbox")
    .stateless
    .noBackend
    .render(i => render(i.mountedPure, i.props))
    .componentDidMount(i => updateDom(i.mountedImpure, i.props))
    .componentDidUpdate(i => updateDom(i.mountedImpure, i.currentProps))
    .configure(Reusability.shouldComponentUpdate)
    .build
}