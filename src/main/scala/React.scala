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
    def createClass[P <: Props](specification: ComponentSpec[P]): AbstractComponent[P] = ???

    def renderComponent(c: ProxyConstructor, n: dom.Node): js.Dynamic = ???

    val DOM: DOM = ???
  }

  /** Type of HTML rendered in React's virtual DOM. */
  trait VDom extends js.Object

  /** Type of `this` passed to `createClass(_.render)`. */
  trait ComponentScope[P <: Props] extends js.Object {
    def props: P = ???
  }

  /** Type of `this.props` passed to `createClass(_.render)`. */
  type Props = js.Object

  /** Type of `createClass(_.render)`. */
  type RenderFn[P <: Props] = js.ThisFunction0[ComponentScope[P], VDom]
  object RenderFn {
    def apply[P <: Props](f: ComponentScope[P] => VDom): RenderFn[P]              = f
    def p    [P <: Props](f: P => VDom)                : RenderFn[P]              = apply(f.compose(_.props))
    def wrapped[P]       (f: P => VDom)                : RenderFn[PropWrapper[P]] = apply(f.compose(_.props.v))
  }

  /** Type of arg passed to `createClass`. */
  trait ComponentSpec[P <: Props] extends js.Object {
    var render: RenderFn[P] = ???
  }
  def ComponentSpec[P <: Props](render: RenderFn[P]) =
    js.Dynamic.literal("render" -> render).asInstanceOf[ComponentSpec[P]]

  /** Return type of `createClass`. */
  trait AbstractComponent[P <: Props] extends js.Object {
    def apply(props: P, children: js.Any*): ProxyConstructor = ???
  }
  trait ProxyConstructor extends js.Object

  /** Allows Scala classes to be used as Props. */
  trait PropWrapper[P] extends js.Object { val v: P }
  def PropWrapper[P](v: P) =
    js.Dynamic.literal("v" -> v.asInstanceOf[js.Any]).asInstanceOf[PropWrapper[P]]

  trait DOM extends js.Object {
    def div(props: js.Object, children: js.Any*): VDom = ???
  }

  // ===================================================================================================================

  @inline implicit def autoWrapProps[P](p: P): PropWrapper[P] = PropWrapper(p)
}
