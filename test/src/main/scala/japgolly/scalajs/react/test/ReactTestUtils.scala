package japgolly.scalajs.react.test

import org.scalajs.dom.document
import org.scalajs.dom.html.Element
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import japgolly.scalajs.react._
import japgolly.scalajs.react.raw.{ReactDOM => RawReactDOM}
import japgolly.scalajs.react.vdom.TopNode

object ReactTestUtils {

  def raw = japgolly.scalajs.react.test.raw.ReactTestUtils

  type Unmounted[M] = GenericComponent.BaseUnmounted[_, M, _, _]
  type Mounted      = GenericComponent.RawAccessMounted

  private type RawM = japgolly.scalajs.react.raw.ReactComponent
  type MountedOutput   = JsComponent.Mounted[js.Object, js.Object]
  private def wrapMO(r: RawM): MountedOutput = JsComponent.mounted(r)
//  type MountedOutput   = japgolly.scalajs.react.raw.ReactComponent
//  private def wrapMO(r: RawM): MountedOutput = r

  type CompType = GenericComponent.RawAccessComponent {type Raw = japgolly.scalajs.react.raw.ReactClass }

  /** Render a component into a detached DOM node in the document. This function requires a DOM. */
  def renderIntoDocument[M](unmounted: Unmounted[M]): M = {
    val r = raw.renderIntoDocument(unmounted.raw)
    unmounted.mountRaw(r)
  }

  def renderIntoDocument(e: vdom.ReactElement): MountedOutput =
    wrapMO(raw.renderIntoDocument(e.rawReactElement))

  /**
   * Traverse all components in tree and accumulate all components where test(component) is true.
   * This is not that useful on its own, but it's used as a primitive for other test utils.
   */
  def findAllInRenderedTree(tree: Mounted, test: MountedOutput => Boolean): Vector[MountedOutput] =
    raw.findAllInRenderedTree(tree.raw, (m: RawM) => test(wrapMO(m))).iterator.map(wrapMO).toVector

  /**
   * Finds all instance of components in the rendered tree that are DOM components with the class name
   * matching className.
   */
  def scryRenderedDOMComponentsWithClass(tree: Mounted, className: String): Vector[MountedOutput] =
    raw.scryRenderedDOMComponentsWithClass(tree.raw, className).iterator.map(wrapMO).toVector

  /**
   * Like [[scryRenderedDOMComponentsWithClass()]] but expects there to be one result, and returns that one result, or
   * throws exception if there is any other number of matches besides one.
   */
  def findRenderedDOMComponentWithClass(tree: Mounted, className: String): MountedOutput =
    wrapMO(raw.findRenderedDOMComponentWithClass(tree.raw, className))

  /**
   * Finds all instance of components in the rendered tree that are DOM components with the tag name
   * matching tagName.
   */
  def scryRenderedDOMComponentsWithTag(tree: Mounted, tagName: String): Vector[MountedOutput] =
    raw.scryRenderedDOMComponentsWithTag(tree.raw, tagName).iterator.map(wrapMO).toVector

  /**
   * Like [[scryRenderedDOMComponentsWithTag()]] but expects there to be one result, and returns that one result, or
   * throws exception if there is any other number of matches besides one.
   */
  def findRenderedDOMComponentWithTag(tree: Mounted, tagName: String): MountedOutput =
    wrapMO(raw.findRenderedDOMComponentWithTag(tree.raw, tagName))

  /** Finds all instances of components with type equal to componentClass. */
  def scryRenderedComponentsWithType(tree: Mounted, c: CompType): Vector[MountedOutput] =
    raw.scryRenderedComponentsWithType(tree.raw, c.raw).iterator.map(wrapMO).toVector

  /**
   * Same as [[scryRenderedComponentsWithType()]] but expects there to be one result and returns that one result, or throws
   * exception if there is any other number of matches besides one.
   */
  def findRenderedComponentWithType(tree: Mounted, c: CompType): MountedOutput =
    wrapMO(raw.findRenderedComponentWithType(tree.raw, c.raw))

  // ===================================================================================================================

  /** Renders a component then unmounts and cleans up after use.
    *
    * @param intoBody Whether to use [[renderIntoBody()]] or [[ReactTestUtils.renderIntoDocument()]].
    */
  def withRendered[M, A](u: Unmounted[M], intoBody: Boolean)(f: M => A): A =
    if (intoBody)
      withRenderedIntoBody(u)(f)
    else
      withRenderedIntoDocument(u)(f)

  /** Renders a component into detached DOM via [[ReactTestUtils.renderIntoDocument()]],
    * then unmounts and cleans up after use.
    */
  def withRenderedIntoDocument[M, A](u: Unmounted[M])(f: M => A): A =
    _withRenderedIntoDocument(raw.renderIntoDocument(u.raw))(RawReactDOM.findDOMNode, f compose u.mountRaw)

  private def _withRenderedIntoDocument[A, B](a: A)(n: A => TopNode, use: A => B): B = {
    try
      use(a)
    finally
      ReactDOM unmountComponentAtNode n(a).parentNode
  }

  /** Renders a component into the document body via [[ReactDOM.render()]],
    * then unmounts and cleans up after use.
    *
    * Unlike [[ReactTestUtils.renderIntoDocument()]], this allows DOM focus to work.
    */
  def withRenderedIntoBody[M, A](u: Unmounted[M])(f: M => A): A =
    _withRenderedIntoBody(RawReactDOM.render(u.raw, _))(RawReactDOM.findDOMNode, f compose u.mountRaw)

  /** Renders a component into the document body via [[ReactDOM.render()]].
    *
    * Unlike [[ReactTestUtils.renderIntoDocument()]], this allows DOM focus to work.
    */
  def renderIntoBody[M, A](u: Unmounted[M])(f: M => A): M =
    u.mountRaw(RawReactDOM.render(u.raw, newBodyElement()))

  private def _withRenderedIntoBody[A, B](render: Element => A)(n: A => TopNode, use: A => B): B =
    withNewBodyElement { parent =>
      val a = render(parent)
      try
        use(a)
      finally
        ReactDOM unmountComponentAtNode n(a).parentNode
    }

  def withNewBodyElement[A](use: Element => A): A = {
    val e = newBodyElement()
    try
      use(e)
    finally {
      ReactDOM unmountComponentAtNode e // Doesn't matter if no component mounted here
      document.body.removeChild(e)
    }
  }

  def newBodyElement(): Element = {
    val cont = document.createElement("div").domAsHtml
    document.body.appendChild(cont)
    cont
  }

  /** Renders a component into detached DOM via [[ReactTestUtils.renderIntoDocument()]],
    * and asynchronously waits for the Future to complete before unmounting.
    */
  def withRenderedIntoDocumentAsync[A](c: ReactElement)(f: ComponentM => Future[A])(implicit ec: ExecutionContext): Future[A] =
    _withRenderedIntoDocumentAsync(* renderIntoDocument c)(_.getDOMNode(), f)

  /** Renders a component into detached DOM via [[ReactTestUtils.renderIntoDocument()]],
    * and asynchronously waits for the Future to complete before unmounting.
    */
  def withRenderedIntoDocumentAsync[P,S,B,N <: TopNode, A](c: ReactComponentU[P,S,B,N])(f: ReactComponentM[P,S,B,N] => Future[A])(implicit ec: ExecutionContext): Future[A] =
    _withRenderedIntoDocumentAsync(* renderIntoDocument c)(_.getDOMNode(), f)

  /** Renders a component into the document body via [[ReactDOM.render()]],
    * and asynchronously waits for the Future to complete before unmounting.
    */
  def withRenderedIntoBodyAsync[A](c: ReactElement)(f: ReactComponentM_[TopNode] => Future[A])(implicit ec: ExecutionContext): Future[A] =
    _withRenderedIntoBodyAsync(ReactDOM.render(c, _))(_.getDOMNode(), f)

  /** Renders a component into the document body via [[ReactDOM.render()]],
    * and asynchronously waits for the Future to complete before unmounting.
    */
  def withRenderedIntoBodyAsync[P,S,B,N <: TopNode, A](c: ReactComponentU[P,S,B,N])(f: ReactComponentM[P,S,B,N] => Future[A])(implicit ec: ExecutionContext): Future[A] =
    _withRenderedIntoBodyAsync(ReactDOM.render(c, _))(_.getDOMNode(), f)

  private def _withRenderedIntoBodyAsync[A, B](render: Element => A)(n: A => TopNode, use: A => Future[B])(implicit ec: ExecutionContext): Future[B] = {
    val parent = _renderIntoBodyContainer()
    try {
      val a = render(parent)
      use(a).andThen {
        case _ =>
          ReactDOM unmountComponentAtNode n(a).parentNode
          document.body.removeChild(parent)
      }
    } catch {
      case e: Exception =>
        document.body.removeChild(parent)
        Future.failed(e)
    }
  }

  private def _withRenderedIntoDocumentAsync[A, B](a: A)(n: A => TopNode, use: A => Future[B])(implicit ec: ExecutionContext): Future[B] = {
    use(a).andThen {
      case _ => ReactDOM unmountComponentAtNode n(a).parentNode
    }
  }

  // ===================================================================================================================

  private val reactDataAttrRegex = """\s+data-react\S*?\s*?=\s*?".*?"""".r

  /**
    * Turn `&lt;div data-reactroot=""&gt;hello&lt/div&gt;`
    * into `&lt;div&gt;hello&lt/div&gt;`
    */
  def removeReactDataAttr(html: String): String =
    reactDataAttrRegex.replaceAllIn(html, "")
}
