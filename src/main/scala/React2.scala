package golly

import org.scalajs.dom
import scala.scalajs.js

package object react2 {

  @inline def React = js.Dynamic.global.React.asInstanceOf[React]

  // ===================================================================================================================

  trait Component {
    type P <: Props
    type S <: State
    type Fn0[+R] = js.ThisFunction0[Scope, R]
    type RenderFn = Fn0[VDom]

    type Scope = react2.ComponentScope[P, S]
    type Constructor = react2.ComponentConstructor[P]
    type Spec = react2.ComponentSpec[P]

    def render: RenderFn
    def getInitialState: js.Function0[S] = null
    def componentDidMount: Fn0[Unit] = null
    def componentWillUnmount: Fn0[Unit] = null

    protected def const0[R](r: R): js.Function0[R] = {
      val sf: js.Function = () => r
      sf.asInstanceOf[js.Function0[R]]
    }
    protected def Fn0[R](f: Scope => R): Fn0[R] = f
//    protected def Render(f: Scope => VDom): RenderFn = f

    def spec =
      js.Dynamic.literal(
        "render" -> render
        , "getInitialState" -> getInitialState
        , "componentDidMount" -> componentDidMount
        , "componentWillUnmount" -> componentWillUnmount
      ).asInstanceOf[Spec]


    def create: Constructor = React.createClass(spec)
  }

  trait ComponentSpec[P <: Props] extends js.Object
  trait ComponentScope[P <: Props, S <: State] extends js.Object {
    def props: P = ???
    def state: S = ???
    def setState(s: S): Unit = ???
  }
  trait ComponentConstructor[P <: Props] extends js.Object {
    def apply(props: P, children: js.Any*): ProxyConstructor = ???
  }

  trait React extends js.Object {

    /**
     * Create a component given a specification. A component implements a render method which returns one single child.
     * That child may have an arbitrarily deep child structure. One thing that makes components different than standard
     * prototypal classes is that you don't need to call new on them. They are convenience wrappers that construct
     * backing instances (via new) for you.
     */
    def createClass[P <: Props](specification: ComponentSpec[P]): ComponentConstructor[P] = ???

    def renderComponent(c: ProxyConstructor, n: dom.Node): js.Dynamic = ???

    val DOM: DOM = ???
  }

  /** Type of HTML rendered in React's virtual DOM. */
  //trait VDom extends js.Object
  type VDom = react.VDom

  /** Type of `this.props` passed to `createClass(_.render)`. */
  type Props = js.Object

  /** Type of `this.state` passed to `createClass(_.render)`. */
  type State = js.Object

  trait ProxyConstructor extends js.Object

  /** Allows Scala classes to be used in place of `js.Object`. */
  trait WrapObj[A] extends js.Object { val v: A }
  def WrapObj[A](v: A) =
    js.Dynamic.literal("v" -> v.asInstanceOf[js.Any]).asInstanceOf[WrapObj[A]]

  trait DOM extends js.Object {
    def div(props: js.Object, children: js.Any*): VDom = ???
  }

  // ===================================================================================================================

  trait UnitObject extends js.Object
  @inline def UnitObject: UnitObject = null
  @inline implicit def autoUnitObject(u: Unit): UnitObject = UnitObject

  //@inline implicit def autoWrapObj[A <: AnyRef](a: A): WrapObj[A] = WrapObj(a) // causes literals -> js.Any
  @inline implicit def autoUnWrapObj[A](a: WrapObj[A]): A = a.v
  implicit class AnyExtReact[A](val a: A) extends AnyVal {
    def wrap: WrapObj[A] = WrapObj(a)
  }
}
