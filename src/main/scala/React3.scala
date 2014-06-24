package golly

import org.scalajs.dom
import scala.scalajs.js

package object react3 {

  @inline def React = js.Dynamic.global.React.asInstanceOf[React]

  // ===================================================================================================================

  trait Component {
    self =>

    type Self <: Component {type Self = self.Self}
    type P <: Props
    type S <: State

    final type Scope = react3.ComponentScope[Self]
    final type Spec = react3.ComponentSpec[Self]
    final type Constructor = react3.ComponentConstructor[Self]

    def spec: Spec
    protected final def specBuilder = ComponentSpecBuilder.apply[Self]

    def create: Constructor = React.createClass(spec)
  }

  final case class ComponentSpecBuilder[C <: Component](
     render: C#Scope => VDom
     , getInitialState: () => C#S
     , componentDidMount: C#Scope => Unit
     , componentWillUnmount: C#Scope => Unit
     ) {
    
    def getInitialState(f: () => C#S): ComponentSpecBuilder[C] =
      copy(getInitialState = f)
    
    def initialState(s: C#S): ComponentSpecBuilder[C] =
      getInitialState(() => s)

    def componentDidMount(f: C#Scope => Unit): ComponentSpecBuilder[C] =
      copy(componentDidMount = f)
    
    def componentWillUnmount(f: C#Scope => Unit): ComponentSpecBuilder[C] =
      copy(componentWillUnmount = f)
    
    def build =
      js.Dynamic.literal(
        "render" -> (render: js.ThisFunction)
        , "getInitialState" -> (getInitialState: js.Function)
        , "componentDidMount" -> (componentDidMount: js.ThisFunction)
        , "componentWillUnmount" -> (componentWillUnmount: js.ThisFunction)
      ).asInstanceOf[C#Spec]
  }
  object ComponentSpecBuilder {
    def apply[C <: Component] = ComponentSpecBuilderRI.subst[C]
  }
  sealed trait ComponentSpecBuilderR[C <: Component] {
    def render(render: C#Scope => VDom) = new ComponentSpecBuilder[C](render, null, null, null)
  }
  private object ComponentSpecBuilderRI extends ComponentSpecBuilderR[Nothing] {
    @inline def subst[C <: Component] = this.asInstanceOf[ComponentSpecBuilderR[C]]
  }

  trait ComponentSpec[C <: Component] extends js.Object

  trait ComponentScope[C <: Component] extends js.Object {
    def props: C#P = ???
    def state: C#S = ???
    def setState(s: C#S): Unit = ???
  }

  trait ComponentConstructor[C <: Component] extends js.Object {
    def apply(props: C#P, children: js.Any*): ProxyConstructor[C] = ???
  }

  trait React extends js.Object {

    /**
     * Create a component given a specification. A component implements a render method which returns one single child.
     * That child may have an arbitrarily deep child structure. One thing that makes components different than standard
     * prototypal classes is that you don't need to call new on them. They are convenience wrappers that construct
     * backing instances (via new) for you.
     */
    def createClass[C <: Component](specification: ComponentSpec[C]): ComponentConstructor[C] = ???

    def renderComponent(c: ProxyConstructor[_], n: dom.Node): js.Dynamic = ???

    val DOM: DOM = ???
  }

  /** Type of HTML rendered in React's virtual DOM. */
  //trait VDom extends js.Object
  type VDom = react.VDom

  /** Type of `this.props` passed to `createClass(_.render)`. */
  type Props = js.Object

  /** Type of `this.state` passed to `createClass(_.render)`. */
  type State = js.Object

  trait ProxyConstructor[C <: Component] extends js.Object

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
