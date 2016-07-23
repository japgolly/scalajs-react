package japgolly.scalajs.react

/*
import org.scalajs.dom
import org.scalajs.dom.html
import raw._

trait ReactEventAliases {
  final type ReactEvent            = SyntheticEvent           [dom.Node]
  final type ReactClipboardEvent   = SyntheticClipboardEvent  [dom.Node]
  final type ReactCompositionEvent = SyntheticCompositionEvent[dom.Node]
  final type ReactDragEvent        = SyntheticDragEvent       [dom.Node]
  final type ReactFocusEvent       = SyntheticFocusEvent      [dom.Node]
  //final type ReactInputEvent     = SyntheticInputEvent      [dom.Node]
  final type ReactKeyboardEvent    = SyntheticKeyboardEvent   [dom.Node]
  final type ReactMouseEvent       = SyntheticMouseEvent      [dom.Node]
  final type ReactTouchEvent       = SyntheticTouchEvent      [dom.Node]
  final type ReactUIEvent          = SyntheticUIEvent         [dom.Node]
  final type ReactWheelEvent       = SyntheticWheelEvent      [dom.Node]

  final type ReactEventH            = SyntheticEvent           [html.Element]
  final type ReactClipboardEventH   = SyntheticClipboardEvent  [html.Element]
  final type ReactCompositionEventH = SyntheticCompositionEvent[html.Element]
  final type ReactDragEventH        = SyntheticDragEvent       [html.Element]
  final type ReactFocusEventH       = SyntheticFocusEvent      [html.Element]
  //final type ReactInputEventH     = SyntheticInputEvent      [html.Element]
  final type ReactKeyboardEventH    = SyntheticKeyboardEvent   [html.Element]
  final type ReactMouseEventH       = SyntheticMouseEvent      [html.Element]
  final type ReactTouchEventH       = SyntheticTouchEvent      [html.Element]
  final type ReactUIEventH          = SyntheticUIEvent         [html.Element]
  final type ReactWheelEventH       = SyntheticWheelEvent      [html.Element]

  final type ReactEventI            = SyntheticEvent           [html.Input]
  final type ReactClipboardEventI   = SyntheticClipboardEvent  [html.Input]
  final type ReactCompositionEventI = SyntheticCompositionEvent[html.Input]
  final type ReactDragEventI        = SyntheticDragEvent       [html.Input]
  final type ReactFocusEventI       = SyntheticFocusEvent      [html.Input]
  //final type ReactInputEventI     = SyntheticInputEvent      [html.Input]
  final type ReactKeyboardEventI    = SyntheticKeyboardEvent   [html.Input]
  final type ReactMouseEventI       = SyntheticMouseEvent      [html.Input]
  final type ReactTouchEventI       = SyntheticTouchEvent      [html.Input]
  final type ReactUIEventI          = SyntheticUIEvent         [html.Input]
  final type ReactWheelEventI       = SyntheticWheelEvent      [html.Input]

  final type ReactEventTA            = SyntheticEvent           [html.TextArea]
  final type ReactClipboardEventTA   = SyntheticClipboardEvent  [html.TextArea]
  final type ReactCompositionEventTA = SyntheticCompositionEvent[html.TextArea]
  final type ReactDragEventTA        = SyntheticDragEvent       [html.TextArea]
  final type ReactFocusEventTA       = SyntheticFocusEvent      [html.TextArea]
  //final type ReactInputEventTA     = SyntheticInputEvent      [html.TextArea]
  final type ReactKeyboardEventTA    = SyntheticKeyboardEvent   [html.TextArea]
  final type ReactMouseEventTA       = SyntheticMouseEvent      [html.TextArea]
  final type ReactTouchEventTA       = SyntheticTouchEvent      [html.TextArea]
  final type ReactUIEventTA          = SyntheticUIEvent         [html.TextArea]
  final type ReactWheelEventTA       = SyntheticWheelEvent      [html.TextArea]
}

final class ReactKeyboardEventOps[N <: dom.Node](private val e: SyntheticKeyboardEvent[N]) extends AnyVal {

  /**
   * Checks the state of all pressed modifier keys.
   *
   * `e.pressedModifierKeys()` returns `true` if no modifier keys are currently pressed.
   *
   * `e.pressedModifierKeys(altKey = true)` returns `true` if alt is the only modifier key currently pressed.
   */
  def pressedModifierKeys(altKey  : Boolean = false,
                          ctrlKey : Boolean = false,
                          metaKey : Boolean = false,
                          shiftKey: Boolean = false): Boolean =
    e.altKey   == altKey   &&
    e.ctrlKey  == ctrlKey  &&
    e.metaKey  == metaKey  &&
    e.shiftKey == shiftKey
}

object ReactMouseEvent {
  /**
   * Would this mouse event (if applied to a link), open it in a new tab?
   */
  def targetsNewTab_?(e: ReactMouseEvent): Boolean = {
    e.metaKey || e.ctrlKey || // Ctrl-click opens new tab
    e.button == 1             // Middle-click opens new tab
  }
}
*/