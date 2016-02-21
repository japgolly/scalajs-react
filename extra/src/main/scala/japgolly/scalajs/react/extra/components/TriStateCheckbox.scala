package japgolly.scalajs.react.extra.components

import japgolly.scalajs.react._, vdom.prefix_<^._
import japgolly.scalajs.react.extra._
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html.Input

/**
 * Checkbox that can have three states: Checked, Unchecked, Indeterminate.
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

  private def render($: CompScope.DuringCallbackU[Props, Unit, Unit]) = {
    val setNext = $.propsCB.flatMap(_.setNextState) // Only access .setNextState inside the Callback for Reusability
    <.input.checkbox(eventHandlers(setNext))
  }

  /**
   * Clicking or pressing space = change.
   */
  def eventHandlers(onChange: Callback): TagMod = {
    def handleKey(e: ReactKeyboardEventH): Callback =
      CallbackOption.asEventDefault(e,
        CallbackOption.keyCodeSwitch(e) {
          case KeyCode.Space => onChange
        }
      )
    TagMod(
      ^.onClick   --> onChange,
      ^.onKeyDown ==> handleKey)
  }

  private def updateDom($: CompScope.Mounted[Input], nextProps: Props): Callback = {
    val s = nextProps.state
    Callback {
      val d = $.getDOMNode()
      d.checked       = s == Checked
      d.indeterminate = s == Indeterminate
    }
  }

  val Component = ReactComponentB[Props]("TriStateCheckbox")
    .render(render)
    .domType[Input]
    .componentDidMount($ => updateDom($, $.props))
    .componentWillReceiveProps(i => updateDom(i.$, i.nextProps))
    .configure(Reusability.shouldComponentUpdate)
    .build
}