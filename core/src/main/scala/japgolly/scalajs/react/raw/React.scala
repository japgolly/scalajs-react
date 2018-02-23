package japgolly.scalajs.react.raw

import scalajs.js
import scalajs.js.|
import scalajs.js.annotation._

@JSImport("react", JSImport.Namespace, "React")
@js.native
object React extends React {

  @js.native
  trait Children extends js.Object {

    final type MapFn = js.Function1[React.Node, js.Any] | js.Function2[React.Node, Int, js.Any]

    /** Invoke fn on every immediate child contained within children with this set to context. If children is a nested object or array it will be traversed: fn will never be passed the container objects. If children is null or undefined returns null or undefined rather than an empty object. */
    def map(c: PropsChildren, fn: MapFn): js.UndefOr[js.Object] = js.native

    /** Like React.Children.map() but does not return an object. */
    def forEach(c: PropsChildren, fn: MapFn): Unit = js.native

    /** Return the only child in children. Throws otherwise. */
    def only(c: PropsChildren): React.Node = js.native

    /** Return the total number of components in children, equal to the number of times that a callback passed to map or forEach would be invoked. */
    def count(c: PropsChildren): Int = js.native

    /** Return the children opaque data structure as a flat array with keys assigned to each child. Useful if you want to manipulate collections of children in your render methods, especially if you want to reorder or slice this.props.children before passing it down. */
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

    def componentWillMount       ()                                  : Unit    = js.native
    def componentWillUnmount     ()                                  : Unit    = js.native
    def componentDidMount        ()                                  : Unit    = js.native
    def componentWillReceiveProps(nextProps: Props)                  : Unit    = js.native
    def componentWillUpdate      (nextProps: Props, nextState: State): Unit    = js.native
    def componentDidUpdate       (prevProps: Props, prevState: State): Unit    = js.native
    def shouldComponentUpdate    (nextProps: Props, nextState: State): Boolean = js.native

    // abstract def render(): React.Element // TODO Fails. Scala.JS bug?
    def render(): React.Element = js.native

    final def forceUpdate(callback: js.Function0[Unit] = js.native): Unit = js.native

    final def setState(partialState: S, callback: js.Function0[Unit] = js.native): Unit = js.native

    @JSName("setState")
    final def modState(updateFn: js.Function2[S, P, S], callback: js.Function0[Unit] = js.native): Unit = js.native

//    final def setState[SS >: S <: js.Object](partialState: SS, callback: js.Function0[Unit] = js.native): Unit = js.native
//
//    @JSName("setState")
//    final def modState[SS >: S <: js.Object](updateFn: js.Function2[S, P, SS], callback: js.Function0[Unit] = js.native): Unit = js.native
  }

  type ComponentUntyped = Component[_ <: js.Object, _ <: js.Object]

  type Constructor[P <: js.Object] = js.Function1[P, js.Any] with HasDisplayName

  type ComponentType[Props <: js.Object] = ReactClass[Props, _ <: js.Object] | React.StatelessFunctionalComponent[Props]

  type StatelessFunctionalComponent[Props <: js.Object] = js.Function1[Props, React.Element]

  type Node = React.Element | ReactFragment | ReactText

  /** A React element is the type for the value of a DOM tag, or the return type of React.createElement(). */
  @js.native
  trait Element extends js.Object

  type Key = String | Boolean | JsNumber | Null

  // TODO: React.ChildrenArray<T>
  // TODO: React.ElementType
  // TODO: React.Ref           <typeof Component>
  // TODO: React.ElementProps  <typeof Component>
  // TODO: React.ElementConfig <typeof Component>
  // TODO: React.ElementRef    <typeof Component>
}

@js.native
trait React extends js.Object {
  import React._

  def createElement[Props <: js.Object](`type`: String                                        ): ReactDOMElement = js.native
  def createElement[Props <: js.Object](`type`: String, props: Props                          ): ReactDOMElement = js.native
  def createElement[Props <: js.Object](`type`: String, props: Props, children: ReactNodeList*): ReactDOMElement = js.native

  def createElement[Props <: js.Object](`type`: ComponentType[Props]                                        ): ReactComponentElement[Props] = js.native
  def createElement[Props <: js.Object](`type`: ComponentType[Props], props: Props                          ): ReactComponentElement[Props] = js.native
  def createElement[Props <: js.Object](`type`: ComponentType[Props], props: Props, children: ReactNodeList*): ReactComponentElement[Props] = js.native

  /** React.Children provides utilities for dealing with the this.props.children opaque data structure. */
  val Children: React.Children = js.native
}
