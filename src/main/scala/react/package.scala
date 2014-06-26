package golly

import org.scalajs.dom
import org.scalajs.dom.console
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSBracketAccess, JSName}
import scalaz.LensFamily

package object react {

  @inline def React = js.Dynamic.global.React.asInstanceOf[React]

  // ===================================================================================================================

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

  /** Type of HTML rendered in React's virtual DOM. */
  trait VDom extends js.Object

  trait ComponentSpec[Props] extends js.Object

  trait ComponentScope_PS[Props, State] extends js.Object {
    @JSName("props") def _props: WrapObj[Props] = ???
    @JSName("state") def _state: WrapObj[State] = ???
  }

  trait ComponentScope_PSS[Props, State] extends ComponentScope_PS[Props, State] {
    @JSName("setState") def _setState(s: WrapObj[State]): Unit = ???
  }

  trait ComponentScope_B[Backend] extends js.Object {
    def _backend: WrapObj[Backend] = ???
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
  trait ComponentScopeU[Props, State, Backend] extends ComponentScope_PSS[Props, State] with ComponentScope_B[Backend]

  /** Type of a component's `this` scope during willUpdate. */
  trait ComponentScopeWU[Props, State, Backend]
    extends ComponentScope_PS[Props, State]
    with ComponentScope_B[Backend]
    with ComponentScope_M

  /** Type of a mounted component's `this` scope. */
  trait ComponentScopeM[Props, State, Backend] extends ComponentScopeU[Props, State, Backend] with ComponentScope_M

  /** Type of a component's `this` scope as is available to backends. */
  trait ComponentScopeB[Props, State] extends ComponentScope_PSS[Props, State] with ComponentScope_M

  /** Type of `this.refs` */
  trait RefsObject extends js.Object {
    @JSBracketAccess
    def apply[Node <: dom.Element](key: js.String): js.UndefOr[ProxyConstructorM[Node]]
  }

  trait ComponentConstructor[Props] extends js.Object {
    def apply(props: WrapObj[Props], children: js.Any*): ProxyConstructorU = ???
  }

  trait ProxyConstructorU extends js.Object

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
    // Date timeStamp
    @JSName("type") val eventType: String = ???
  }

  /** Allows Scala classes to be used in place of `js.Object`. */
  trait WrapObj[+A] extends js.Object { val v: A }
  def WrapObj[A](v: A) =
    js.Dynamic.literal("v" -> v.asInstanceOf[js.Any]).asInstanceOf[WrapObj[A]]

  // ===================================================================================================================

  case class Ref[T <: dom.Element](name: String) {
    @inline final def apply(scope: ComponentScope_M): js.UndefOr[ProxyConstructorM[T]] = apply(scope.refs)
    @inline final def apply(refs: RefsObject): js.UndefOr[ProxyConstructorM[T]] = refs[T](name)
  }

  //@inline implicit def autoWrapObj[A <: AnyRef](a: A): WrapObj[A] = WrapObj(a) // causes literals -> js.Any
  @inline implicit def autoUnWrapObj[A](a: WrapObj[A]): A = a.v
  implicit final class AnyExtReact[A](val a: A) extends AnyVal {
    @inline def wrap: WrapObj[A] = WrapObj(a)
  }

  implicit final class ComponentScope_PS_Ext[Props, State](val u: ComponentScope_PS[Props, State]) extends AnyVal {
    @inline def props = u._props.v
    @inline def state = u._state.v
  }

  implicit final class ComponentScope_PSS_Ext[Props, State](val u: ComponentScope_PSS[Props, State]) extends AnyVal {
    @inline def setState(s: State): Unit = u._setState(WrapObj(s))
    @inline def modState(f: State => State) = u.setState(f(u.state))
    @inline def setL[V](l: LensFamily[State, State, _, V])(v: V) = modState(l.set(_, v))
  }

  implicit final class ComponentScope_B_Ext[Backend](val u: ComponentScope_B[Backend]) extends AnyVal {
    @inline def backend = u._backend.v
  }

  implicit final class ComponentConstructorExt[P](val u: ComponentConstructor[P]) extends AnyVal {
    @inline def create(props: P, children: js.Any*) = u(WrapObj(props), children: _*)
  }

  implicit final class SyntheticEventExt[N <: dom.Node](val u: SyntheticEvent[N]) extends AnyVal {
    def keyboardEvent = u.nativeEvent.asInstanceOf[dom.KeyboardEvent]
    def messageEvent  = u.nativeEvent.asInstanceOf[dom.MessageEvent]
    def mouseEvent    = u.nativeEvent.asInstanceOf[dom.MouseEvent]
    def mutationEvent = u.nativeEvent.asInstanceOf[dom.MutationEvent]
    def storageEvent  = u.nativeEvent.asInstanceOf[dom.StorageEvent]
    def textEvent     = u.nativeEvent.asInstanceOf[dom.TextEvent]
    def touchEvent    = u.nativeEvent.asInstanceOf[dom.TouchEvent]
  }

  implicit final class URefExt[T <: dom.HTMLElement](val u: js.UndefOr[ProxyConstructorM[T]]) extends AnyVal {
    def tryFocus(): Unit = u.foreach(_.getDOMNode().focus())
  }
}
