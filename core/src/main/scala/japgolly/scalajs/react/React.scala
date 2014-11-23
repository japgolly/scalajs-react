package japgolly.scalajs.react

import org.scalajs.dom
import scala.scalajs.js
import js.{Dynamic, UndefOr, ThisFunction, ThisFunction0, Object, Number, Any => JAny, Function => JFn}
import js.annotation.{JSBracketAccess, JSName}

object React extends Object {

  /**
   * Create a component given a specification. A component implements a render method which returns one single child.
   * That child may have an arbitrarily deep child structure. One thing that makes components different than standard
   * prototypal classes is that you don't need to call new on them. They are convenience wrappers that construct
   * backing instances (via new) for you.
   */
  def createClass[P,S,B,N <: TopNode](spec: ReactComponentSpec[P,S,B,N]): ReactComponentType[P,S,B,N] = ???

  def createFactory[P,S,B,N <: TopNode](t: ReactComponentType[P,S,B,N]): ReactComponentCU[P,S,B,N] = ???

  def createElement[P,S,B,N <: TopNode](t: ReactComponentType[P,S,B,N]): ReactComponentCU[P,S,B,N] = ???
  def createElement(tag: String, props: Object, children: ReactNode*): ReactDOMElement = ???

  def render(e: ReactElement, n: dom.Node): ReactComponentM_[TopNode] = ???
  def render(e: ReactElement, n: dom.Node, callback: ThisFunction): ReactComponentM_[TopNode] = ???
  def render[P,S,B,N <: TopNode](c: ReactComponentU[P,S,B,N], n: dom.Node): ReactComponentM[P,S,B,N] = ???
  def render[P,S,B,N <: TopNode](c: ReactComponentU[P,S,B,N], n: dom.Node, callback: ThisFunction0[ReactComponentM[P,S,B,N], Unit]): ReactComponentM[P,S,B,N] = ???

  /**
   * Remove a mounted React component from the DOM and clean up its event handlers and state. If no component was
   * mounted in the container, calling this function does nothing. Returns true if a component was unmounted and false
   * if there was no component to unmount.
   */
  def unmountComponentAtNode(container: dom.Node): Boolean = ???

  /**
   * Render a ReactElement to its initial HTML. This should only be used on the server. React will return an HTML
   * string. You can use this method to generate HTML on the server and send the markup down on the initial request for
   * faster page loads and to allow search engines to crawl your pages for SEO purposes.
   *
   * If you call React.render() on a node that already has this server-rendered markup, React will preserve it and only
   * attach event handlers, allowing you to have a very performant first-load experience.
   */
  def renderToString(e: ReactElement): String = ???

  /**
   * Similar to renderToString, except this doesn't create extra DOM attributes such as data-react-id, that React uses
   * internally. This is useful if you want to use React as a simple static page generator, as stripping away the extra
   * attributes can save lots of bytes.
   */
  def renderToStaticMarkup(e: ReactElement): String = ???

  /** Verifies the object is a ReactElement. */
  def isValidElement(o: JAny): Boolean = ???

  /** Configure React's event system to handle touch events on mobile devices. */
  def initializeTouchEvents(shouldUseTouch: Boolean): Unit = ???

  /**
   * React.DOM provides convenience wrappers around React.createElement for DOM components. These should only be used
   * when not using JSX. For example, React.DOM.div(null, 'Hello World!')
   */
  def DOM: Dynamic = ???

  def addons: Dynamic = ???

  /** React.Children provides utilities for dealing with the this.props.children opaque data structure. */
  def Children: ReactChildren = ???

  @deprecated("React.renderComponent will be deprecated in a future version. Use React.render instead.", "React 0.12.0")
  def renderComponent(c: ReactComponentU_, n: dom.Node): ReactComponentM_[TopNode] = ???

  @deprecated("React.renderComponent will be deprecated in a future version. Use React.render instead.", "React 0.12.0")
  def renderComponent(c: ReactComponentU_, n: dom.Node, callback: ThisFunction): ReactComponentM_[TopNode] = ???

  @deprecated("React.renderComponent will be deprecated in a future version. Use React.render instead.", "React 0.12.0")
  def renderComponent[P, S, B, N <: TopNode](c: ReactComponentU[P, S, B, N], n: dom.Node): ReactComponentM[P, S, B, N] = ???

  @deprecated("React.renderComponent will be deprecated in a future version. Use React.render instead.", "React 0.12.0")
  def renderComponent[P, S, B, N <: TopNode](c: ReactComponentU[P, S, B, N], n: dom.Node, callback: ThisFunction0[ReactComponentM[P, S, B, N], Unit]): ReactComponentM[P, S, B, N] = ???

  @deprecated("React.renderComponentToString will be deprecated in a future version. Use React.renderToString instead.", "React 0.12.0")
  def renderComponentToString(component: ReactComponentU_): String = ???

  @deprecated("React.renderComponentToStaticMarkup will be deprecated in a future version. Use React.renderToStaticMarkup instead.", "React 0.12.0")
  def renderComponentToStaticMarkup(component: ReactComponentU_): String = ???
}

/** `React.Children` */
trait ReactChildren extends Object {
  /** Invoke fn on every immediate child contained within children with this set to context. If children is a nested object or array it will be traversed: fn will never be passed the container objects. If children is null or undefined returns null or undefined rather than an empty object. */
  def map(c: PropsChildren, fn: js.Function1[ReactNode, JAny]): UndefOr[Object] = ???
  /** Invoke fn on every immediate child contained within children with this set to context. If children is a nested object or array it will be traversed: fn will never be passed the container objects. If children is null or undefined returns null or undefined rather than an empty object. */
  def map(c: PropsChildren, fn: js.Function2[ReactNode, Number, JAny]): UndefOr[Object] = ???

  /** Like React.Children.map() but does not return an object. */
  def forEach(c: PropsChildren, fn: js.Function1[ReactNode, JAny]): Unit = ???
  /** Like React.Children.map() but does not return an object. */
  def forEach(c: PropsChildren, fn: js.Function2[ReactNode, Number, JAny]): Unit = ???

  /** Return the only child in children. Throws otherwise. */
  def only(c: PropsChildren): ReactNode = ???

  /** Return the total number of components in children, equal to the number of times that a callback passed to map or forEach would be invoked. */
  def count(c: PropsChildren): Number = ???
}

trait ReactComponentSpec[Props, State, +Backend, +Node <: TopNode] extends Object

/** The meat in React's createClass-createFactory sandwich. */
trait ReactComponentType[Props, State, +Backend, +Node <: TopNode] extends Object

/**
 * http://facebook.github.io/react/docs/glossary.html indicates children can be a super type of ReactElement.
 * Array and null are acceptable, thus this can be 0-n elements.
 */
trait ReactNode extends Object

/** ReactElement = ReactComponentElement | ReactDOMElement  */
trait ReactElement extends Object with ReactNode {
  def key: UndefOr[String] = ???
  def ref: UndefOr[String] = ???
}

/** A React virtual DOM element, such as 'div', 'table', etc. */
trait ReactDOMElement extends ReactElement {
  def `type`: String = ???
  def typ   : String = `type`
  def props : Object = ???
}

/** An instance of a React component. Prefer using the subtype ReactComponentU instead. */
trait ReactComponentElement[Props]
  extends ReactElement
     with ComponentScope_P[Props]

/** A JS function that creates a React component instance. */
trait ReactComponentC_ extends JFn

/** The underlying function that creates a Scala-based React component instance. */
trait ReactComponentCU[Props, State, +Backend, +Node <: TopNode] extends ReactComponentC_ {
  def apply(props: WrapObj[Props], children: ReactNode*): ReactComponentU[Props, State, Backend, Node] = ???
}

/** An unmounted component. Not guaranteed to have been created by Scala, could be a React addon. */
trait ReactComponentU_ extends ReactElement {
  def dynamic = this.asInstanceOf[Dynamic]
}

/** A mounted component. Not guaranteed to have been created by Scala, could be a React addon. */
trait ReactComponentM_[+Node <: TopNode]
  extends ReactComponentU_
     with ComponentScope_M[Node]

/** An unmounted Scala component. */
trait ReactComponentU[Props, State, +Backend, +Node <: TopNode]
  extends ReactComponentU_

/** A mounted Scala component. */
trait ReactComponentM[Props, State, +Backend, +Node <: TopNode]
  extends ReactComponentM_[Node]
     with ComponentScopeMN[Props, State, Backend, Node]

// =====================================================================================================================
// Scope

trait ComponentScope_P[+Props] extends Object {
  @JSName("props") def _props: WrapObj[Props] with PropsMixedIn = ???
}

trait ComponentScope_S[+State] extends Object {
  @JSName("state") def _state: WrapObj[State] = ???
}

trait ComponentScope_SS[State] extends ComponentScope_S[State] {
  @JSName("setState") def _setState(s: WrapObj[State]): Unit = ???
  @JSName("setState") def _setState(s: WrapObj[State], callback: UndefOr[JFn]): Unit = ???
}

trait ComponentScope_B[+Backend] extends Object {
  def backend: Backend = ???
}

trait ComponentScope_PS[-Props, +State] extends Object {
  @JSName("getInitialState") def _getInitialState(s: WrapObj[Props]): WrapObj[State] = ???
}

trait ComponentScope_M[+Node <: TopNode] extends Object {

  /** Can be invoked on any mounted component in order to obtain a reference to its rendered DOM node. */
  def getDOMNode(): Node

  def refs: RefsObject

  /**
   * Can be invoked on any mounted component when you know that some deeper aspect of the component's state has
   * changed without using this.setState().
   */
  def forceUpdate(): Unit
}

/** Type of an unmounted component's `this` scope. */
trait ComponentScopeU[Props, State, +Backend]
  extends ComponentScope_PS[Props, State]
     with ComponentScope_P[Props]
     with ComponentScope_SS[State]
     with ComponentScope_B[Backend]
  // prohibits: ComponentScope_M.*

/** Type of a component's `this` scope during componentWillUpdate. */
trait ComponentScopeWU[Props, +State, +Backend]
  extends ComponentScope_PS[Props, State]
     with ComponentScope_P[Props]
     with ComponentScope_S[State]
     with ComponentScope_B[Backend]
     with ComponentScope_M[TopNode]
  // prohibits: .setState

/** Type of a mounted component's `this` scope. */
trait ComponentScopeM[Props, State, +Backend] extends ComponentScopeMN[Props, State, Backend, TopNode]
trait ComponentScopeMN[Props, State, +Backend, +Node <: TopNode]
  extends ComponentScope_PS[Props, State]
     with ComponentScopeU[Props, State, Backend]
     with ComponentScope_M[Node]

/** Type of a component's `this` scope as is available to backends. */
trait BackendScope[Props, State]
  extends ComponentScope_PS[Props, State]
     with ComponentScope_P[Props]
     with ComponentScope_SS[State]
     with ComponentScope_M[TopNode]
  // prohibits: .backend

/** Type of `this.refs` */
trait RefsObject extends Object {
  @JSBracketAccess
  def apply[Node <: TopNode](key: String): UndefOr[ReactComponentM_[Node]]
}

/** Additional methods that React mixes into `this.props` */
trait PropsMixedIn extends Object {
  def children: PropsChildren = ???
}

/** Type of `this.props.children` */
trait PropsChildren extends Object

// =====================================================================================================================
// Events

/** http://facebook.github.io/react/docs/events.html */
trait SyntheticEvent[+DOMEventTarget <: dom.Node] extends Object {
  val bubbles: Boolean = ???
  val cancelable: Boolean = ???
  val currentTarget: DOMEventTarget = ???
  def defaultPrevented: Boolean = ???
  val eventPhase: Number = ???
  val isTrusted: Boolean = ???
  val nativeEvent: dom.Event = ???
  /**
   * Stops the default action of an element from happening.
   * For example: Prevent a submit button from submitting a form Prevent a link from following the URL
   */
  def preventDefault(): Unit = ???
  /**
   * Stops the bubbling of an event to parent elements, preventing any parent event handlers from being executed.
   */
  def stopPropagation(): Unit = ???
  val target: DOMEventTarget = ???
  val timeStamp: js.Date = ???
  @JSName("type") val eventType: String = ???
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticUIEvent.js */
trait SyntheticUIEvent[+DOMEventTarget <: dom.Node] extends SyntheticEvent[DOMEventTarget] {
  override val nativeEvent: dom.UIEvent = ???
  /**
   * The view attribute identifies the AbstractView from which the event was generated.
   * The un-initialized value of this attribute must be null.
   */
  def view(event: dom.Event): Object = ???
  /**
   * Specifies some detail information about the Event, depending on the type of event.
   * The un-initialized value of this attribute must be 0.
   */
  def detail(event: dom.Event): Number = ???
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
  def clipboardData(event: dom.Event): dom.DataTransfer = ???
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticCompositionEvent.js */
trait SyntheticCompositionEvent[+DOMEventTarget <: dom.Node] extends SyntheticEvent[DOMEventTarget] {
  override val nativeEvent: dom.CompositionEvent = ???
  /**
   * Holds the value of the characters generated by an input method.
   * This may be a single Unicode character or a non-empty sequence of Unicode characters [Unicode].
   * Characters should be normalized as defined by the Unicode normalization form NFC, defined in [UAX #15].
   * This attribute may be null or contain the empty string.
   *
   * http://www.w3.org/TR/DOM-Level-3-Events/#events-compositionevents
   */
  val data: String = ???
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticDragEvent.js */
trait SyntheticDragEvent[+DOMEventTarget <: dom.Node] extends SyntheticMouseEvent[DOMEventTarget] {
  override val nativeEvent: dom.DragEvent = ???
  val dataTransfer: dom.DataTransfer = ???
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticFocusEvent.js */
trait SyntheticFocusEvent[+DOMEventTarget <: dom.Node] extends SyntheticUIEvent[DOMEventTarget] {
  override val nativeEvent: dom.FocusEvent = ???
  val relatedTarget: dom.EventTarget = ???
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
//  val data: String = ???
//}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticKeyboardEvent.js */
trait SyntheticKeyboardEvent[+DOMEventTarget <: dom.Node] extends SyntheticUIEvent[DOMEventTarget] {
  override val nativeEvent: dom.KeyboardEvent = ???
  /** See org.scalajs.dom.extensions.KeyValue */
  val key: String = ???
  val location: Number = ???
  val altKey: Boolean = ???
  val ctrlKey: Boolean = ???
  val metaKey: Boolean = ???
  val shiftKey: Boolean = ???
  val repeat: Boolean = ???
  val locale: String = ???
  def getModifierState(keyArg: String): Boolean = ???
  //  charCode: function(event) {
  //  keyCode: function(event) {
  //  which: function(event) {
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticMouseEvent.js */
trait SyntheticMouseEvent[+DOMEventTarget <: dom.Node] extends SyntheticUIEvent[DOMEventTarget] {
  override val nativeEvent: dom.MouseEvent = ???
  val screenX: Number = ???
  val screenY: Number = ???
  val clientX: Number = ???
  val clientY: Number = ???
  val altKey: Boolean = ???
  val ctrlKey: Boolean = ???
  val metaKey: Boolean = ???
  val shiftKey: Boolean = ???
  def getModifierState(keyArg: String): Boolean = ???
  def button(event: dom.Event): Number = ???
  val buttons: Number = ???
  def relatedTarget(event: dom.Event): dom.EventTarget = ???
  def pageX(event: dom.Event): Number = ???
  def pageY(event: dom.Event): Number = ???
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticTouchEvent.js */
trait SyntheticTouchEvent[+DOMEventTarget <: dom.Node] extends SyntheticUIEvent[DOMEventTarget] {
  override val nativeEvent: dom.TouchEvent = ???
  val altKey: Boolean = ???
  val ctrlKey: Boolean = ???
  val metaKey: Boolean = ???
  val shiftKey: Boolean = ???
  def getModifierState(keyArg: String): Boolean = ???
  val touches: dom.TouchList
  val targetTouches: dom.TouchList
  val changedTouches: dom.TouchList
}

/** https://github.com/facebook/react/blob/master/src/browser/syntheticEvents/SyntheticWheelEvent.js */
trait SyntheticWheelEvent[+DOMEventTarget <: dom.Node] extends SyntheticMouseEvent[DOMEventTarget] {
  override val nativeEvent: dom.WheelEvent = ???
  def deltaX(event: dom.Event): Number = ???
  def deltaY(event: dom.Event): Number = ???
  val deltaZ: Number = ???
  /**
   * Browsers without "deltaMode" is reporting in raw wheel delta where one
   * notch on the scroll is always +/- 120, roughly equivalent to pixels.
   * A good approximation of DOM_DELTA_LINE (1) is 5% of viewport size or
   * ~40 pixels, for DOM_DELTA_SCREEN (2) it is 87.5% of viewport size.
   */
  val deltaMode: Number = ???
}
