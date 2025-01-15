package japgolly.scalajs.react.facade

import scala.annotation.nowarn
import scala.annotation.unchecked.uncheckedVariance
import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.|

@JSImport("react", JSImport.Namespace, "React")
@js.native
@nowarn("cat=unused")
object React extends React {

  @js.native
  trait Children extends js.Object {

    // @uncheckedVariance is used here because Scala 2.13.6 doesn't recognise | as covariant
    final type MapFn[+A] = js.Function1[React.Node, A @uncheckedVariance] | js.Function2[React.Node, Int, A @uncheckedVariance]

    /** Invokes a function on every immediate child contained within children with this set to thisArg. If children is a keyed fragment or array it will be traversed: the function will never be passed the container objects. If children is null or undefined, returns null or undefined rather than an array. */
    def map[A](c: js.UndefOr[PropsChildren | Null], fn: MapFn[A]): js.UndefOr[Null | js.Array[A]] = js.native

    /** Like React.Children.map() but does not return an array. */
    def forEach(c: js.UndefOr[PropsChildren | Null], fn: MapFn[Any]): Unit = js.native

    /** Verifies that children has only one child (a React element) and returns it. Otherwise this method throws an error.
      *
      * Note: React.Children.only() does not accept the return value of React.Children.map() because it is an array rather than a React element. */
    def only(c: PropsChildren): React.Node = js.native

    /** Returns the total number of components in children, equal to the number of times that a callback passed to map or forEach would be invoked. */
    def count(c: PropsChildren): Int = js.native

    /** Returns the children opaque data structure as a flat array with keys assigned to each child. Useful if you want to manipulate collections of children in your render methods, especially if you want to reorder or slice this.props.children before passing it down.
      *
      * Note: React.Children.toArray() changes keys to preserve the semantics of nested arrays when flattening lists of children. That is, toArray prefixes each key in the returned array so that each element's key is scoped to the input array containing it.
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

    def render(): React.Node = js.native

    final def forceUpdate(callback: js.Function0[Any] = js.native): Unit = js.native

    final def setState(partialState: State | Null, callback: js.Function0[Any] = js.native): Unit = js.native

    @JSName("setState")
    final def modState(updateFn: js.Function2[State, Props, State | Null], callback: js.Function0[Any] = js.native): Unit = js.native

//    final def setState[SS >: S <: js.Object](partialState: SS, callback: js.Function0[Any] = js.native): Unit = js.native
//
//    @JSName("setState")
//    final def modState[SS >: S <: js.Object](updateFn: js.Function2[S, P, SS], callback: js.Function0[Any] = js.native): Unit = js.native
  }

  /** `Class[React.Component[P, S]]` */
  // TODO: https://github.com/lampepfl/dotty/issues/12115
  // type ComponentClass [P <: js.Object, S <: js.Object] = js.Function1[P, React.Component[P, S]] with HasDisplayName
  // type ComponentClassP[P <: js.Object]                 = ComponentClass[P, _ <: js.Object]
  // type ComponentClassUntyped                           = ComponentClass[_ <: js.Object, _ <: js.Object]
  type ComponentClass [P <: js.Object, S <: js.Object] = js.Function1[P, React.Component[P, S]] with HasDisplayName
  type ComponentClassP[P <: js.Object]                 = js.Function1[P, React.Component[P, _ <: js.Object]] with HasDisplayName
  type ComponentClassUntyped                           = js.Function1[_ <: js.Object, React.Component[_ <: js.Object, _ <: js.Object]] with HasDisplayName

  /** A `React.Element` that is known to be a component */
  @js.native
  trait ComponentElement[P <: js.Object] extends React.Element {
    def `type`: React.Constructor[P]
    def props: P with PropsWithChildren
  }

  type ComponentUntyped = Component[_ <: js.Object, _ <: js.Object]

  // TODO https://github.com/lampepfl/dotty/issues/12115
  type ComponentType[Props <: js.Object] =
    (js.Function1[Props, React.Component[Props, _ <: js.Object]] with HasDisplayName) | // TODO: ComponentClass[Props, _ <: js.Object] |
    ForwardRefComponent[Props, _] |
    StatelessFunctionalComponent[Props]

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
    // def `type`: ElementType
    // def props: React$ElementProps<ElementType>,
    def key: Key | Null
    def ref: Ref | Null
  }

  /** A React element is the type for the value of a DOM tag, or the return type of React.createElement(). */
  @js.native
  trait ElementRef extends js.Any

  // https://github.com/lampepfl/dotty/issues/12115
  // TODO: type ElementType = String | ComponentType[_ <: js.Object]
  type ElementType =
    String |
    (js.Function1[_ <: js.Object, React.Component[_ <: js.Object, _ <: js.Object]] with HasDisplayName) |
    ForwardRefComponent[_ <: js.Object, _] |
    StatelessFunctionalComponent[_ <: js.Object]

  @js.native
  trait ErrorInfo extends js.Object {
    val componentStack: String = js.native
  }

  type Key = String | JsNumber

  type Node = ChildrenArray[Empty | String | JsNumber | Element]

  type Ref = RefFn[ElementRef] | RefHandle[Any]

  type RefFn[A] = js.Function1[A | Null, Unit]

  @js.native
  trait RefHandle[A] extends js.Object {
    var current: A
  }

  type StatelessFunctionalComponent[Props <: js.Object] = js.Function1[Props, Node] with HasMutableDisplayName

  trait ValueProps[A] extends js.Object {
    val value: A
  }

  @js.native
  trait Context[A] extends js.Object {
    var displayName: js.UndefOr[String]                  = js.native
    val Provider   : ComponentClass[ValueProps[A], Null] = js.native
    val Consumer   : ComponentClass[Null, Null]          = js.native
  }

  type ForwardedRef[A] = RefHandle[A | Null] | Null

  @js.native
  trait ForwardRefComponent[P <: js.Object, R] extends js.Object {
    var displayName: js.UndefOr[String] = js.native
    val `$$typeof`: js.Symbol = js.native
    val render: js.Function2[P, ForwardedRef[R], Node] = js.native
    def props: P with PropsWithChildren
  }

  @js.native
  trait Lazy[P <: js.Object] extends js.Object

  @js.native
  trait LazyResult[P <: js.Object] extends js.Object {
    val default: LazyResultValue[P] = js.native
  }

  type LazyResultValue[P <: js.Object] = ComponentType[P]
}

@js.native
trait React extends Hooks with Testing {
  import React._

  final def createContext[A](defaultValue: A): React.Context[A] = js.native

  /** React.createContext(...).Consumer */
  final def createElement[A <: js.Any](contextConsumer: ComponentClass[Null, Null], props: Null, childrenFn: js.Function1[A, Node]): Element = js.native

  final def createElement(s: Suspense.type, props: SuspenseProps, children: Node*): Element = js.native
  final def createElement[P <: js.Object](l: Lazy[P], props: P, children: Node*): Element = js.native

  final def createElement(`type`: js.Symbol, props: js.Object, children: Node*): Element = js.native

  final def createElement(`type`: String                                   ): DomElement = js.native
  final def createElement(`type`: String, props: js.Object                 ): DomElement = js.native
  final def createElement(`type`: String, props: js.Object, children: Node*): DomElement = js.native

  final def createElement[P <: js.Object](`type`: ComponentType[P]                           ): ComponentElement[P] = js.native
  final def createElement[P <: js.Object](`type`: ComponentType[P], props: P                 ): ComponentElement[P] = js.native
  final def createElement[P <: js.Object](`type`: ComponentType[P], props: P, children: Node*): ComponentElement[P] = js.native

  final def cloneElement(element: DomElement                                   ): DomElement = js.native
  final def cloneElement(element: DomElement, props: js.Object                 ): DomElement = js.native
  final def cloneElement(element: DomElement, props: js.Object, children: Node*): DomElement = js.native

  final def cloneElement[P <: js.Object](element: ComponentElement[P]                           ): ComponentElement[P] = js.native
  final def cloneElement[P <: js.Object](element: ComponentElement[P], props: P                 ): ComponentElement[P] = js.native
  final def cloneElement[P <: js.Object](element: ComponentElement[P], props: P, children: Node*): ComponentElement[P] = js.native

  final def createRef[A](): RefHandle[A] = js.native

  final def forwardRef[P <: js.Object, R](f: js.Function2[P with PropsWithChildren, ForwardedRef[R], Node]): ForwardRefComponent[P, R] = js.native

  /** @since 16.6.0 */
  final def `lazy`[P <: js.Object](f: js.Function0[js.Promise[LazyResult[P]]]): Lazy[P] = js.native

  /** @since 16.6.0 */
  final def memo[P <: js.Object, A](f: js.Function1[P, A], areEqual: js.Function2[P, P, Boolean] = js.native): js.Object = js.native

  final def startTransition(callback: js.Function0[Unit]): Unit = js.native

  final val version: String = js.native

  /** React.Children provides utilities for dealing with the this.props.children opaque data structure. */
  final val Children: React.Children = js.native

  final val Fragment: js.Symbol = js.native

  final val Profiler: js.Symbol = js.native

  final val StrictMode: js.Symbol = js.native

  @JSName("__SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED")
  final val SecretInternals: SecretInternals = js.native
}
