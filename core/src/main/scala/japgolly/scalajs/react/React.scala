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
  def createClass[P, S, B](spec: ComponentSpec[P, S, B]): ComponentConstructor[P, S, B] = ???

  def renderComponent(c: ReactComponentU_, n: dom.Node): Dynamic = ???
  def renderComponent(c: ReactComponentU_, n: dom.Node, callback: ThisFunction): Dynamic = ???
  def renderComponent[P, S, B](c: ReactComponentU[P, S, B], n: dom.Node): MountedComponent[P, S, B] = ???
  def renderComponent[P, S, B](c: ReactComponentU[P, S, B], n: dom.Node, callback: ThisFunction0[ComponentScopeM[P, S, B], Unit]): MountedComponent[P, S, B] = ???

  /** Configure React's event system to handle touch events on mobile devices. */
  def initializeTouchEvents(shouldUseTouch: Boolean): Unit = ???

  /**
   * Remove a mounted React component from the DOM and clean up its event handlers and state. If no component was
   * mounted in the container, calling this function does nothing. Returns true if a component was unmounted and false
   * if there was no component to unmount.
   */
  def unmountComponentAtNode(container: dom.Node): Boolean = ???

  def renderComponentToString(component: ReactComponentU_): String = ???
  def renderComponentToString(component: ReactComponentU[_, _, _]): String = ???

  def renderComponentToStaticMarkup(component: ReactComponentU_): String = ???
  def renderComponentToStaticMarkup(component: ReactComponentU[_, _, _]): String = ???

  def DOM: Dynamic = ???
  def addons: Dynamic = ???

  def Children: ReactChildren = ???
}

/** `React.Children` */
trait ReactChildren extends Object {
  def map(c: PropsChildren, fn: js.Function1[VDom, JAny]): UndefOr[Object] = ???
  def map(c: PropsChildren, fn: js.Function2[VDom, Number, JAny]): UndefOr[Object] = ???
  def forEach(c: PropsChildren, fn: js.Function1[VDom, JAny]): Unit = ???
  def forEach(c: PropsChildren, fn: js.Function2[VDom, Number, JAny]): Unit = ???
  /** WARNING: Throws an exception is exact number of children is not 1. */
  def only(c: PropsChildren): VDom = ???
  def count(c: PropsChildren): Number = ???
}

/** A React DOM representation of HTML. Could be React.DOM output, or a React component. */
trait VDom extends Object

trait ComponentSpec[Props, State, Backend] extends Object

trait ComponentConstructor[Props, State, Backend] extends JFn {
  def apply(props: WrapObj[Props], children: VDom*): ReactComponentU[Props, State, Backend] = ???
}

/** An unmounted component. Not guaranteed to have been created by Scala, could be a React addon. */
trait ReactComponentU_ extends Object with VDom

/** An unmounted component with known PSB types. */
trait ReactComponentU[Props, State, Backend] extends Object with VDom

trait ReactComponentM[Node <: dom.Element] extends ReactComponentU_ {
  def getDOMNode(): Node
}

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

// =====================================================================================================================
// Scope

trait ComponentScope_P[Props] extends Object {
  @JSName("props") def _props: WrapObj[Props] with PropsMixedIn = ???
}

trait ComponentScope_S[State] extends Object {
  @JSName("state") def _state: WrapObj[State] = ???
}

trait ComponentScope_SS[State] extends ComponentScope_S[State] {
  @JSName("setState") def _setState(s: WrapObj[State]): Unit = ???
  @JSName("setState") def _setState(s: WrapObj[State], callback: UndefOr[JFn]): Unit = ???
}

trait ComponentScope_B[Backend] extends Object {
  def backend: Backend = ???
}

trait ComponentScope_PS[Props, State] extends Object {
  @JSName("getInitialState") def _getInitialState(s: WrapObj[Props]): WrapObj[State] = ???
}

trait ComponentScope_M extends Object {
  /** Can be invoked on any mounted component in order to obtain a reference to its rendered DOM node. */
  def getDOMNode(): dom.Element

  /**
   * Can be invoked on any mounted component when you know that some deeper aspect of the component's state has
   * changed without using this.setState().
   */
  def forceUpdate(): Unit

  def refs: RefsObject
}

/** Type of an unmounted component's `this` scope. */
trait ComponentScopeU[Props, State, Backend]
  extends ComponentScope_PS[Props, State]
  with ComponentScope_P[Props]
  with ComponentScope_SS[State]
  with ComponentScope_B[Backend]
  // prohibits: ComponentScope_M.*

/** Type of a component's `this` scope during componentWillUpdate. */
trait ComponentScopeWU[Props, State, Backend]
  extends ComponentScope_PS[Props, State]
  with ComponentScope_P[Props]
  with ComponentScope_S[State]
  with ComponentScope_B[Backend]
  with ComponentScope_M
  // prohibits: .setState

/** Type of a mounted component's `this` scope. */
trait ComponentScopeM[Props, State, Backend]
  extends ComponentScope_PS[Props, State]
  with ComponentScopeU[Props, State, Backend]
  with ComponentScope_M

/** Type of a component's `this` scope as is available to backends. */
trait BackendScope[Props, State]
  extends ComponentScope_PS[Props, State]
  with ComponentScope_P[Props]
  with ComponentScope_SS[State]
  with ComponentScope_M
  // prohibits: .backend

/** Type of `this.refs` */
trait RefsObject extends Object {
  @JSBracketAccess
  def apply[Node <: dom.Element](key: String): UndefOr[ReactComponentM[Node]]
}

/** Additional methods that React mixes into `this.props` */
trait PropsMixedIn extends Object {
  def key: UndefOr[String] = ???
  def children: PropsChildren = ???
}

/** Type of `this.props.children` */
trait PropsChildren extends Object