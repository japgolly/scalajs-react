package japgolly.scalajs.react

import org.scalajs.dom

import scala.scalajs.js
import js.{Object, ThisFunction, ThisFunction0}
import scala.scalajs.js.annotation.JSImport

@JSImport("react-dom", JSImport.Namespace)
@js.native
object ReactDOM extends ReactDOM

@js.native
trait ReactDOM extends Object {

  /**
   * Render a ReactElement into the DOM in the supplied `container` and return a reference to the component (or returns
   * `null` for stateless components).
   *
   * If the [[ReactElement]] was previously rendered into `container`, this will perform an update on it and only mutate
   * the DOM as necessary to reflect the latest React component.
   *
   * If the optional callback is provided, it will be executed after the component is rendered or updated.
   *
   * Note:
   *
   * `ReactDOM.render()` controls the contents of the container node you pass in. Any existing DOM elements
   * inside are replaced when first called. Later calls use React’s DOM diffing algorithm for efficient
   * updates.
   *
   * `ReactDOM.render()` does not modify the container node (only modifies the children of the container). In
   * the future, it may be possible to insert a component to an existing DOM node without overwriting
   * the existing children.
   */
  def render(element: ReactElement, container: dom.Node): ReactComponentM_[TopNode] = js.native
  /**
   * Render a ReactElement into the DOM in the supplied `container` and return a reference to the component (or returns
   * `null` for stateless components).
   *
   * If the [[ReactElement]] was previously rendered into `container`, this will perform an update on it and only mutate
   * the DOM as necessary to reflect the latest React component.
   *
   * If the optional callback is provided, it will be executed after the component is rendered or updated.
   *
   * Note:
   *
   * `ReactDOM.render()` controls the contents of the container node you pass in. Any existing DOM elements
   * inside are replaced when first called. Later calls use React’s DOM diffing algorithm for efficient
   * updates.
   *
   * `ReactDOM.render()` does not modify the container node (only modifies the children of the container). In
   * the future, it may be possible to insert a component to an existing DOM node without overwriting
   * the existing children.
   */
  def render(element: ReactElement, container: dom.Node, callback: ThisFunction): ReactComponentM_[TopNode] = js.native
  /**
   * Render a ReactElement into the DOM in the supplied `container` and return a reference to the component (or returns
   * `null` for stateless components).
   *
   * If the [[ReactElement]] was previously rendered into `container`, this will perform an update on it and only mutate
   * the DOM as necessary to reflect the latest React component.
   *
   * If the optional callback is provided, it will be executed after the component is rendered or updated.
   *
   * Note:
   *
   * `ReactDOM.render()` controls the contents of the container node you pass in. Any existing DOM elements
   * inside are replaced when first called. Later calls use React’s DOM diffing algorithm for efficient
   * updates.
   *
   * `ReactDOM.render()` does not modify the container node (only modifies the children of the container). In
   * the future, it may be possible to insert a component to an existing DOM node without overwriting
   * the existing children.
   */
  def render[P,S,B,N <: TopNode](component: ReactComponentU[P,S,B,N], container: dom.Node): ReactComponentM[P,S,B,N] = js.native
  /**
   * Render a ReactElement into the DOM in the supplied `container` and return a reference to the component (or returns
   * `null` for stateless components).
   *
   * If the [[ReactElement]] was previously rendered into `container`, this will perform an update on it and only mutate
   * the DOM as necessary to reflect the latest React component.
   *
   * If the optional callback is provided, it will be executed after the component is rendered or updated.
   *
   * Note:
   *
   * `ReactDOM.render()` controls the contents of the container node you pass in. Any existing DOM elements
   * inside are replaced when first called. Later calls use React’s DOM diffing algorithm for efficient
   * updates.
   *
   * `ReactDOM.render()` does not modify the container node (only modifies the children of the container). In
   * the future, it may be possible to insert a component to an existing DOM node without overwriting
   * the existing children.
   */
  def render[P,S,B,N <: TopNode](component: ReactComponentU[P,S,B,N], container: dom.Node, callback: ThisFunction0[ReactComponentM[P,S,B,N], Unit]): ReactComponentM[P,S,B,N] = js.native

  /**
   * Remove a mounted React component from the DOM and clean up its event handlers and state. If no component was
   * mounted in the container, calling this function does nothing. Returns `true` if a component was unmounted and
   * `false` if there was no component to unmount.
   */
  def unmountComponentAtNode(container: dom.Node): Boolean = js.native

  /**
   * If this component has been mounted into the DOM, this returns the corresponding native browser DOM element. This
   * method is useful for reading values out of the DOM, such as form field values and performing DOM measurements.
   * **In most cases, you can attach a ref to the DOM node and avoid using `findDOMNode` at all.** When `render` returns
   * `null` or `false`, `findDOMNode` returns `null`.
   *
   * Note:
   *
   * `findDOMNode()` is an escape hatch used to access the underlying DOM node. In most cases, use of this escape hatch
   * is discouraged because it pierces the component abstraction.
   *
   * `findDOMNode()` only works on mounted components (that is, components that have been placed in the DOM). If you try
   * to call this on a component that has not been mounted yet (like calling `findDOMNode()` in `render()` on a
   * component that has yet to be created) an exception will be thrown.
   *
   * `findDOMNode()` cannot be used on stateless components.
   */
  def findDOMNode[N <: TopNode](component: CompScope.Mounted[N]): N = js.native
}

@JSImport("react-dom/server", JSImport.Namespace)
@js.native
object ReactDOMServer extends ReactDOMServer

@js.native
trait ReactDOMServer extends Object {

  /**
   * Render a ReactElement to its initial HTML. This should only be used on the server. React will return an HTML
   * string. You can use this method to generate HTML on the server and send the markup down on the initial request for
   * faster page loads and to allow search engines to crawl your pages for SEO purposes.
   *
   * If you call `ReactDOM.render()` on a node that already has this server-rendered markup, React will preserve it and
   * only attach event handlers, allowing you to have a very performant first-load experience.
   */
  def renderToString(e: ReactElement): String = js.native

  /**
   * Similar to [[renderToString]], except this doesn't create extra DOM attributes such as `data-react-id`, that React
   * uses internally. This is useful if you want to use React as a simple static page generator, as stripping away the
   * extra attributes can save lots of bytes.
   */
  def renderToStaticMarkup(e: ReactElement): String = js.native
}
