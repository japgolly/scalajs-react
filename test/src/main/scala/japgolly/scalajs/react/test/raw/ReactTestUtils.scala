package japgolly.scalajs.react.test.raw

import scala.scalajs.js
import scala.scalajs.js.annotation._
import japgolly.scalajs.react.raw._

/** https://facebook.github.io/react/docs/test-utils.html */
@JSImport("react-dom/test-utils", JSImport.Namespace, "ReactTestUtils")
@js.native
object ReactTestUtils extends ReactTestUtils

@js.native
trait ReactTestUtils extends js.Object {

  final val Simulate: Simulate = js.native

  /** Render a component into a detached DOM node in the document. This function requires a DOM. */
  final def renderIntoDocument(element: React.Element): React.ComponentUntyped = js.native

  /**
   * Pass a mocked component module to this method to augment it with useful methods that allow it to be used as a dummy
   * React component. Instead of rendering as usual, the component will become a simple &lt;div&gt; (or other tag if
   * mockTagName is provided) containing any provided children.
   */
  final def mockComponent[P <: js.Object, S <: js.Object](c: React.ComponentClass[P, S], mockTagName: String = js.native): React.ComponentClass[P, S] = js.native

  final type Mounted = React.ComponentUntyped

  /** Returns true if instance is an instance of a React componentClass. */
  final def isComponentOfType(instance: React.Element, c: React.ComponentClassUntyped): Boolean = js.native

  /** Returns true if instance is a DOM component (such as a &lt;div&gt; or &lt;span&gt;). */
  final def isDOMComponent(instance: React.Element): Boolean = js.native

  /** Returns true if instance is a composite component (created with React.createClass()) */
  final def isCompositeComponent(instance: React.Element): Boolean = js.native

  /** The combination of [[isComponentOfType()]] and [[isCompositeComponent()]]. */
  final def isCompositeComponentWithType(instance: React.Element, c: React.ComponentClassUntyped): Boolean = js.native

  /**
   * Traverse all components in tree and accumulate all components where test(component) is true.
   * This is not that useful on its own, but it's used as a primitive for other test utils.
   */
  final def findAllInRenderedTree(tree: Mounted, test: js.Function1[Mounted, Boolean]): js.Array[Mounted] = js.native

  /**
   * Finds all instance of components in the rendered tree that are DOM components with the class name
   * matching className.
   */
  final def scryRenderedDOMComponentsWithClass(tree: Mounted, className: String): js.Array[Mounted] = js.native

  /**
   * Like [[scryRenderedDOMComponentsWithClass()]] but expects there to be one result, and returns that one result, or
   * throws exception if there is any other number of matches besides one.
   */
  final def findRenderedDOMComponentWithClass(tree: Mounted, className: String): Mounted = js.native

  /**
   * Finds all instance of components in the rendered tree that are DOM components with the tag name
   * matching tagName.
   */
  final def scryRenderedDOMComponentsWithTag(tree: Mounted, tagName: String): js.Array[Mounted] = js.native

  /**
   * Like [[scryRenderedDOMComponentsWithTag()]] but expects there to be one result, and returns that one result, or
   * throws exception if there is any other number of matches besides one.
   */
  final def findRenderedDOMComponentWithTag(tree: Mounted, tagName: String): Mounted = js.native

  /** Finds all instances of components with type equal to componentClass. */
  final def scryRenderedComponentsWithType(tree: Mounted, c: React.ComponentClassUntyped): js.Array[Mounted] = js.native

  /**
   * Same as [[scryRenderedComponentsWithType()]] but expects there to be one result and returns that one result, or throws
   * exception if there is any other number of matches besides one.
   */
  final def findRenderedComponentWithType(tree: Mounted, c: React.ComponentClassUntyped): Mounted = js.native
}
