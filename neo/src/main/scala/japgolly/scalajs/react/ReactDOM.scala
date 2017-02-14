package japgolly.scalajs.react

import org.scalajs.dom
import vdom.ReactElement

object ReactDOM {

  def unmountComponentAtNode(container: dom.Node): Boolean =
    raw.ReactDOM.unmountComponentAtNode(container)

  @deprecated("Use .renderIntoDOM on unmounted components.", "")
  def render(x: Nothing): Null = null

  @deprecated("Use .getDOMNode on mounted components.", "")
  def findDOMNode(x: Nothing): Null = null
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

object ReactDOMServer {

  /**
    * Render a ReactElement to its initial HTML. This should only be used on the server. React will return an HTML
    * string. You can use this method to generate HTML on the server and send the markup down on the initial request for
    * faster page loads and to allow search engines to crawl your pages for SEO purposes.
    *
    * If you call `ReactDOM.render()` on a node that already has this server-rendered markup, React will preserve it and
    * only attach event handlers, allowing you to have a very performant first-load experience.
    */
  def renderToString(e: ReactElement): String =
    raw.ReactDOMServer.renderToString(e.rawReactElement)

  /**
    * Similar to [[renderToString]], except this doesn't create extra DOM attributes such as `data-react-id`, that React
    * uses internally. This is useful if you want to use React as a simple static page generator, as stripping away the
    * extra attributes can save lots of bytes.
    */
  def renderToStaticMarkup(e: ReactElement): String =
    raw.ReactDOMServer.renderToStaticMarkup(e.rawReactElement)
}
