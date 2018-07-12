package japgolly.scalajs.react.raw

import scalajs.js
import scalajs.js.|
import scalajs.js.annotation._

@JSImport("react", JSImport.Namespace, "React")
@js.native
object React extends React {

  @js.native
  trait Children extends js.Object {

    final type MapFn[A] = js.Function1[React.Node, A] | js.Function2[React.Node, Int, A]

    /** Invokes a function on every immediate child contained within children with this set to thisArg. If children is a keyed fragment or array it will be traversed: the function will never be passed the container objects. If children is null or undefined, returns null or undefined rather than an array. */
    def map[A](c: js.UndefOr[PropsChildren | Null], fn: MapFn[A]): js.UndefOr[Null | js.Array[A]] = js.native

    /** Like React.Children.map() but does not return an array. */
    def forEach(c: js.UndefOr[PropsChildren | Null], fn: MapFn[_]): Unit = js.native

    /** Verifies that children has only one child (a React element) and returns it. Otherwise this method throws an error.
      *
      * Note: React.Children.only() does not accept the return value of React.Children.map() because it is an array rather than a React element. */
    def only(c: PropsChildren): React.Node = js.native

    /** Returns the total number of components in children, equal to the number of times that a callback passed to map or forEach would be invoked. */
    def count(c: PropsChildren): Int = js.native

    /** Returns the children opaque data structure as a flat array with keys assigned to each child. Useful if you want to manipulate collections of children in your render methods, especially if you want to reorder or slice this.props.children before passing it down.
      *
      * Note: React.Children.toArray() changes keys to preserve the semantics of nested arrays when flattening lists of children. That is, toArray prefixes each key in the returned array so that each element’s key is scoped to the input array containing it.
      */
    def toArray(c: PropsChildren): js.Array[React.Node] = js.native
  }

  //  @JSImport("react", "Component", "React.Component")
  @js.native
  abstract class Component[P <: js.Object, S <: js.Object](ctorProps: P = js.native) extends js.Object {
    final type Props = P with PropsWithChildren
    final type State = S

    final def props: Props = js.native
    var state: State = js.native

    final val constructor: Constructor[P] = js.native

    def componentDidCatch        (error: Error, info: ErrorInfo)     : Unit    = js.native
    def componentDidMount        ()                                  : Unit    = js.native
    def componentDidUpdate       (prevProps: Props, prevState: State): Unit    = js.native
    def componentWillMount       ()                                  : Unit    = js.native
    def componentWillUnmount     ()                                  : Unit    = js.native
    def componentWillReceiveProps(nextProps: Props)                  : Unit    = js.native
    def componentWillUpdate      (nextProps: Props, nextState: State): Unit    = js.native
    def shouldComponentUpdate    (nextProps: Props, nextState: State): Boolean = js.native

    // abstract def render(): React.Element // TODO Fails. Scala.JS bug?
    def render(): React.Element = js.native

    final def forceUpdate(callback: js.Function0[Unit] = js.native): Unit = js.native

    final def setState(partialState: State | Null, callback: js.Function0[Unit] = js.native): Unit = js.native

    @JSName("setState")
    final def modState(updateFn: js.Function2[State, Props, State | Null], callback: js.Function0[Unit] = js.native): Unit = js.native

//    final def setState[SS >: S <: js.Object](partialState: SS, callback: js.Function0[Unit] = js.native): Unit = js.native
//
//    @JSName("setState")
//    final def modState[SS >: S <: js.Object](updateFn: js.Function2[S, P, SS], callback: js.Function0[Unit] = js.native): Unit = js.native
  }

  /** `Class[React.Component[P, S]]` */
  type ComponentClass [P <: js.Object, S <: js.Object] = js.Function1[P, React.Component[P, S]] with HasDisplayName
  type ComponentClassP[P <: js.Object]                 = ComponentClass[P, _ <: js.Object]
  type ComponentClassUntyped                           = ComponentClass[_ <: js.Object, _ <: js.Object]

  /** A `React.Element` that is known to be a component */
  @js.native
  trait ComponentElement[P <: js.Object] extends React.Element {
    def `type`: React.Constructor[P]
    def props: P with PropsWithChildren
  }

  type ComponentUntyped = Component[_ <: js.Object, _ <: js.Object]

  type ComponentType[Props <: js.Object] = ComponentClass[Props, _ <: js.Object] | StatelessFunctionalComponent[Props]

  type Constructor[P <: js.Object] = js.Function1[P, js.Any] with HasDisplayName

  /** A `React.Element` that is known to be DOM */
  @js.native
  trait DomElement extends React.Element {
    def `type`: String
    def props: PropsWithChildren
  }

  /** A React element is the type for the value of a DOM tag, or the return type of React.createElement(). */
  @js.native
  trait Element extends js.Object {
    def key: Key | Null
    def ref: Ref
  }

  /** A React element is the type for the value of a DOM tag, or the return type of React.createElement(). */
  @js.native
  trait ElementRef extends js.Any

  type ElementType = String | ComponentType[_ <: js.Object]

  @js.native
  trait Error extends js.Object {
    val fileName    : String = js.native
    val lineNumber  : Int    = js.native
    val columnNumber: Int    = js.native
    val message     : String = js.native
    val stack       : String = js.native
  }

  @js.native
  trait ErrorInfo extends js.Object {
    val componentStack: String = js.native
  }

  type Key = String | JsNumber

  type Node = ChildrenArray[Empty | String | JsNumber | Element]

  type Ref = RefNonNull | Null

  type RefNonNull = String | RefFn[ElementRef]

  type RefFn[A] = js.Function1[A | Null, Unit]

  type StatelessFunctionalComponent[Props <: js.Object] = js.Function1[Props, Node]

  trait ValueProps[A <: js.Any] extends js.Object {
    val value: A
  }

  @js.native
  trait Context[A <: js.Any] extends js.Object {
    val Provider: ComponentClass[ValueProps[A], Null] = js.native
    val Consumer: ComponentClass[Null, Null]          = js.native
  }
}

@js.native
trait React extends js.Object {
  import React._

  final def createContext[A <: js.Any](defaultValue: A): React.Context[A] = js.native

  /** React.createContext(...).Consumer */
  final def createElement[A <: js.Any](contextConsumer: ComponentClass[Null, Null], props: Null, childrenFn: A => Node): Element = js.native

  final def createElement(`type`: js.Symbol, props: js.Object, children: Node*): Element = js.native

  final def createElement[Props <: js.Object](`type`: String                               ): DomElement = js.native
  final def createElement[Props <: js.Object](`type`: String, props: Props                 ): DomElement = js.native
  final def createElement[Props <: js.Object](`type`: String, props: Props, children: Node*): DomElement = js.native

  final def createElement[Props <: js.Object](`type`: ComponentType[Props]                               ): ComponentElement[Props] = js.native
  final def createElement[Props <: js.Object](`type`: ComponentType[Props], props: Props                 ): ComponentElement[Props] = js.native
  final def createElement[Props <: js.Object](`type`: ComponentType[Props], props: Props, children: Node*): ComponentElement[Props] = js.native

  final val version: String = js.native

  /** React.Children provides utilities for dealing with the this.props.children opaque data structure. */
  final val Children: React.Children = js.native

  final val Fragment: js.Symbol = js.native
}
