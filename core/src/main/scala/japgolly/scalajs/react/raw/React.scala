package japgolly.scalajs.react.raw

import scalajs.js
import scalajs.js.|
import scalajs.js.annotation._

@JSImport("react", JSImport.Namespace, "React")
@js.native
object React extends React {

  @js.native
  trait Children extends js.Object {

    final type MapFn = js.Function1[ReactNode, js.Any] | js.Function2[ReactNode, Int, js.Any]

    /** Invoke fn on every immediate child contained within children with this set to context. If children is a nested object or array it will be traversed: fn will never be passed the container objects. If children is null or undefined returns null or undefined rather than an empty object. */
    def map(c: PropsChildren, fn: MapFn): js.UndefOr[js.Object] = js.native

    /** Like React.Children.map() but does not return an object. */
    def forEach(c: PropsChildren, fn: MapFn): Unit = js.native

    /** Return the only child in children. Throws otherwise. */
    def only(c: PropsChildren): ReactNode = js.native

    /** Return the total number of components in children, equal to the number of times that a callback passed to map or forEach would be invoked. */
    def count(c: PropsChildren): Int = js.native

    /** Return the children opaque data structure as a flat array with keys assigned to each child. Useful if you want to manipulate collections of children in your render methods, especially if you want to reorder or slice this.props.children before passing it down. */
    def toArray(c: PropsChildren): js.Array[ReactNode] = js.native
  }

  //  @JSImport("react", "Component", "React.Component")
  @js.native
  abstract class Component[P <: js.Object, S <: js.Object](ctorProps: P = js.native) extends ReactComponent[P, S] {

    def componentWillMount       ()                                  : Unit    = js.native
    def componentWillUnmount     ()                                  : Unit    = js.native
    def componentDidMount        ()                                  : Unit    = js.native
    def componentWillReceiveProps(nextProps: Props)                  : Unit    = js.native
    def componentWillUpdate      (nextProps: Props, nextState: State): Unit    = js.native
    def componentDidUpdate       (prevProps: Props, prevState: State): Unit    = js.native
    def shouldComponentUpdate    (nextProps: Props, nextState: State): Boolean = js.native

    // These are all defined in the super class:
    //  def props: Props = js.native
    //  var state: State = js.native
    //  def render(): ReactElement
    //  override def forceUpdate(callback: js.Function0[Unit] = js.native): Unit = js.native
    //  override def replaceState(newState: State, callback: js.Function0[Unit] = js.native): Unit = js.native
    //  override def setState(partialState: js.Object, callback: js.Function0[Unit] = js.native): Unit = js.native
    //  @JSName("setState") override def modState(fn: js.Function1[State, js.Object], callback: js.Function0[Unit] = js.native): Unit = js.native
  }
}

@js.native
trait React extends js.Object {

  def createClass[P <: js.Object, S <: js.Object](spec: ReactComponentSpec[P, S]): ReactClass[P, S] = js.native

//  def createFactory[P,S,B,N <: TopNode](t: ReactClass[P,S,B,N]): ReactComponentCU[P,S,B,N] = js.native
//  def createFactory[P <: js.Any, S <: js.Any, N <: TopNode](t: JsComponentType[P, S, N]): JsComponentC[P, S, N] = js.native

  def createElement(`type`: String                                        ): ReactDOMElement = js.native
  def createElement(`type`: String, props: Props                          ): ReactDOMElement = js.native
  def createElement(`type`: String, props: Props, children: ReactNodeList*): ReactDOMElement = js.native

  def createElement(`type`: ReactCtor                                        ): ReactComponentElement = js.native
  def createElement(`type`: ReactCtor, props: Props                          ): ReactComponentElement = js.native
  def createElement(`type`: ReactCtor, props: Props, children: ReactNodeList*): ReactComponentElement = js.native

  //  /** Verifies the object is a ReactElement. */
//  def isValidElement(o: JAny): Boolean = js.native

  /** React.Children provides utilities for dealing with the this.props.children opaque data structure. */
  val Children: React.Children = js.native
}
