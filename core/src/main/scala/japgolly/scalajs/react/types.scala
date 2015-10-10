package japgolly.scalajs.react

import scala.scalajs.js
import js.{UndefOr, Object, Function => JFn}
import js.annotation.{JSBracketAccess, JSName}

/**
 * Here we break React's `this` into tiny little bits, then stitch them together to use type-safety to enforce the
 * rules documented in the React API.
 */
object CompScope {

  /** Methods always available. */
  trait AlwaysAvailable extends Object {
    def isMounted(): Boolean = js.native
  }

  trait HasProps[+Props] extends Object {
    @JSName("props") private[react] def _props: WrapObj[Props] with PropsMixedIn = js.native
  }

  trait HasState[+State] extends Object {
    @JSName("state") private[react] def _state: WrapObj[State] = js.native
  }

  trait CanSetState[State] extends HasState[State] {
    @JSName("setState") private[react] def _setState(s: WrapObj[State]): Unit = js.native
    @JSName("setState") private[react] def _setState(s: WrapObj[State], callback: UndefOr[JFn]): Unit = js.native
    @JSName("setState") private[react] def _modState(s: js.Function1[WrapObj[State], WrapObj[State]], callback: UndefOr[JFn]): Unit = js.native
  }

  trait HasBackend[+Backend] extends Object {
    def backend: Backend = js.native
  }

  trait CanGetInitialState[-Props, +State] extends Object {
    @JSName("getInitialState") private[react] def _getInitialState(s: WrapObj[Props]): WrapObj[State] = js.native
  }

  /** Functions available to components when they're mounted. */
  trait Mounted[+Node <: TopNode] extends Object {
    def refs: RefsObject = js.native

    /** Can be invoked on any mounted component in order to obtain a reference to its rendered DOM node. */
    def getDOMNode(): Node = js.native

    /**
     * Can be invoked on any mounted component when you know that some deeper aspect of the component's state has
     * changed without using this.setState().
     */
    @JSName("forceUpdate") private[react] def _forceUpdate(): Unit = js.native
  }

  trait ReadDirect    extends Object
  trait ReadCallback  extends Object
  trait WriteDirect   extends Object
  trait WriteCallback extends Object

  trait AnyUnmounted[Props, State, +Backend]
    extends AlwaysAvailable
       with HasProps[Props]
       with CanSetState[State]
       with CanGetInitialState[Props, State]
       with HasBackend[Backend]
       // prohibits: IsMounted

  trait AnyMounted[Props, State, +Backend, +Node <: TopNode]
    extends AnyUnmounted[Props, State, Backend]
       with Mounted[Node]
       with ReactComponentTypeAuxJ[Props, State, Backend, Node]

  trait AnyDuringCallback
    extends ReadDirect
       with WriteCallback

  /** Type of an unmounted component's `this` scope, as available within lifecycle methods. */
  trait DuringCallbackU[Props, State, +Backend]
    extends AnyUnmounted[Props, State, Backend]
       with AnyDuringCallback

  /** Type of a mounted component's `this` scope, as available within lifecycle methods. */
  trait DuringCallbackM[Props, State, +Backend, +Node <: TopNode]
    extends AnyMounted[Props, State, Backend, Node]
       with AnyDuringCallback

  /** Type of a component's `this` scope during componentWillUpdate. */
  trait WillUpdate[Props, +State, +Backend, +Node <: TopNode]
    extends AlwaysAvailable
       with HasProps[Props]
       with HasState[State]
       with HasBackend[Backend]
       with CanGetInitialState[Props, State]
       with Mounted[Node]
       with AnyDuringCallback
       // prohibits: .setState
}

import CompScope._

/** Type of a component's `this` scope as is available to backends. */
trait BackendScope[Props, State]
  extends AlwaysAvailable
     with HasProps[Props]
     with CanSetState[State]
     with CanGetInitialState[Props, State]
     with Mounted[TopNode]
     with ReadCallback // ReadDirect BackendScope causes subtle and very annoying bugs #169
     with WriteCallback
     // prohibits: .backend

// =====================================================================================================================

/** Type of `this.refs` */
trait RefsObject extends Object {
  @JSBracketAccess
  def apply[Node <: TopNode](key: String): UndefOr[ReactComponentM_[Node]] = js.native
}

/** Additional methods that React mixes into `this.props` */
trait PropsMixedIn extends Object {
  def children: PropsChildren = js.native
}

/** Type of `this.props.children` */
trait PropsChildren extends Object

/**
 * https://facebook.github.io/react/docs/glossary.html indicates children can be a super type of ReactElement.
 * Array and null are acceptable, thus this can be 0-n elements.
 */
trait ReactNode extends Object

/** ReactElement = ReactComponentElement | ReactDOMElement  */
trait ReactElement extends Object with ReactNode {
  def key: UndefOr[String] = js.native
  def ref: UndefOr[String] = js.native
}

/** A React virtual DOM element, such as 'div', 'table', etc. */
trait ReactDOMElement extends ReactElement {
  def `type`: String = js.native
  def props : Object = js.native
}

/** An instance of a React component. Prefer using the subtype ReactComponentU instead. */
trait ReactComponentElement[Props]
  extends ReactElement
     with HasProps[Props]

/** A JS function that creates a React component instance. */
trait ReactComponentC_ extends JFn

/** An unmounted component. Not guaranteed to have been created by Scala, could be a React addon. */
trait ReactComponentU_ extends ReactElement

/** A mounted component. Not guaranteed to have been created by Scala, could be a React addon. */
trait ReactComponentM_[+Node <: TopNode]
  extends ReactComponentU_
     with Mounted[Node]

/** The underlying function that creates a Scala-based React component instance. */
trait ReactComponentCU[Props, State, +Backend, +Node <: TopNode]
  extends ReactComponentC_
     with ReactComponentTypeAuxJ[Props, State, Backend, Node] {
  def apply(props: WrapObj[Props], children: ReactNode*): ReactComponentU[Props, State, Backend, Node] = js.native
}

/** An unmounted Scala component. */
trait ReactComponentU[Props, State, +Backend, +Node <: TopNode]
  extends ReactComponentU_
     with AnyUnmounted[Props, State, Backend]
     with ReactComponentTypeAuxJ[Props, State, Backend, Node]
     with ReadDirect
     with WriteDirect

/** A mounted Scala component. */
trait ReactComponentM[Props, State, +Backend, +Node <: TopNode]
  extends ReactComponentU[Props, State, Backend, Node]
     with ReactComponentM_[Node]
     with AnyMounted[Props, State, Backend, Node]

trait ReactComponentSpec[Props, State, +Backend, +Node <: TopNode] extends Object with ReactComponentTypeAuxJ[Props, State, Backend, Node]

/**
 * A component created via [[React.createClass]].
 */
trait ReactClass[Props, State, +Backend, +Node <: TopNode] extends Object with ReactComponentTypeAuxJ[Props, State, Backend, Node]

// =====================================================================================================================

trait JsComponentType[Props <: js.Any, State <: js.Any, +Node <: TopNode] extends Object

trait JsComponentC[Props <: js.Any, State <: js.Any, +Node <: TopNode] extends ReactComponentC_ with JsComponentType[Props, State, Node] {
  def apply(props: Props, children: ReactNode*): JsComponentU[Props, State, Node] = js.native
}

trait JsComponentU[Props <: js.Any, State <: js.Any, +Node <: TopNode]
  extends ReactComponentU_
  with JsComponentType[Props, State, Node]

trait JsComponentM[Props <: js.Any, State <: js.Any, +Node <: TopNode]
  extends JsComponentU[Props, State, Node]
  with Mounted[Node] with ReactComponentM_[Node] {
  def props: Props = js.native
  def state: State = js.native
  def setState(state: State): Unit = js.native
}
