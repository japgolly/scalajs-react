package golly

import org.scalajs.dom
import scala.scalajs.js

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
    def createClass[C <: Component](specification: ComponentSpec[C]): ComponentConstructor[C] = ???

    def renderComponent(c: ProxyConstructor[_], n: dom.Node): js.Dynamic = ???

//    val DOM: DOM = ???
  }

  /** Type of HTML rendered in React's virtual DOM. */
  trait VDom extends js.Object

  /** Type of `this.props` passed to `createClass(_.render)`. */
  type Props = js.Object

  /** Type of `this.state` passed to `createClass(_.render)`. */
  type State = js.Object

  trait ComponentSpec[C <: Component] extends js.Object

  trait ComponentScope[C <: Component] extends js.Object {
    def props: C#P = ???
    def state: C#S = ???
    def setState(s: C#S): Unit = ???
    def _sc_b: WrapObj[C#Backend] = ???
  }

  trait ComponentConstructor[C <: Component] extends js.Object {
    def apply(props: C#P, children: js.Any*): ProxyConstructor[C] = ???
  }

  trait ProxyConstructor[C <: Component] extends js.Object

  /** Allows Scala classes to be used in place of `js.Object`. */
  trait WrapObj[A] extends js.Object { val v: A }
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

  implicit class ComponentScopeExt[C <: Component](val scope: ComponentScope[C]) extends AnyVal {
    @inline def modState(f: C#S => C#S) = scope.setState(f(scope.state))
    @inline def backend = scope._sc_b.v
  }

}
