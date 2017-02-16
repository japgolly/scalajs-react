package japgolly.scalajs.react

import org.scalajs.dom
import org.scalajs.dom.html

trait ReactEventTypes {
  final type ReactEvent            = raw.SyntheticEvent           [dom.Node]
  final type ReactAnimationEvent   = raw.SyntheticAnimationEvent  [dom.Node]
  final type ReactClipboardEvent   = raw.SyntheticClipboardEvent  [dom.Node]
  final type ReactCompositionEvent = raw.SyntheticCompositionEvent[dom.Node]
  final type ReactDragEvent        = raw.SyntheticDragEvent       [dom.Node]
  final type ReactFocusEvent       = raw.SyntheticFocusEvent      [dom.Node]
  //final type ReactInputEvent     = raw.SyntheticInputEvent      [dom.Node]
  final type ReactKeyboardEvent    = raw.SyntheticKeyboardEvent   [dom.Node]
  final type ReactMouseEvent       = raw.SyntheticMouseEvent      [dom.Node]
  final type ReactTouchEvent       = raw.SyntheticTouchEvent      [dom.Node]
  final type ReactTransitionEvent  = raw.SyntheticTransitionEvent [dom.Node]
  final type ReactUIEvent          = raw.SyntheticUIEvent         [dom.Node]
  final type ReactWheelEvent       = raw.SyntheticWheelEvent      [dom.Node]

  final type ReactEventH            = raw.SyntheticEvent           [html.Element]
  final type ReactAnimationEventH   = raw.SyntheticAnimationEvent  [html.Element]
  final type ReactClipboardEventH   = raw.SyntheticClipboardEvent  [html.Element]
  final type ReactCompositionEventH = raw.SyntheticCompositionEvent[html.Element]
  final type ReactDragEventH        = raw.SyntheticDragEvent       [html.Element]
  final type ReactFocusEventH       = raw.SyntheticFocusEvent      [html.Element]
  //final type ReactInputEventH     = raw.SyntheticInputEvent      [html.Element]
  final type ReactKeyboardEventH    = raw.SyntheticKeyboardEvent   [html.Element]
  final type ReactMouseEventH       = raw.SyntheticMouseEvent      [html.Element]
  final type ReactTouchEventH       = raw.SyntheticTouchEvent      [html.Element]
  final type ReactTransitionEventH  = raw.SyntheticTransitionEvent [html.Element]
  final type ReactUIEventH          = raw.SyntheticUIEvent         [html.Element]
  final type ReactWheelEventH       = raw.SyntheticWheelEvent      [html.Element]

  final type ReactEventI            = raw.SyntheticEvent           [html.Input]
  final type ReactAnimationEventI   = raw.SyntheticAnimationEvent  [html.Input]
  final type ReactClipboardEventI   = raw.SyntheticClipboardEvent  [html.Input]
  final type ReactCompositionEventI = raw.SyntheticCompositionEvent[html.Input]
  final type ReactDragEventI        = raw.SyntheticDragEvent       [html.Input]
  final type ReactFocusEventI       = raw.SyntheticFocusEvent      [html.Input]
  //final type ReactInputEventI     = raw.SyntheticInputEvent      [html.Input]
  final type ReactKeyboardEventI    = raw.SyntheticKeyboardEvent   [html.Input]
  final type ReactMouseEventI       = raw.SyntheticMouseEvent      [html.Input]
  final type ReactTouchEventI       = raw.SyntheticTouchEvent      [html.Input]
  final type ReactTransitionEventI  = raw.SyntheticTransitionEvent [html.Input]
  final type ReactUIEventI          = raw.SyntheticUIEvent         [html.Input]
  final type ReactWheelEventI       = raw.SyntheticWheelEvent      [html.Input]

  final type ReactEventTA            = raw.SyntheticEvent           [html.TextArea]
  final type ReactAnimationEventTA   = raw.SyntheticAnimationEvent  [html.TextArea]
  final type ReactClipboardEventTA   = raw.SyntheticClipboardEvent  [html.TextArea]
  final type ReactCompositionEventTA = raw.SyntheticCompositionEvent[html.TextArea]
  final type ReactDragEventTA        = raw.SyntheticDragEvent       [html.TextArea]
  final type ReactFocusEventTA       = raw.SyntheticFocusEvent      [html.TextArea]
  //final type ReactInputEventTA     = raw.SyntheticInputEvent      [html.TextArea]
  final type ReactKeyboardEventTA    = raw.SyntheticKeyboardEvent   [html.TextArea]
  final type ReactMouseEventTA       = raw.SyntheticMouseEvent      [html.TextArea]
  final type ReactTouchEventTA       = raw.SyntheticTouchEvent      [html.TextArea]
  final type ReactTransitionEventTA  = raw.SyntheticTransitionEvent [html.TextArea]
  final type ReactUIEventTA          = raw.SyntheticUIEvent         [html.TextArea]
  final type ReactWheelEventTA       = raw.SyntheticWheelEvent      [html.TextArea]

  implicit def toReactExt_DomEvent                         (e: dom.Event                    ): ReactExt_DomEvent              = new ReactExt_DomEvent(e)
  implicit def toReactExt_ReactEvent[E <: ReactEvent]      (e: E                            ): ReactExt_ReactEvent[E]         = new ReactExt_ReactEvent(e)
  implicit def toReactExt_ReactKeyboardEvent[N <: dom.Node](e: raw.SyntheticKeyboardEvent[N]): ReactExt_ReactKeyboardEvent[N] = new ReactExt_ReactKeyboardEvent(e)
}

final class ReactExt_DomEvent(private val e: dom.Event) extends AnyVal {
  /**
   * Stops the default action of an element from happening.
   * For example: Prevent a submit button from submitting a form Prevent a link from following the URL
   */
  def preventDefaultCB = Callback(e.preventDefault())

  /**
   * Stops the bubbling of an event to parent elements, preventing any parent event handlers from being executed.
   */
  def stopPropagationCB = Callback(e.stopPropagation())
}

final class ReactExt_ReactEvent[E <: ReactEvent](private val e: E) extends AnyVal {
  /**
   * Stops the default action of an element from happening.
   * For example: Prevent a submit button from submitting a form Prevent a link from following the URL
   */
  def preventDefaultCB = Callback(e.preventDefault())

  /**
   * Stops the bubbling of an event to parent elements, preventing any parent event handlers from being executed.
   */
  def stopPropagationCB = Callback(e.stopPropagation())

  /**
   * If you want to access the event properties in an asynchronous way (eg. in a `modState(…)` function),
   * React will have recycled the event by the time the asynchronous call executes.
   *
   * This convenience function extracts a value from the event synchronously (i.e. now!) and so that it is
   * available to the asynchronous code.
   */
  @inline def extract[A, B](getNow: E => A)(useAsync: A => B): B = {
    val a = getNow(e)
    useAsync(a)
  }
}

final class ReactExt_ReactKeyboardEvent[N <: dom.Node](private val e: raw.SyntheticKeyboardEvent[N]) extends AnyVal {

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