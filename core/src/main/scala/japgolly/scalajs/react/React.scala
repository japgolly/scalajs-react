package japgolly.scalajs.react

import org.scalajs.dom
import scala.scalajs.js
import js.{Dynamic, UndefOr, ThisFunction, ThisFunction0, Object, Any => JAny, Function => JFn}
import js.annotation.{JSBracketAccess, JSName}

object React extends React
trait React extends Object {

  /**
   * Create a component given a specification. A component implements a render method which returns one single child.
   * That child may have an arbitrarily deep child structure. One thing that makes components different than standard
   * prototypal classes is that you don't need to call new on them. They are convenience wrappers that construct
   * backing instances (via new) for you.
   */
  def createClass[P,S,B,N <: TopNode](spec: ReactComponentSpec[P,S,B,N]): ReactComponentType[P,S,B,N] = js.native

  def createFactory[P,S,B,N <: TopNode](t: ReactComponentType[P,S,B,N]): ReactComponentCU[P,S,B,N] = js.native
  def createFactory[P,N <: TopNode](t: JSComponentType[P,N]): JSComponentCU[P,N] = js.native

  def createElement[P,S,B,N <: TopNode](t: ReactComponentType[P,S,B,N]): ReactComponentCU[P,S,B,N] = js.native
  def createElement(tag: String, props: Object, children: ReactNode*): ReactDOMElement = js.native

  def render(e: ReactElement, n: dom.Node): ReactComponentM_[TopNode] = js.native
  def render(e: ReactElement, n: dom.Node, callback: ThisFunction): ReactComponentM_[TopNode] = js.native
  def render[P,S,B,N <: TopNode](c: ReactComponentU[P,S,B,N], n: dom.Node): ReactComponentM[P,S,B,N] = js.native
  def render[P,S,B,N <: TopNode](c: ReactComponentU[P,S,B,N], n: dom.Node, callback: ThisFunction0[ReactComponentM[P,S,B,N], Unit]): ReactComponentM[P,S,B,N] = js.native

  /**
   * Remove a mounted React component from the DOM and clean up its event handlers and state. If no component was
   * mounted in the container, calling this function does nothing. Returns true if a component was unmounted and false
   * if there was no component to unmount.
   */
  def unmountComponentAtNode(container: dom.Node): Boolean = js.native

  /**
   * Render a ReactElement to its initial HTML. This should only be used on the server. React will return an HTML
   * string. You can use this method to generate HTML on the server and send the markup down on the initial request for
   * faster page loads and to allow search engines to crawl your pages for SEO purposes.
   *
   * If you call React.render() on a node that already has this server-rendered markup, React will preserve it and only
   * attach event handlers, allowing you to have a very performant first-load experience.
   */
  def renderToString(e: ReactElement): String = js.native

  /**
   * Similar to renderToString, except this doesn't create extra DOM attributes such as data-react-id, that React uses
   * internally. This is useful if you want to use React as a simple static page generator, as stripping away the extra
   * attributes can save lots of bytes.
   */
  def renderToStaticMarkup(e: ReactElement): String = js.native

  /** Verifies the object is a ReactElement. */
  def isValidElement(o: JAny): Boolean = js.native

  /** Configure React's event system to handle touch events on mobile devices. */
  def initializeTouchEvents(shouldUseTouch: Boolean): Unit = js.native

  /**
   * React.DOM provides convenience wrappers around React.createElement for DOM components. These should only be used
   * when not using JSX. For example, React.DOM.div(null, 'Hello World!')
   */
  def DOM: Dynamic = js.native

  def addons: Dynamic = js.native

  /** React.Children provides utilities for dealing with the this.props.children opaque data structure. */
  def Children: ReactChildren = js.native

  @deprecated("React.renderComponent will be deprecated in a future version. Use React.render instead.", "React 0.12.0")
  def renderComponent(c: ReactComponentU_, n: dom.Node): ReactComponentM_[TopNode] = js.native

  @deprecated("React.renderComponent will be deprecated in a future version. Use React.render instead.", "React 0.12.0")
  def renderComponent(c: ReactComponentU_, n: dom.Node, callback: ThisFunction): ReactComponentM_[TopNode] = js.native

  @deprecated("React.renderComponent will be deprecated in a future version. Use React.render instead.", "React 0.12.0")
  def renderComponent[P, S, B, N <: TopNode](c: ReactComponentU[P, S, B, N], n: dom.Node): ReactComponentM[P, S, B, N] = js.native

  @deprecated("React.renderComponent will be deprecated in a future version. Use React.render instead.", "React 0.12.0")
  def renderComponent[P, S, B, N <: TopNode](c: ReactComponentU[P, S, B, N], n: dom.Node, callback: ThisFunction0[ReactComponentM[P, S, B, N], Unit]): ReactComponentM[P, S, B, N] = js.native

  @deprecated("React.renderComponentToString will be deprecated in a future version. Use React.renderToString instead.", "React 0.12.0")
  def renderComponentToString(component: ReactComponentU_): String = js.native

  @deprecated("React.renderComponentToStaticMarkup will be deprecated in a future version. Use React.renderToStaticMarkup instead.", "React 0.12.0")
  def renderComponentToStaticMarkup(component: ReactComponentU_): String = js.native
}

/** `React.Children` */
trait ReactChildren extends Object {
  /** Invoke fn on every immediate child contained within children with this set to context. If children is a nested object or array it will be traversed: fn will never be passed the container objects. If children is null or undefined returns null or undefined rather than an empty object. */
  def map(c: PropsChildren, fn: js.Function1[ReactNode, JAny]): UndefOr[Object] = js.native
  /** Invoke fn on every immediate child contained within children with this set to context. If children is a nested object or array it will be traversed: fn will never be passed the container objects. If children is null or undefined returns null or undefined rather than an empty object. */
  def map(c: PropsChildren, fn: js.Function2[ReactNode, Int, JAny]): UndefOr[Object] = js.native

  /** Like React.Children.map() but does not return an object. */
  def forEach(c: PropsChildren, fn: js.Function1[ReactNode, JAny]): Unit = js.native
  /** Like React.Children.map() but does not return an object. */
  def forEach(c: PropsChildren, fn: js.Function2[ReactNode, Int, JAny]): Unit = js.native

  /** Return the only child in children. Throws otherwise. */
  def only(c: PropsChildren): ReactNode = js.native

  /** Return the total number of components in children, equal to the number of times that a callback passed to map or forEach would be invoked. */
  def count(c: PropsChildren): Int = js.native
}

trait ReactComponentSpec[Props, State, +Backend, +Node <: TopNode] extends Object with ReactComponentTypeAuxJ[Props, State, Backend, Node]

/** The meat in React's createClass-createFactory sandwich. */
trait ReactComponentType[Props, State, +Backend, +Node <: TopNode] extends Object with ReactComponentTypeAuxJ[Props, State, Backend, Node]

trait JSComponentType[Props, +Node <: TopNode] extends Object

/**
 * https://facebook.github.io/react/docs/glossary.html indicates children can be a super type of ReactElement.
 * Array and null are acceptable, thus this can be 0-n elements.
 */
trait ReactNode extends Object

/** ReactElement = ReactComponentElement | ReactDOMElement  */
trait ReactElement extends Object with ReactNode {
  def key: UndefOr[String] = js.native
  def ref: UndefOr[String] = js.native
}

/** A React virtual DOM element, such as 'div', 'table', etc. */
trait ReactDOMElement extends ReactElement {
  def `type`: String = js.native
  def props : Object = js.native
}

/** An instance of a React component. Prefer using the subtype ReactComponentU instead. */
trait ReactComponentElement[Props]
  extends ReactElement
     with ComponentScope_P[Props]

/** A JS function that creates a React component instance. */
trait ReactComponentC_ extends JFn

/** The underlying function that creates a Scala-based React component instance. */
trait ReactComponentCU[Props, State, +Backend, +Node <: TopNode] extends ReactComponentC_ with ReactComponentTypeAuxJ[Props, State, Backend, Node] {
  def apply(props: WrapObj[Props], children: ReactNode*): ReactComponentU[Props, State, Backend, Node] = js.native
}

trait JSComponentCU[Props, +Node <: TopNode] extends ReactComponentC_ with JSComponentType[Props, Node] {
  def apply(props: Props, children: ReactNode*): JSComponentU[Props, Node] = js.native
}

/** An unmounted component. Not guaranteed to have been created by Scala, could be a React addon. */
trait ReactComponentU_ extends ReactElement

/** A mounted component. Not guaranteed to have been created by Scala, could be a React addon. */
trait ReactComponentM_[+Node <: TopNode]
  extends ReactComponentU_
     with ComponentScope_M[Node]

trait JSComponentM[Props, +Node <: TopNode]
  extends JSComponentU[Props, Node]
     with ComponentScope_M[Node] with ReactComponentM_[Node] {
  def props : Props = js.native
}

/** An unmounted Scala component. */
trait ReactComponentU[Props, State, +Backend, +Node <: TopNode]
  extends ReactComponentU_
     with ReactComponentTypeAuxJ[Props, State, Backend, Node]

trait JSComponentU[Props, +Node <: TopNode]
  extends ReactComponentU_
     with JSComponentType[Props, Node]

/** A mounted Scala component. */
trait ReactComponentM[Props, State, +Backend, +Node <: TopNode]
  extends ReactComponentM_[Node]
     with ComponentScopeM[Props, State, Backend, Node]
     with ReactComponentTypeAuxJ[Props, State, Backend, Node]

// =====================================================================================================================
// Scope

/** Methods always available. */
trait ComponentScope_A extends Object {
  def isMounted(): Boolean = js.native
}

trait ComponentScope_P[+Props] extends Object {
  @JSName("props") private[react] def _props: WrapObj[Props] with PropsMixedIn = js.native
}

trait ComponentScope_S[+State] extends Object {
  @JSName("state") private[react] def _state: WrapObj[State] = js.native
}

trait ComponentScope_SS[State] extends ComponentScope_S[State] {
  @JSName("setState") private[react] def _setState(s: WrapObj[State]): Unit = js.native
  @JSName("setState") private[react] def _setState(s: WrapObj[State], callback: UndefOr[JFn]): Unit = js.native
}

trait ComponentScope_B[+Backend] extends Object {
  def backend: Backend = js.native
}

trait ComponentScope_PS[-Props, +State] extends Object {
  @JSName("getInitialState") private[react] def _getInitialState(s: WrapObj[Props]): WrapObj[State] = js.native
}

trait ComponentScope_M[+Node <: TopNode] extends Object {

  /** Can be invoked on any mounted component in order to obtain a reference to its rendered DOM node. */
  def getDOMNode(): Node = js.native

  def refs: RefsObject = js.native

  /**
   * Can be invoked on any mounted component when you know that some deeper aspect of the component's state has
   * changed without using this.setState().
   */
  def forceUpdate(): Unit = js.native
}

/** Type of an unmounted component's `this` scope. */
trait ComponentScopeU[Props, State, +Backend]
  extends ComponentScope_A
     with ComponentScope_PS[Props, State]
     with ComponentScope_P[Props]
     with ComponentScope_SS[State]
     with ComponentScope_B[Backend] {
  // prohibits: ComponentScope_M.*
}

/** Type of a component's `this` scope during componentWillUpdate. */
trait ComponentScopeWU[Props, +State, +Backend, +Node <: TopNode]
  extends ComponentScope_A
     with ComponentScope_PS[Props, State]
     with ComponentScope_P[Props]
     with ComponentScope_S[State]
     with ComponentScope_B[Backend]
     with ComponentScope_M[Node]
  // prohibits: .setState

/** Type of a mounted component's `this` scope. */
trait ComponentScopeM[Props, State, +Backend, +Node <: TopNode]
  extends ComponentScopeU[Props, State, Backend]
     with ReactComponentTypeAuxJ[Props, State, Backend, Node]
     with ComponentScope_PS[Props, State]
     with ComponentScope_M[Node]

/** Type of a component's `this` scope as is available to backends. */
trait BackendScope[Props, State]
  extends ComponentScope_A
     with ComponentScope_PS[Props, State]
     with ComponentScope_P[Props]
     with ComponentScope_SS[State]
     with ComponentScope_M[TopNode]
  // prohibits: .backend

/** Type of `this.refs` */
trait RefsObject extends Object {
  @JSBracketAccess
  def apply[Node <: TopNode](key: String): UndefOr[ReactComponentM_[Node]] = js.native
}

/** Additional methods that React mixes into `this.props` */
trait PropsMixedIn extends Object {
  def children: PropsChildren = js.native
}

/** Type of `this.props.children` */
trait PropsChildren extends Object

// =====================================================================================================================
// Events

/** https://facebook.github.io/react/docs/events.html */
trait SyntheticEvent[+DOMEventTarget <: dom.Node] extends Object {
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
trait SyntheticUIEvent[+DOMEventTarget <: dom.Node] extends SyntheticEvent[DOMEventTarget] {
  override val nativeEvent: dom.UIEvent = js.native
  /**
   * The view attribute identifies the AbstractView from which the event was generated.
   * The un-initialized value of this attribute must be null.
   */
  def view(event: dom.Event): Object = js.native
  /**
   * Specifies some detail information about the Event, depending on the type of event.
   * The un-initialized value of this attribute must be 0.
   */
  def detail(event: dom.Event): Double = js.native
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticClipboardEvent.js */
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
trait SyntheticDragEvent[+DOMEventTarget <: dom.Node] extends SyntheticMouseEvent[DOMEventTarget] {
  override val nativeEvent: dom.DragEvent = js.native
  val dataTransfer: dom.DataTransfer = js.native
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticFocusEvent.js */
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
