package japgolly.scalajs.react

import org.scalajs.dom
import scala.scalajs.js
import js.{Dynamic, UndefOr, ThisFunction, ThisFunction0, Object, Any => JAny}

object React extends React
trait React extends Object {

  /**
   * Create a component given a specification. A component implements a render method which returns one single child.
   * That child may have an arbitrarily deep child structure. One thing that makes components different than standard
   * prototypal classes is that you don't need to call new on them. They are convenience wrappers that construct
   * backing instances (via new) for you.
   */
  def createClass[P,S,B,N <: TopNode](spec: ReactComponentSpec[P,S,B,N]): ReactClass[P,S,B,N] = js.native

  /**
   * Return a function that produces ReactElements of a given type. Like `React.createElement`, the type argument can be
   * either an html tag name string (eg. 'div', 'span', etc), or a [[ReactClass]].
   */
  def createFactory[P,S,B,N <: TopNode](t: ReactClass[P,S,B,N]): ReactComponentCU[P,S,B,N] = js.native
  /**
   * Return a function that produces ReactElements of a given type. Like `React.createElement`, the type argument can be
   * either an html tag name string (eg. 'div', 'span', etc), or a [[ReactClass]].
   */
  def createFactory[P <: js.Any, S <: js.Any, N <: TopNode](t: JsComponentType[P, S, N]): JsComponentC[P, S, N] = js.native

  /**
   * Create and return a new `ReactElement` of the given type. The type argument can be either an html tag name string
   * (eg. 'div', 'span', etc), or a [[ReactClass]] (created via [[React.createClass]]).
   */
  def createElement[P,S,B,N <: TopNode](t: ReactClass[P,S,B,N]): ReactComponentCU[P,S,B,N] = js.native
  /**
   * Create and return a new `ReactElement` of the given type. The type argument can be either an html tag name string
   * (eg. 'div', 'span', etc), or a [[ReactClass]] (created via [[React.createClass]]).
   */
  def createElement(tag: String, props: Object, children: ReactNode*): ReactDOMElement = js.native

  /** Verifies the object is a ReactElement. */
  def isValidElement(o: JAny): Boolean = js.native

  /**
   * Clone and return a new `ReactElement` using `element` as the starting point.
   * The resulting element will have the original element's props with the new props merged in shallowly.
   * New children will replace existing children. Unlike `React.addons.cloneWithProps`, `key` and `ref` from the
   * original element will be preserved. There is no special behavior for merging any props (unlike `cloneWithProps`).
   */
  def cloneElement(element: ReactElement, props: Object, children: ReactNode*): ReactElement = js.native

  /**
   * React.DOM provides convenience wrappers around React.createElement for DOM components. These should only be used
   * when not using JSX. For example, React.DOM.div(null, 'Hello World!')
   */
  def DOM: Dynamic = js.native

  def addons: Dynamic = js.native

  /** React.Children provides utilities for dealing with the this.props.children opaque data structure. */
  def Children: ReactChildren = js.native

  @deprecated("As of React 0.14, you must use ReactDOM.render instead.", "0.10.0")
  def render(element: ReactElement, container: dom.Node): ReactComponentM_[TopNode] = js.native

  @deprecated("As of React 0.14, you must use ReactDOM.render instead.", "0.10.0")
  def render(element: ReactElement, container: dom.Node, callback: ThisFunction): ReactComponentM_[TopNode] = js.native

  @deprecated("As of React 0.14, you must use ReactDOM.render instead.", "0.10.0")
  def render[P,S,B,N <: TopNode](component: ReactComponentU[P,S,B,N], container: dom.Node): ReactComponentM[P,S,B,N] = js.native

  @deprecated("As of React 0.14, you must use ReactDOM.render instead.", "0.10.0")
  def render[P,S,B,N <: TopNode](component: ReactComponentU[P,S,B,N], container: dom.Node, callback: ThisFunction0[ReactComponentM[P,S,B,N], Unit]): ReactComponentM[P,S,B,N] = js.native

  @deprecated("As of React 0.14, you must use ReactDOM.unmountComponentAtNode instead.", "0.10.0")
  def unmountComponentAtNode(container: dom.Node): Boolean = js.native

  @deprecated("As of React 0.14, you must use ReactDOM.findDOMNode instead.", "0.10.0")
  def findDOMNode[N <: TopNode](component: CompScope.Mounted[N]): N = js.native

  @deprecated("As of React 0.14, you must use ReactDOMServer.renderToString instead.", "0.10.0")
  def renderToString(e: ReactElement): String = js.native

  @deprecated("As of React 0.14, you must use ReactDOMServer.renderToStaticMarkup instead.", "0.10.0")
  def renderToStaticMarkup(e: ReactElement): String = js.native
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
