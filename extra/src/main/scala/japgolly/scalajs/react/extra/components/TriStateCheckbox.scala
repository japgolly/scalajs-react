package japgolly.scalajs.react.extra.components

import japgolly.scalajs.react._
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
                         setNextState: Callback,
                         disabled    : Boolean = false) {
    @inline def render: VdomElement = Component(this)
  }

  private def render($: ScalaComponent.MountedPure[Props, Unit, Unit], p: Props) = {
    val setNext = $.props.flatMap(p => p.setNextState.unless_(p.disabled)) // Only access .setNextState inside the Callback for Reusability
    <.input.checkbox(
      ^.disabled := p.disabled,
      TagMod.unless(p.disabled)(eventHandlers(setNext)))
  }

  /**
   * Clicking or pressing space = change.
   */
  def eventHandlers(onChange: Callback): TagMod = {
    def handleKey(e: ReactKeyboardEventFromHtml): Callback =
      CallbackOption.keyCodeSwitch(e) {
        case KeyCode.Space => onChange
      }.asEventDefault(e)
    TagMod(
      ^.onClick   --> onChange,
      ^.onKeyDown ==> handleKey)
  }

  private def updateDom($: ScalaComponent.MountedImpure[_, _, _], nextProps: Props): Callback = {
    val s = nextProps.state
    Callback {
      $.getDOMNode.toElement.map(_.domCast[Input]).foreach { d =>
        d.checked       = s == Checked
        d.indeterminate = s == Indeterminate
      }
    }
  }

  implicit val reusabilityState: Reusability[State] =
    Reusability.by_==

  implicit val reusabilityProps: Reusability[Props] =
    Reusability.caseClassExcept("setNextState") // .setNextState is never accessed outside of a Callback

  val Component = ScalaComponent.builder[Props]("TriStateCheckbox")
    .stateless
    .noBackend
    .render(i => render(i.mountedPure, i.props))
    .componentDidMount(i => updateDom(i.mountedImpure, i.props))
    .componentDidUpdate(i => updateDom(i.mountedImpure, i.currentProps))
    .configure(Reusability.shouldComponentUpdate)
    .build
}