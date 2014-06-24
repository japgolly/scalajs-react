package golly

import org.scalajs.dom
import org.scalajs.dom.console
import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

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

    def renderComponent(c: ProxyConstructor, n: dom.Node): js.Dynamic = ???

//    val DOM: DOM = ???
  }

  /** Type of HTML rendered in React's virtual DOM. */
  trait VDom extends js.Object

  trait ComponentSpec[Props] extends js.Object

  trait ComponentScope[Props, State, Backend] extends js.Object {
    @JSName("props") def _props: WrapObj[Props] = ???
    @JSName("state") def _state: WrapObj[State] = ???
    @JSName("setState") def _setState(s: WrapObj[State]): Unit = ???
    def _backend: WrapObj[Backend] = ???
  }

  trait ComponentConstructor[Props] extends js.Object {
    def apply(props: WrapObj[Props], children: js.Any*): ProxyConstructor = ???
  }

  trait ProxyConstructor extends js.Object

  /** http://facebook.github.io/react/docs/events.html */
  trait SyntheticEvent extends js.Object {
    val bubbles: Boolean = ???
    val cancelable: Boolean = ???
    val currentTarget: DOMEventTarget = ???
    def defaultPrevented: Boolean = ???
    val eventPhase: Number = ???
    val isTrusted: Boolean = ???
//      DOMEvent nativeEvent
    def preventDefault(): Unit = ???
    def stopPropagation(): Unit = ???
    val target: DOMEventTarget = ???
//      Date timeStamp
    @JSName("type") val eventType: String = ???
  }

  trait DOMEventTarget extends dom.Node {
    val value: String = ???
  }

  /** Allows Scala classes to be used in place of `js.Object`. */
  trait WrapObj[+A] extends js.Object { val v: A }
  def WrapObj[A](v: A) =
    js.Dynamic.literal("v" -> v.asInstanceOf[js.Any]).asInstanceOf[WrapObj[A]]

//  trait DOM extends js.Object {
//    def div(props: js.Object, children: js.Any*): VDom = ???
//  }

  // ===================================================================================================================

  trait UnitObject extends js.Object
  @inline def UnitObject: UnitObject = null
  @inline implicit def autoUnitObject(u: Unit): UnitObject = UnitObject

  //@inline implicit def autoWrapObj[A <: AnyRef](a: A): WrapObj[A] = WrapObj(a) // causes literals -> js.Any
  @inline implicit def autoUnWrapObj[A](a: WrapObj[A]): A = a.v
  implicit class AnyExtReact[A](val a: A) extends AnyVal {
    @inline def wrap: WrapObj[A] = WrapObj(a)
  }

  implicit class ComponentScopeExt[Props, State, Backend](val u: ComponentScope[Props, State, Backend]) extends AnyVal {
    @inline def props = u._props.v
    @inline def state = u._state.v
    @inline def setState(s: State): Unit = u._setState(WrapObj(s))
    @inline def modState(f: State => State) = u.setState(f(u.state))
    @inline def backend = u._backend.v
//    @inline def curryB[R](f: Backend => ComponentScope[Props, State, Backend] => R): R = f(u.backend)(u)
    @inline def backendFn(f: Backend => js.Function): js.Function = f(u.backend)
  }

  implicit class ComponentConstructorExt[P](val u: ComponentConstructor[P]) {
    @inline def create(props: P, children: js.Any*) = u(WrapObj(props), children: _*)
  }

}
