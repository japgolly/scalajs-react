package japgolly.scalajs.react

import org.scalajs.dom
import scala.scalajs.js
import js.annotation.{JSBracketAccess, JSName}

object React extends js.Object {

  /**
   * Create a component given a specification. A component implements a render method which returns one single child.
   * That child may have an arbitrarily deep child structure. One thing that makes components different than standard
   * prototypal classes is that you don't need to call new on them. They are convenience wrappers that construct
   * backing instances (via new) for you.
   */
  def createClass[P, S, B](spec: ComponentSpec[P, S, B]): ComponentConstructor[P, S, B] = ???

  def renderComponent(c: ReactComponentU_, n: dom.Node): js.Dynamic = ???
  def renderComponent(c: ReactComponentU_, n: dom.Node, callback: js.ThisFunction): js.Dynamic = ???
  def renderComponent[P, S, B](c: ReactComponentU[P, S, B], n: dom.Node): MountedComponent[P, S, B] = ???
  def renderComponent[P, S, B](c: ReactComponentU[P, S, B], n: dom.Node, callback: js.ThisFunction0[ComponentScopeM[P, S, B], Unit]): MountedComponent[P, S, B] = ???

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

  def DOM: js.Dynamic = ???
  def addons: js.Dynamic = ???
}

/** A React DOM representation of HTML. Could be Scalatags.render output, or a React component. */
trait VDom extends js.Object

trait ComponentSpec[Props, State, Backend] extends js.Object

trait ComponentConstructor[Props, State, Backend] extends js.Function {
  def apply(props: WrapObj[Props], children: js.Any*): ReactComponentU[Props, State, Backend] = ???
}

/** An unmounted component. Not guaranteed to have been created by Scala, could be a React addon. */
trait ReactComponentU_ extends js.Object with VDom

/** An unmounted component with known PSB types. */
trait ReactComponentU[Props, State, Backend] extends js.Object with VDom

trait ReactComponentM[Node <: dom.Element] extends ReactComponentU_ {
  def getDOMNode(): Node
}

/** http://facebook.github.io/react/docs/events.html */
trait SyntheticEvent[DOMEventTarget <: dom.Node] extends js.Object {
  val bubbles: Boolean = ???
  val cancelable: Boolean = ???
  val currentTarget: DOMEventTarget = ???
  def defaultPrevented: Boolean = ???
  val eventPhase: Number = ???
  val isTrusted: Boolean = ???
  val nativeEvent: dom.Event = ???
  def preventDefault(): Unit = ???
  def stopPropagation(): Unit = ???
  val target: DOMEventTarget = ???
  val timeStamp: js.Date = ???
  @JSName("type") val eventType: String = ???
}

// =====================================================================================================================
// Scope

trait ComponentScope_P[Props] extends js.Object {
  @JSName("props") def _props: WrapObj[Props] = ???
}

trait ComponentScope_S[State] extends js.Object {
  @JSName("state") def _state: WrapObj[State] = ???
}

trait ComponentScope_SS[State] extends ComponentScope_S[State] {
  @JSName("setState") def _setState(s: WrapObj[State]): Unit = ???
  @JSName("setState") def _setState(s: WrapObj[State], callback: js.UndefOr[js.Function]): Unit = ???
}

trait ComponentScope_B[Backend] extends js.Object {
  def backend: Backend = ???
}

trait ComponentScope_M extends js.Object {
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
  extends ComponentScope_P[Props]
  with ComponentScope_SS[State]
  with ComponentScope_B[Backend]
  // prohibits: ComponentScope_M.*

/** Type of a component's `this` scope during componentWillUpdate. */
trait ComponentScopeWU[Props, State, Backend]
  extends ComponentScope_P[Props]
  with ComponentScope_S[State]
  with ComponentScope_B[Backend]
  with ComponentScope_M
  // prohibits: .setState

/** Type of a mounted component's `this` scope. */
trait ComponentScopeM[Props, State, Backend]
  extends ComponentScopeU[Props, State, Backend]
  with ComponentScope_M

/** Type of a component's `this` scope as is available to backends. */
trait BackendScope[Props, State]
  extends ComponentScope_P[Props]
  with ComponentScope_SS[State]
  with ComponentScope_M
  // prohibits: .backend

/** Type of `this.refs` */
trait RefsObject extends js.Object {
  @JSBracketAccess
  def apply[Node <: dom.Element](key: js.String): js.UndefOr[ReactComponentM[Node]]
}
