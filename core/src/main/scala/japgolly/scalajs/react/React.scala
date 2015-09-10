package japgolly.scalajs.react

import org.scalajs.dom
import scala.scalajs.js
import js.{Dynamic, UndefOr, ThisFunction, ThisFunction0, Object, Any => JAny, Function => JFn}
import js.annotation.{JSBracketAccess, JSName}

object React extends React
trait React extends Object {

  /**
   * Create a component given a specification. A component implements a render method which returns one single child.
   * That child may have an arbitrarily deep child structure. One thing that makes components different than standard
   * prototypal classes is that you don't need to call new on them. They are convenience wrappers that construct
   * backing instances (via new) for you.
   */
  def createClass[P,S,B,N <: TopNode](spec: ReactComponentSpec[P,S,B,N]): ReactComponentType[P,S,B,N] = js.native

  def createFactory[P,S,B,N <: TopNode](t: ReactComponentType[P,S,B,N]): ReactComponentCU[P,S,B,N] = js.native
  def createFactory[P <: js.Any, S <: js.Any, N <: TopNode](t: JsComponentType[P, S, N]): JsComponentC[P, S, N] = js.native

  def createElement[P,S,B,N <: TopNode](t: ReactComponentType[P,S,B,N]): ReactComponentCU[P,S,B,N] = js.native
  def createElement(tag: String, props: Object, children: ReactNode*): ReactDOMElement = js.native

  /**
   * Render a ReactElement into the DOM in the supplied `container` and return a reference to the component.
   *
   * If the ReactElement was previously rendered into `container`, this will perform an update on it and only mutate the
   * DOM as necessary to reflect the latest React component.
   *
   * If the optional callback is provided, it will be executed after the component is rendered or updated.
   */
  def render(element: ReactElement, container: dom.Node): ReactComponentM_[TopNode] = js.native
  /**
   * Render a ReactElement into the DOM in the supplied `container` and return a reference to the component.
   *
   * If the ReactElement was previously rendered into `container`, this will perform an update on it and only mutate the
   * DOM as necessary to reflect the latest React component.
   *
   * If the optional callback is provided, it will be executed after the component is rendered or updated.
   */
  def render(element: ReactElement, container: dom.Node, callback: ThisFunction): ReactComponentM_[TopNode] = js.native
  /**
   * Render a Scala-based React component into the DOM in the supplied `container` and return a reference to the component.
   *
   * If the ReactElement was previously rendered into `container`, this will perform an update on it and only mutate the
   * DOM as necessary to reflect the latest React component.
   *
   * If the optional callback is provided, it will be executed after the component is rendered or updated.
   */
  def render[P,S,B,N <: TopNode](component: ReactComponentU[P,S,B,N], container: dom.Node): ReactComponentM[P,S,B,N] = js.native
  /**
   * Render a Scala-based React component into the DOM in the supplied `container` and return a reference to the component.
   *
   * If the ReactElement was previously rendered into `container`, this will perform an update on it and only mutate the
   * DOM as necessary to reflect the latest React component.
   *
   * If the optional callback is provided, it will be executed after the component is rendered or updated.
   */
  def render[P,S,B,N <: TopNode](component: ReactComponentU[P,S,B,N], container: dom.Node, callback: ThisFunction0[ReactComponentM[P,S,B,N], Unit]): ReactComponentM[P,S,B,N] = js.native

  /**
   * Remove a mounted React component from the DOM and clean up its event handlers and state. If no component was
   * mounted in the container, calling this function does nothing. Returns true if a component was unmounted and false
   * if there was no component to unmount.
   */
  def unmountComponentAtNode(container: dom.Node): Boolean = js.native

  /**
   * Render a ReactElement to its initial HTML. This should only be used on the server. React will return an HTML
   * string. You can use this method to generate HTML on the server and send the markup down on the initial request for
   * faster page loads and to allow search engines to crawl your pages for SEO purposes.
   *
   * If you call React.render() on a node that already has this server-rendered markup, React will preserve it and only
   * attach event handlers, allowing you to have a very performant first-load experience.
   */
  def renderToString(e: ReactElement): String = js.native

  /**
   * Similar to renderToString, except this doesn't create extra DOM attributes such as data-react-id, that React uses
   * internally. This is useful if you want to use React as a simple static page generator, as stripping away the extra
   * attributes can save lots of bytes.
   */
  def renderToStaticMarkup(e: ReactElement): String = js.native

  /** Verifies the object is a ReactElement. */
  def isValidElement(o: JAny): Boolean = js.native

  /** Configure React's event system to handle touch events on mobile devices. */
  def initializeTouchEvents(shouldUseTouch: Boolean): Unit = js.native

  /**
   * React.DOM provides convenience wrappers around React.createElement for DOM components. These should only be used
   * when not using JSX. For example, React.DOM.div(null, 'Hello World!')
   */
  def DOM: Dynamic = js.native

  def addons: Dynamic = js.native

  /** React.Children provides utilities for dealing with the this.props.children opaque data structure. */
  def Children: ReactChildren = js.native

  @deprecated("React.renderComponent will be deprecated in a future version. Use React.render instead.", "React 0.12.0")
  def renderComponent(c: ReactComponentU_, n: dom.Node): ReactComponentM_[TopNode] = js.native

  @deprecated("React.renderComponent will be deprecated in a future version. Use React.render instead.", "React 0.12.0")
  def renderComponent(c: ReactComponentU_, n: dom.Node, callback: ThisFunction): ReactComponentM_[TopNode] = js.native

  @deprecated("React.renderComponent will be deprecated in a future version. Use React.render instead.", "React 0.12.0")
  def renderComponent[P, S, B, N <: TopNode](c: ReactComponentU[P, S, B, N], n: dom.Node): ReactComponentM[P, S, B, N] = js.native

  @deprecated("React.renderComponent will be deprecated in a future version. Use React.render instead.", "React 0.12.0")
  def renderComponent[P, S, B, N <: TopNode](c: ReactComponentU[P, S, B, N], n: dom.Node, callback: ThisFunction0[ReactComponentM[P, S, B, N], Unit]): ReactComponentM[P, S, B, N] = js.native

  @deprecated("React.renderComponentToString will be deprecated in a future version. Use React.renderToString instead.", "React 0.12.0")
  def renderComponentToString(component: ReactComponentU_): String = js.native

  @deprecated("React.renderComponentToStaticMarkup will be deprecated in a future version. Use React.renderToStaticMarkup instead.", "React 0.12.0")
  def renderComponentToStaticMarkup(component: ReactComponentU_): String = js.native
}

/** `React.Children` */
trait ReactChildren extends Object {
  /** Invoke fn on every immediate child contained within children with this set to context. If children is a nested object or array it will be traversed: fn will never be passed the container objects. If children is null or undefined returns null or undefined rather than an empty object. */
  def map(c: PropsChildren, fn: js.Function1[ReactNode, JAny]): UndefOr[Object] = js.native
  /** Invoke fn on every immediate child contained within children with this set to context. If children is a nested object or array it will be traversed: fn will never be passed the container objects. If children is null or undefined returns null or undefined rather than an empty object. */
  def map(c: PropsChildren, fn: js.Function2[ReactNode, Int, JAny]): UndefOr[Object] = js.native

  /** Like React.Children.map() but does not return an object. */
  def forEach(c: PropsChildren, fn: js.Function1[ReactNode, JAny]): Unit = js.native
  /** Like React.Children.map() but does not return an object. */
  def forEach(c: PropsChildren, fn: js.Function2[ReactNode, Int, JAny]): Unit = js.native

  /** Return the only child in children. Throws otherwise. */
  def only(c: PropsChildren): ReactNode = js.native

  /** Return the total number of components in children, equal to the number of times that a callback passed to map or forEach would be invoked. */
  def count(c: PropsChildren): Int = js.native
}

// =====================================================================================================================

/**
 * Here we break React's `this` into tiny little bits, then stitch them together to use type-safety to enforce the
 * rules documented in the React API.
 */
object ComponentScope {

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

import ComponentScope._

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

/** The meat in React's createClass-createFactory sandwich. */
trait ReactComponentType[Props, State, +Backend, +Node <: TopNode] extends Object with ReactComponentTypeAuxJ[Props, State, Backend, Node]

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
