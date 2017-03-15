package japgolly.scalajs.react.test.raw

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import japgolly.scalajs.react.raw._

/** https://facebook.github.io/react/docs/test-utils.html */
@JSImport("react-addons-test-utils", JSImport.Namespace)
@js.native
object ReactAddonsTestUtils extends ReactAddonsTestUtils

@js.native
trait ReactAddonsTestUtils extends js.Object {

  val Simulate: Simulate = js.native

  /** Render a component into a detached DOM node in the document. This function requires a DOM. */
  def renderIntoDocument(element: ReactElement): ReactComponentUntyped = js.native

  /**
   * Pass a mocked component module to this method to augment it with useful methods that allow it to be used as a dummy
   * React component. Instead of rendering as usual, the component will become a simple &lt;div&gt; (or other tag if
   * mockTagName is provided) containing any provided children.
   */
  def mockComponent[P <: js.Object, S <: js.Object](c: ReactClass[P, S], mockTagName: String = js.native): ReactClass[P, S] = js.native

  type Mounted = ReactComponentUntyped

  /** Returns true if instance is an instance of a React componentClass. */
  def isComponentOfType(instance: ReactElement, c: ReactClassUntyped): Boolean = js.native

  /** Returns true if instance is a DOM component (such as a &lt;div&gt; or &lt;span&gt;). */
  def isDOMComponent(instance: ReactElement): Boolean = js.native

  /** Returns true if instance is a composite component (created with React.createClass()) */
  def isCompositeComponent(instance: ReactElement): Boolean = js.native

  /** The combination of [[isComponentOfType()]] and [[isCompositeComponent()]]. */
  def isCompositeComponentWithType(instance: ReactElement, c: ReactClassUntyped): Boolean = js.native

  /**
   * Traverse all components in tree and accumulate all components where test(component) is true.
   * This is not that useful on its own, but it's used as a primitive for other test utils.
   */
  def findAllInRenderedTree(tree: Mounted, test: js.Function1[Mounted, Boolean]): js.Array[Mounted] = js.native

  /**
   * Finds all instance of components in the rendered tree that are DOM components with the class name
   * matching className.
   */
  def scryRenderedDOMComponentsWithClass(tree: Mounted, className: String): js.Array[Mounted] = js.native

  /**
   * Like [[scryRenderedDOMComponentsWithClass()]] but expects there to be one result, and returns that one result, or
   * throws exception if there is any other number of matches besides one.
   */
  def findRenderedDOMComponentWithClass(tree: Mounted, className: String): Mounted = js.native

  /**
   * Finds all instance of components in the rendered tree that are DOM components with the tag name
   * matching tagName.
   */
  def scryRenderedDOMComponentsWithTag(tree: Mounted, tagName: String): js.Array[Mounted] = js.native

  /**
   * Like [[scryRenderedDOMComponentsWithTag()]] but expects there to be one result, and returns that one result, or
   * throws exception if there is any other number of matches besides one.
   */
  def findRenderedDOMComponentWithTag(tree: Mounted, tagName: String): Mounted = js.native

  /** Finds all instances of components with type equal to componentClass. */
  def scryRenderedComponentsWithType(tree: Mounted, c: ReactClassUntyped): js.Array[Mounted] = js.native

  /**
   * Same as [[scryRenderedComponentsWithType()]] but expects there to be one result and returns that one result, or throws
   * exception if there is any other number of matches besides one.
   */
  def findRenderedComponentWithType(tree: Mounted, c: ReactClassUntyped): Mounted = js.native
}
