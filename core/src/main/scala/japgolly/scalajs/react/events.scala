package japgolly.scalajs.react

import org.scalajs.dom
import org.scalajs.dom.html

trait ReactEventTypes {
  final type            ReactEventFrom[+N <: dom.Node] = raw.            SyntheticEvent[N]
  final type   ReactAnimationEventFrom[+N <: dom.Node] = raw.   SyntheticAnimationEvent[N]
  final type   ReactClipboardEventFrom[+N <: dom.Node] = raw.   SyntheticClipboardEvent[N]
  final type ReactCompositionEventFrom[+N <: dom.Node] = raw. SyntheticCompositionEvent[N]
  final type        ReactDragEventFrom[+N <: dom.Node] = raw.        SyntheticDragEvent[N]
  final type       ReactFocusEventFrom[+N <: dom.Node] = raw.       SyntheticFocusEvent[N]
  //final type     ReactInputEventFrom[+N <: dom.Node] = raw.       SyntheticInputEvent[N]
  final type    ReactKeyboardEventFrom[+N <: dom.Node] = raw.    SyntheticKeyboardEvent[N]
  final type       ReactMouseEventFrom[+N <: dom.Node] = raw.       SyntheticMouseEvent[N]
  final type     ReactPointerEventFrom[+N <: dom.Node] = raw.     SyntheticPointerEvent[N]
  final type       ReactTouchEventFrom[+N <: dom.Node] = raw.       SyntheticTouchEvent[N]
  final type  ReactTransitionEventFrom[+N <: dom.Node] = raw.  SyntheticTransitionEvent[N]
  final type          ReactUIEventFrom[+N <: dom.Node] = raw.          SyntheticUIEvent[N]
  final type       ReactWheelEventFrom[+N <: dom.Node] = raw.       SyntheticWheelEvent[N]

  final type            ReactEvent =            ReactEventFrom[dom.Node]
  final type   ReactAnimationEvent =   ReactAnimationEventFrom[dom.Node]
  final type   ReactClipboardEvent =   ReactClipboardEventFrom[dom.Node]
  final type ReactCompositionEvent = ReactCompositionEventFrom[dom.Node]
  final type        ReactDragEvent =        ReactDragEventFrom[dom.Node]
  final type       ReactFocusEvent =       ReactFocusEventFrom[dom.Node]
  //final type     ReactInputEvent =       ReactInputEventFrom[dom.Node]
  final type    ReactKeyboardEvent =    ReactKeyboardEventFrom[dom.Node]
  final type       ReactMouseEvent =       ReactMouseEventFrom[dom.Node]
  final type     ReactPointerEvent =     ReactPointerEventFrom[dom.Node]
  final type       ReactTouchEvent =       ReactTouchEventFrom[dom.Node]
  final type  ReactTransitionEvent =  ReactTransitionEventFrom[dom.Node]
  final type          ReactUIEvent =          ReactUIEventFrom[dom.Node]
  final type       ReactWheelEvent =       ReactWheelEventFrom[dom.Node]

  final type            ReactEventFromHtml =            ReactEventFrom[html.Element]
  final type   ReactAnimationEventFromHtml =   ReactAnimationEventFrom[html.Element]
  final type   ReactClipboardEventFromHtml =   ReactClipboardEventFrom[html.Element]
  final type ReactCompositionEventFromHtml = ReactCompositionEventFrom[html.Element]
  final type        ReactDragEventFromHtml =        ReactDragEventFrom[html.Element]
  final type       ReactFocusEventFromHtml =       ReactFocusEventFrom[html.Element]
  //final type     ReactInputEventFromHtml =       ReactInputEventFrom[html.Element]
  final type    ReactKeyboardEventFromHtml =    ReactKeyboardEventFrom[html.Element]
  final type       ReactMouseEventFromHtml =       ReactMouseEventFrom[html.Element]
  final type     ReactPointerEventFromHtml =     ReactPointerEventFrom[html.Element]
  final type       ReactTouchEventFromHtml =       ReactTouchEventFrom[html.Element]
  final type  ReactTransitionEventFromHtml =  ReactTransitionEventFrom[html.Element]
  final type          ReactUIEventFromHtml =          ReactUIEventFrom[html.Element]
  final type       ReactWheelEventFromHtml =       ReactWheelEventFrom[html.Element]

  final type            ReactEventFromInput =            ReactEventFrom[html.Input]
  final type   ReactAnimationEventFromInput =   ReactAnimationEventFrom[html.Input]
  final type   ReactClipboardEventFromInput =   ReactClipboardEventFrom[html.Input]
  final type ReactCompositionEventFromInput = ReactCompositionEventFrom[html.Input]
  final type        ReactDragEventFromInput =        ReactDragEventFrom[html.Input]
  final type       ReactFocusEventFromInput =       ReactFocusEventFrom[html.Input]
  //final type     ReactInputEventFromInput =       ReactInputEventFrom[html.Input]
  final type    ReactKeyboardEventFromInput =    ReactKeyboardEventFrom[html.Input]
  final type       ReactMouseEventFromInput =       ReactMouseEventFrom[html.Input]
  final type     ReactPointerEventFromInput =     ReactPointerEventFrom[html.Input]
  final type       ReactTouchEventFromInput =       ReactTouchEventFrom[html.Input]
  final type  ReactTransitionEventFromInput =  ReactTransitionEventFrom[html.Input]
  final type          ReactUIEventFromInput =          ReactUIEventFrom[html.Input]
  final type       ReactWheelEventFromInput =       ReactWheelEventFrom[html.Input]

  final type            ReactEventFromTextArea =            ReactEventFrom[html.TextArea]
  final type   ReactAnimationEventFromTextArea =   ReactAnimationEventFrom[html.TextArea]
  final type   ReactClipboardEventFromTextArea =   ReactClipboardEventFrom[html.TextArea]
  final type ReactCompositionEventFromTextArea = ReactCompositionEventFrom[html.TextArea]
  final type        ReactDragEventFromTextArea =        ReactDragEventFrom[html.TextArea]
  final type       ReactFocusEventFromTextArea =       ReactFocusEventFrom[html.TextArea]
  //final type  ReactTextAreaEventFromTextArea =    ReactTextAreaEventFrom[html.TextArea]
  final type    ReactKeyboardEventFromTextArea =    ReactKeyboardEventFrom[html.TextArea]
  final type       ReactMouseEventFromTextArea =       ReactMouseEventFrom[html.TextArea]
  final type     ReactPointerEventFromTextArea =     ReactPointerEventFrom[html.TextArea]
  final type       ReactTouchEventFromTextArea =       ReactTouchEventFrom[html.TextArea]
  final type  ReactTransitionEventFromTextArea =  ReactTransitionEventFrom[html.TextArea]
  final type          ReactUIEventFromTextArea =          ReactUIEventFrom[html.TextArea]
  final type       ReactWheelEventFromTextArea =       ReactWheelEventFrom[html.TextArea]

  implicit def toReactExt_DomEvent                         (e: dom.Event                ): ReactExt_DomEvent              = new ReactExt_DomEvent(e)
  implicit def toReactExt_ReactEvent[E <: ReactEvent]      (e: E                        ): ReactExt_ReactEvent[E]         = new ReactExt_ReactEvent(e)
  implicit def toReactExt_ReactKeyboardEvent[N <: dom.Node](e: ReactKeyboardEventFrom[N]): ReactExt_ReactKeyboardEvent[N] = new ReactExt_ReactKeyboardEvent(e)
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