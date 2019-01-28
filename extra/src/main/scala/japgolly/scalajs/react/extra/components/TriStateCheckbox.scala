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

  final case class Props(state: State, setNextState: Callback) {
    def render = Component(this)
  }

  implicit def reusabilityState: Reusability[State] =
    Reusability.by_==

  implicit def reusabilityProps: Reusability[Props] =
    Reusability.by(_.state) // .setNextState is never accessed outside of a Callback

  private def render($: ScalaComponent.MountedPure[Props, Unit, Unit]) = {
    val setNext = $.props.flatMap(_.setNextState) // Only access .setNextState inside the Callback for Reusability
    <.input.checkbox(eventHandlers(setNext))
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

  val Component = ScalaComponent.builder[Props]("TriStateCheckbox")
    .render(i => render(i.mountedPure))
    .componentDidMount(i => updateDom(i.mountedImpure, i.props))
    .componentWillReceiveProps(i => updateDom(i.mountedImpure, i.nextProps))
    .configure(Reusability.shouldComponentUpdate)
    .build
}