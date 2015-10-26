package japgolly.scalajs.react

import org.scalajs.dom
import org.scalajs.dom.html
import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

trait ReactEventAliases {
  type ReactEvent            = SyntheticEvent           [dom.Node]
  type ReactClipboardEvent   = SyntheticClipboardEvent  [dom.Node]
  type ReactCompositionEvent = SyntheticCompositionEvent[dom.Node]
  type ReactDragEvent        = SyntheticDragEvent       [dom.Node]
  type ReactFocusEvent       = SyntheticFocusEvent      [dom.Node]
  //type ReactInputEvent     = SyntheticInputEvent      [dom.Node]
  type ReactKeyboardEvent    = SyntheticKeyboardEvent   [dom.Node]
  type ReactMouseEvent       = SyntheticMouseEvent      [dom.Node]
  type ReactTouchEvent       = SyntheticTouchEvent      [dom.Node]
  type ReactUIEvent          = SyntheticUIEvent         [dom.Node]
  type ReactWheelEvent       = SyntheticWheelEvent      [dom.Node]

  type ReactEventH            = SyntheticEvent           [html.Element]
  type ReactClipboardEventH   = SyntheticClipboardEvent  [html.Element]
  type ReactCompositionEventH = SyntheticCompositionEvent[html.Element]
  type ReactDragEventH        = SyntheticDragEvent       [html.Element]
  type ReactFocusEventH       = SyntheticFocusEvent      [html.Element]
  //type ReactInputEventH     = SyntheticInputEvent      [html.Element]
  type ReactKeyboardEventH    = SyntheticKeyboardEvent   [html.Element]
  type ReactMouseEventH       = SyntheticMouseEvent      [html.Element]
  type ReactTouchEventH       = SyntheticTouchEvent      [html.Element]
  type ReactUIEventH          = SyntheticUIEvent         [html.Element]
  type ReactWheelEventH       = SyntheticWheelEvent      [html.Element]

  type ReactEventI            = SyntheticEvent           [html.Input]
  type ReactClipboardEventI   = SyntheticClipboardEvent  [html.Input]
  type ReactCompositionEventI = SyntheticCompositionEvent[html.Input]
  type ReactDragEventI        = SyntheticDragEvent       [html.Input]
  type ReactFocusEventI       = SyntheticFocusEvent      [html.Input]
  //type ReactInputEventI     = SyntheticInputEvent      [html.Input]
  type ReactKeyboardEventI    = SyntheticKeyboardEvent   [html.Input]
  type ReactMouseEventI       = SyntheticMouseEvent      [html.Input]
  type ReactTouchEventI       = SyntheticTouchEvent      [html.Input]
  type ReactUIEventI          = SyntheticUIEvent         [html.Input]
  type ReactWheelEventI       = SyntheticWheelEvent      [html.Input]

  type ReactEventTA            = SyntheticEvent           [html.TextArea]
  type ReactClipboardEventTA   = SyntheticClipboardEvent  [html.TextArea]
  type ReactCompositionEventTA = SyntheticCompositionEvent[html.TextArea]
  type ReactDragEventTA        = SyntheticDragEvent       [html.TextArea]
  type ReactFocusEventTA       = SyntheticFocusEvent      [html.TextArea]
  //type ReactInputEventTA     = SyntheticInputEvent      [html.TextArea]
  type ReactKeyboardEventTA    = SyntheticKeyboardEvent   [html.TextArea]
  type ReactMouseEventTA       = SyntheticMouseEvent      [html.TextArea]
  type ReactTouchEventTA       = SyntheticTouchEvent      [html.TextArea]
  type ReactUIEventTA          = SyntheticUIEvent         [html.TextArea]
  type ReactWheelEventTA       = SyntheticWheelEvent      [html.TextArea]
}

object ReactKeyboardEvent {
  def checkKeyMods(e       : ReactKeyboardEvent,
                   altKey  : Boolean = false,
                   ctrlKey : Boolean = false,
                   metaKey : Boolean = false,
                   shiftKey: Boolean = false): Boolean =
    e.altKey   == altKey   &&
    e.ctrlKey  == ctrlKey  &&
    e.metaKey  == metaKey  &&
    e.shiftKey == shiftKey
}

/** https://facebook.github.io/react/docs/events.html */
@js.native
trait SyntheticEvent[+DOMEventTarget <: dom.Node] extends js.Object {
  val bubbles         : Boolean        = js.native
  val cancelable      : Boolean        = js.native
  val currentTarget   : DOMEventTarget = js.native
  def defaultPrevented: Boolean        = js.native
  val eventPhase      : Double         = js.native
  val isTrusted       : Boolean        = js.native
  val nativeEvent     : dom.Event      = js.native
  val target          : DOMEventTarget = js.native
  val timeStamp       : js.Date        = js.native
  /**
   * Stops the default action of an element from happening.
   * For example: Prevent a submit button from submitting a form Prevent a link from following the URL
   */
  def preventDefault(): Unit = js.native
  /**
   * Stops the bubbling of an event to parent elements, preventing any parent event handlers from being executed.
   */
  def stopPropagation(): Unit = js.native
  @JSName("type") val eventType: String = js.native
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticUIEvent.js */
@js.native
trait SyntheticUIEvent[+DOMEventTarget <: dom.Node] extends SyntheticEvent[DOMEventTarget] {
  override val nativeEvent: dom.UIEvent = js.native
  /**
   * The view attribute identifies the AbstractView from which the event was generated.
   * The un-initialized value of this attribute must be null.
   */
  def view(event: dom.Event): js.Object = js.native
  /**
   * Specifies some detail information about the Event, depending on the type of event.
   * The un-initialized value of this attribute must be 0.
   */
  def detail(event: dom.Event): Double = js.native
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticClipboardEvent.js */
@js.native
trait SyntheticClipboardEvent[+DOMEventTarget <: dom.Node] extends SyntheticEvent[DOMEventTarget] {
  /**
   * The clipboardData attribute is an instance of the DataTransfer interface which lets a script read and manipulate
   * values on the system clipboard during user-initiated copy, cut and paste operations. The associated drag data store
   * is a live but filtered view of the system clipboard, exposing data types the implementation knows the script can
   * safely access.
   *
   * The clipboardData object's items and files properties enable processing of multi-part or non-textual data from the
   * clipboard.
   *
   * http://www.w3.org/TR/clipboard-apis/#widl-ClipboardEvent-clipboardData
   */
  def clipboardData(event: dom.Event): dom.DataTransfer = js.native
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticCompositionEvent.js */
@js.native
trait SyntheticCompositionEvent[+DOMEventTarget <: dom.Node] extends SyntheticEvent[DOMEventTarget] {
  override val nativeEvent: dom.CompositionEvent = js.native
  /**
   * Holds the value of the characters generated by an input method.
   * This may be a single Unicode character or a non-empty sequence of Unicode characters [Unicode].
   * Characters should be normalized as defined by the Unicode normalization form NFC, defined in [UAX #15].
   * This attribute may be null or contain the empty string.
   *
   * http://www.w3.org/TR/DOM-Level-3-Events/#events-compositionevents
   */
  val data: String = js.native
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticDragEvent.js */
@js.native
trait SyntheticDragEvent[+DOMEventTarget <: dom.Node] extends SyntheticMouseEvent[DOMEventTarget] {
  override val nativeEvent: dom.DragEvent = js.native
  val dataTransfer: dom.DataTransfer = js.native
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticFocusEvent.js */
@js.native
trait SyntheticFocusEvent[+DOMEventTarget <: dom.Node] extends SyntheticUIEvent[DOMEventTarget] {
  override val nativeEvent: dom.FocusEvent = js.native
  val relatedTarget: dom.EventTarget = js.native
}

// DISABLED. input.onchange generates SyntheticEvent not SyntheticInputEvent
///** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticInputEvent.js */
//trait SyntheticInputEvent[+DOMEventTarget <: dom.Node] extends SyntheticEvent[DOMEventTarget] {
//  /**
//   * Holds the value of the characters generated by an input method.
//   * This may be a single Unicode character or a non-empty sequence of Unicode characters [Unicode].
//   * Characters should be normalized as defined by the Unicode normalization form NFC, defined in [UAX #15].
//   * This attribute may be null or contain the empty string.
//   *
//   * http://www.w3.org/TR/2013/WD-DOM-Level-3-Events-20131105/#events-inputevents
//   */
//  val data: String = js.native
//}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticKeyboardEvent.js */
@js.native
trait SyntheticKeyboardEvent[+DOMEventTarget <: dom.Node] extends SyntheticUIEvent[DOMEventTarget] {
  override val nativeEvent: dom.KeyboardEvent = js.native
  /** See org.scalajs.dom.extensions.KeyValue */
  val key      : String  = js.native
  val location : Double  = js.native
  val altKey   : Boolean = js.native
  val ctrlKey  : Boolean = js.native
  val metaKey  : Boolean = js.native
  val shiftKey : Boolean = js.native
  val repeat   : Boolean = js.native
  val locale   : String  = js.native
  def getModifierState(keyArg: String): Boolean = js.native
  //  charCode: function(event) {
  //  keyCode: function(event) {
  //  which: function(event) {
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticMouseEvent.js */
@js.native
trait SyntheticMouseEvent[+DOMEventTarget <: dom.Node] extends SyntheticUIEvent[DOMEventTarget] {
  override val nativeEvent: dom.MouseEvent = js.native
  val screenX  : Double  = js.native
  val screenY  : Double  = js.native
  val clientX  : Double  = js.native
  val clientY  : Double  = js.native
  val buttons  : Int     = js.native
  val altKey   : Boolean = js.native
  val ctrlKey  : Boolean = js.native
  val metaKey  : Boolean = js.native
  val shiftKey : Boolean = js.native
  def getModifierState(keyArg: String)  : Boolean         = js.native
  def relatedTarget   (event: dom.Event): dom.EventTarget = js.native
  def button          (event: dom.Event): Double          = js.native
  def pageX           (event: dom.Event): Double          = js.native
  def pageY           (event: dom.Event): Double          = js.native
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticTouchEvent.js */
@js.native
trait SyntheticTouchEvent[+DOMEventTarget <: dom.Node] extends SyntheticUIEvent[DOMEventTarget] {
  override val nativeEvent: dom.TouchEvent = js.native
  val altKey        : Boolean       = js.native
  val ctrlKey       : Boolean       = js.native
  val metaKey       : Boolean       = js.native
  val shiftKey      : Boolean       = js.native
  val touches       : dom.TouchList = js.native
  val targetTouches : dom.TouchList = js.native
  val changedTouches: dom.TouchList = js.native
  def getModifierState(keyArg: String): Boolean = js.native
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticWheelEvent.js */
@js.native
trait SyntheticWheelEvent[+DOMEventTarget <: dom.Node] extends SyntheticMouseEvent[DOMEventTarget] {
  override val nativeEvent: dom.WheelEvent = js.native
  def deltaX(event: dom.Event): Double = js.native
  def deltaY(event: dom.Event): Double = js.native
  val deltaZ: Double = js.native
  /**
   * Browsers without "deltaMode" is reporting in raw wheel delta where one
   * notch on the scroll is always +/- 120, roughly equivalent to pixels.
   * A good approximation of DOM_DELTA_LINE (1) is 5% of viewport size or
   * ~40 pixels, for DOM_DELTA_SCREEN (2) it is 87.5% of viewport size.
   */
  val deltaMode: Double = js.native
}
