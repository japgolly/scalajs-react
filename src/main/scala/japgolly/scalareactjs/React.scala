package japgolly.scalareactjs

import org.scalajs.dom
import scala.scalajs.js
import js.annotation.{JSBracketAccess, JSName}

trait React extends js.Object {

  /**
   * Create a component given a specification. A component implements a render method which returns one single child.
   * That child may have an arbitrarily deep child structure. One thing that makes components different than standard
   * prototypal classes is that you don't need to call new on them. They are convenience wrappers that construct
   * backing instances (via new) for you.
   */
  def createClass[Props](specification: ComponentSpec[Props]): ComponentConstructor[Props] = ???

  def renderComponent(c: ProxyConstructorU, n: dom.Node): js.Dynamic = ???
}

/** A React DOM representation of HTML. Could be Scalatags.render output, or a React component. */
trait VDom extends js.Object

trait ComponentSpec[Props] extends js.Object

trait ComponentConstructor[Props] extends js.Object {
  def apply(props: WrapObj[Props], children: js.Any*): ProxyConstructorU = ???
}

/** An unmounted component. Called ProxyConstructor in React-land. */
trait ProxyConstructorU extends js.Object with VDom

trait ProxyConstructorM[Node <: dom.Element] extends ProxyConstructorU {
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
  @JSName("setState") def _setState(s: WrapObj[State], callback: js.Function): Unit = ???
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
trait ComponentScopeB[Props, State]
  extends ComponentScope_P[Props]
  with ComponentScope_SS[State]
  with ComponentScope_M
  // prohibits: .backend

/** Type of `this.refs` */
trait RefsObject extends js.Object {
  @JSBracketAccess
  def apply[Node <: dom.Element](key: js.String): js.UndefOr[ProxyConstructorM[Node]]
}
