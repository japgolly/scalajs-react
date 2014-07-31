package japgolly.scalajs.react.test

import scala.scalajs.js.{Function1 => JFn1, UndefOr, Object, Array, undefined}
import japgolly.scalajs.react._

/** http://facebook.github.io/react/docs/test-utils.html */
trait ReactTestUtils extends Object {

  /** Render a component into a detached DOM node in the document. This function requires a DOM. */
  def renderIntoDocument(c: ReactComponentU_): ComponentM = ???

  /**
   * Pass a mocked component module to this method to augment it with useful methods that allow it to be used as a dummy
   * React component. Instead of rendering as usual, the component will become a simple &lt;div&gt; (or other tag if
   * mockTagName is provided) containing any provided children.
   */
  def mockComponent(c: ComponentClass, tagName: String = ???): Object = ???

  /** Returns true if instance is an instance of a React componentClass. */
  def isComponentOfType(instance: ReactComponentU_, c: ComponentClass): Boolean = ???

  /** Returns true if instance is a DOM component (such as a &lt;div&gt; or &lt;span&gt;). */
  def isDOMComponent(instance: ReactComponentU_): Boolean = ???

  /** Returns true if instance is a composite component (created with React.createClass()) */
  def isCompositeComponent(instance: ReactComponentU_): Boolean = ???

  /** The combination of isComponentOfType() and isCompositeComponent(). */
  def isCompositeComponentWithType(instance: ReactComponentU_, c: ComponentClass): Boolean = ???

  /** Returns true if instance is a plain text component. */
  def isTextComponent(instance: ReactComponentU_): Boolean = ???

  /**
   * Traverse all components in tree and accumulate all components where test(component) is true.
   * This is not that useful on its own, but it's used as a primitive for other test utils.
   */
  def findAllInRenderedTree(tree: ComponentM, test: JFn1[ComponentM, Boolean]): Array[ComponentM] = ???

  /**
   * Finds all instance of components in the rendered tree that are DOM components with the class name
   * matching className.
   */
  def scryRenderedDOMComponentsWithClass(tree: ComponentM, className: String): Array[ComponentM] = ???

  /**
   * Like scryRenderedDOMComponentsWithClass() but expects there to be one result, and returns that one result, or
   * throws exception if there is any other number of matches besides one.
   */
  def findRenderedDOMComponentWithClass(tree: ComponentM, className: String): ComponentM = ???

  /**
   * Finds all instance of components in the rendered tree that are DOM components with the tag name
   * matching tagName.
   */
  def scryRenderedDOMComponentsWithTag(tree: ComponentM, tagName: String): Array[ComponentM] = ???

  /**
   * Like scryRenderedDOMComponentsWithTag() but expects there to be one result, and returns that one result, or
   * throws exception if there is any other number of matches besides one.
   */
  def findRenderedDOMComponentWithTag(tree: ComponentM, tagName: String): ComponentM = ???

  /** Finds all instances of components with type equal to componentClass. */
  def scryRenderedComponentsWithType(tree: ComponentM, c: ComponentClass): Array[ComponentM] = ???

  /**
   * Same as scryRenderedComponentsWithType() but expects there to be one result and returns that one result, or throws
   * exception if there is any other number of matches besides one.
   */
  def findRenderedComponentWithType(tree: ComponentM, c: ComponentClass): ComponentM = ???

  // TODO Add Simulate
}