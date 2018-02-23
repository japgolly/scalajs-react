package japgolly.scalajs.react

import org.scalajs.dom
import japgolly.scalajs.react.vdom.VdomElement

object ReactDOM {
  def raw = japgolly.scalajs.react.raw.ReactDOM

  def unmountComponentAtNode(container: dom.Node): Boolean =
    raw.unmountComponentAtNode(container)

  @deprecated("Use .renderIntoDOM on unmounted components.", "")
  def render(element  : Nothing,
             container: Nothing,
             callback : Any = null): Null = null

  @deprecated("Use .getDOMNode on mounted components.", "")
  def findDOMNode(comonent: Nothing): Null = null
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

object ReactDOMServer {
  def raw = japgolly.scalajs.react.raw.ReactDOMServer

  /**
    * Render a React.Element to its initial HTML. This should only be used on the server. React will return an HTML
    * string. You can use this method to generate HTML on the server and send the markup down on the initial request for
    * faster page loads and to allow search engines to crawl your pages for SEO purposes.
    *
    * If you call `ReactDOM.render()` on a node that already has this server-rendered markup, React will preserve it and
    * only attach event handlers, allowing you to have a very performant first-load experience.
    */
  def renderToString(e: VdomElement): String =
    raw.renderToString(e.rawElement)

  /**
    * Similar to [[renderToString]], except this doesn't create extra DOM attributes such as `data-react-id`, that React
    * uses internally. This is useful if you want to use React as a simple static page generator, as stripping away the
    * extra attributes can save lots of bytes.
    */
  def renderToStaticMarkup(e: VdomElement): String =
    raw.renderToStaticMarkup(e.rawElement)
}
