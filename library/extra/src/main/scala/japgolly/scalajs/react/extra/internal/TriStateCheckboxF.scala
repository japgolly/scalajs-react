package japgolly.scalajs.react.extra.internal

import japgolly.scalajs.react._
import japgolly.scalajs.react.internal.CoreGeneral._
import japgolly.scalajs.react.internal.EffectUtil
import japgolly.scalajs.react.util.DefaultEffects
import japgolly.scalajs.react.util.Effect.Sync
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html.Input

/**
 * Checkbox that can have three states: Checked, Unchecked, Indeterminate.
 *
 * @since 0.11.0
 */
object TriStateCheckboxF {

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
}

class TriStateCheckboxF[F[_]](implicit F: Sync[F]) {

  final type State        = TriStateCheckboxF.State
  final type Determinate  = TriStateCheckboxF.Determinate
  final val Checked       = TriStateCheckboxF.Checked
  final val Unchecked     = TriStateCheckboxF.Unchecked
  final val Indeterminate = TriStateCheckboxF.Indeterminate

  case class Props(state       : State,
                   setNextState: F[Unit],
                   disabled    : Boolean = false,
                   tagMod      : TagMod = TagMod.empty,
                  ) {
    @inline def render: VdomElement = Component(this)
  }

  implicit val reusabilityState: Reusability[State] =
    Reusability.by_==

  val Component = {

    /** Clicking or pressing space = change. */
    def eventHandlers(onChange: F[Unit]): TagMod = {
      def handleKey(e: ReactKeyboardEventFromHtml): F[Unit] =
        F.delay {
          EffectUtil.unsafeAsEventDefaultOption_(e)(
            EffectUtil.unsafeKeyCodeSwitch(e) {
              case KeyCode.Space => F.runSync(onChange)
            }
          )
        }
      TagMod(
        ^.onClick   --> onChange,
        ^.onKeyDown ==> handleKey)
    }

    def updateDom(dom: Input, nextState: State): F[Unit] =
      F.delay {
        dom.checked       = nextState == Checked
        dom.indeterminate = nextState == Indeterminate
      }

    ScalaFnComponent
      .withHooks[Props]
      .useRefToVdom[Input]
      .useEffectWithDepsBy((props, _) => props.state) { (_, ref) => state =>
        F.flatMap(
          F.transSync(ref.get)(DefaultEffects.Sync)
        ) {
          case Some(input) => updateDom(input, state)
          case None        => F.empty
        }
      }
      .render { (props, ref) =>
        val setNext = if (props.disabled) F.empty else props.setNextState

        <.input.checkbox.withRef(ref)(
          props.tagMod,
          ^.disabled := props.disabled,
          TagMod.unless(props.disabled)(eventHandlers(setNext))
        )
      }
  }
}
