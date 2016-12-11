package japgolly.scalajs.react

import scala.scalajs.js
import js.{Dynamic, Object, UndefOr, Any => JAny}
import scala.scalajs.js.annotation.JSImport

@JSImport("react", JSImport.Namespace)
@js.native
object React extends React

@js.native
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

  def createElement(fc: FunctionalComponent[Nothing], props: Object, children: ReactNode*): ReactDOMElement = js.native
  def createElement(fc: FunctionalComponent.WithChildren[Nothing], props: Object, children: ReactNode*): ReactDOMElement = js.native

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

  /** React.Children provides utilities for dealing with the this.props.children opaque data structure. */
  def Children: ReactChildren = js.native
}

/** `React.Children` */
@js.native
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

  /** Return the children opaque data structure as a flat array with keys assigned to each child. Useful if you want to manipulate collections of children in your render methods, especially if you want to reorder or slice this.props.children before passing it down. */
  def toArray(c: PropsChildren): js.Array[ReactNode] = js.native
}
