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
    def createClass[P <: Props, S <: State](specification: ComponentSpec[P, S]): AbstractComponent[P, S] = ???

    def renderComponent(c: ProxyConstructor, n: dom.Node): js.Dynamic = ???

    val DOM: DOM = ???
  }

  /** Type of HTML rendered in React's virtual DOM. */
  trait VDom extends js.Object

  /** Type of `this.props` passed to `createClass(_.render)`. */
  type Props = js.Object

  /** Type of `this.state` passed to `createClass(_.render)`. */
  type State = js.Object

  /** Type of `this` passed to `createClass(_.render)`. */
  trait ComponentScope[P <: Props, S <: State] extends js.Object {
    def props: P = ???
    def state: S = ???
    def setState(s: S): Unit = ???
  }

  /** Type of `createClass(_.render)`. */
  type RenderFn[P <: Props, S <: State] = js.ThisFunction0[ComponentScope[P, S], VDom]
  object RenderFn {
//    def apply[P <: Props](f: ComponentScope[P] => VDom): RenderFn[P]          = f
//    def p    [P <: Props](f: P => VDom)                : RenderFn[P]          = apply(f.compose(_.props))
//    def wrapped[P]       (f: P => VDom)                : RenderFn[WrapObj[P]] = apply(f.compose(_.props.v))

    def apply[P <: Props, S <: State](f: ComponentScope[P, S] => VDom): RenderFn[P, S] = f
//    def p    [P <: Props](f: P => VDom)                : RenderFn[P]          = apply(f.compose(_.props))
//    def wrapped[P]       (f: P => VDom)                : RenderFn[WrapObj[P]] = apply(f.compose(_.props.v))
  }

  /** Type of arg passed to `createClass`. */
  trait ComponentSpec[P <: Props, S <: State] extends js.Object

  case class ComponentSpecBuilder[P <: Props, S <: State](
    render: RenderFn[P, S]
    , getInitialState: js.Function0[S]
    , componentDidMount: js.ThisFunction0[ComponentScope[P, S], Unit]
    , componentWillUnmount: js.ThisFunction0[ComponentScope[P, S], Unit]
  ) {
    def getInitialState(f: js.Function0[S]): ComponentSpecBuilder[P, S] =
      copy(getInitialState = f)
    def initialState(s: State): ComponentSpecBuilder[P, S] = {
      val sf: js.Function = () => s
      getInitialState(sf.asInstanceOf[js.Function0[S]]) // TODO why?
    }
    def componentDidMount(f: ComponentScope[P, S] => Unit): ComponentSpecBuilder[P, S] =
      copy(componentDidMount = f)
    def componentWillUnmount(f: ComponentScope[P, S] => Unit): ComponentSpecBuilder[P, S] =
      copy(componentWillUnmount = f)
    def build =
      js.Dynamic.literal(
        "render" -> render
        , "getInitialState" -> getInitialState
        , "componentDidMount" -> componentDidMount
        , "componentWillUnmount" -> componentWillUnmount
      ).asInstanceOf[ComponentSpec[P, S]]
  }
  object ComponentSpecBuilder {
    def apply[P <: Props, S <: State](render: RenderFn[P, S]) =
      new ComponentSpecBuilder(render, null, null, null)
  }

  /** Return type of `createClass`. */
  trait AbstractComponent[P <: Props, S <: State] extends js.Object {
    def apply(props: P, children: js.Any*): ProxyConstructor = ???
  }
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
