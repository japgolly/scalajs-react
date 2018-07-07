package japgolly.scalajs.react.test

import org.scalajs.dom.document
import org.scalajs.dom.html.Element
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import japgolly.scalajs.react._
import japgolly.scalajs.react.raw.{React => RawReact, ReactDOM => RawReactDOM}
import japgolly.scalajs.react.vdom.TopNode

object ReactTestUtils {

  def raw = japgolly.scalajs.react.test.raw.ReactTestUtils

  type Unmounted[M] = GenericComponent.Unmounted[_, M]
  type Mounted      = GenericComponent.MountedRaw

  private type RawM = japgolly.scalajs.react.raw.React.ComponentUntyped
  type MountedOutput = JsComponent.Mounted[_ <: js.Object, _ <: js.Object]
  private def wrapMO(r: RawM): MountedOutput = JsComponent.mounted(r)

  type CompType = GenericComponent.ComponentRaw {type Raw <: japgolly.scalajs.react.raw.React.ComponentClassUntyped }

  /** Render a component into a detached DOM node in the document. This function requires a DOM. */
  def renderIntoDocument[M](unmounted: Unmounted[M]): M = {
    val r = raw.renderIntoDocument(unmounted.raw)
    unmounted.mountRaw(r)
  }

  def renderIntoDocument(e: vdom.VdomElement): MountedOutput =
    wrapMO(raw.renderIntoDocument(e.rawElement))

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

  def newBodyElement(): Element = {
    val cont = document.createElement("div").domAsHtml
    document.body.appendChild(cont)
    cont
  }

  def removeNewBodyElement(e: Element): Unit = {
    ReactDOM unmountComponentAtNode e // Doesn't matter if no component mounted here
    document.body.removeChild(e)
  }

  def withNewBodyElement[A](use: Element => A): A = {
    val e = newBodyElement()
    try
      use(e)
    finally
      removeNewBodyElement(e)
  }

  private def mountedElement(m: RawReact.ComponentUntyped) =
    ReactDOM.findDOMNode(m).get.asElement

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
    _withRenderedIntoDocument(raw.renderIntoDocument(u.raw))(mountedElement, f compose u.mountRaw)

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
    _withRenderedIntoBody(RawReactDOM.render(u.raw, _))(mountedElement, f compose u.mountRaw)

  /** Renders a component into the document body via [[ReactDOM.render()]].
    *
    * Unlike [[ReactTestUtils.renderIntoDocument()]], this allows DOM focus to work.
    */
  def renderIntoBody[M, A](u: Unmounted[M]): M =
    u.mountRaw(RawReactDOM.render(u.raw, newBodyElement()))

  private def _withRenderedIntoBody[A, B](render: Element => A)(n: A => TopNode, use: A => B): B =
    withNewBodyElement { parent =>
      val a = render(parent)
      try
        use(a)
      finally
        ReactDOM unmountComponentAtNode n(a).parentNode
    }

  // -------------------------------------------------------------------------------------------------------------------

  private def attemptFuture[A](f: => Future[A]): Future[A] =
    try f catch { case err: Exception => Future.failed(err) }

  def withNewBodyElementAsync[A](use: Element => Future[A])(implicit ec: ExecutionContext): Future[A] = {
    val e = newBodyElement()
    attemptFuture(use(e)).andThen { case _ => removeNewBodyElement(e) }
  }

  /** Renders a component then unmounts and cleans up after use.
    *
    * @param intoBody Whether to use [[renderIntoBodyAsync()]] or [[renderIntoDocumentAsync()]].
    */
  def withRenderedAsync[M, A](u: Unmounted[M], intoBody: Boolean)(f: M => Future[A])(implicit ec: ExecutionContext): Future[A] =
    if (intoBody)
      withRenderedIntoBodyAsync(u)(f)
    else
      withRenderedIntoDocumentAsync(u)(f)

  /** Renders a component into detached DOM via [[ReactTestUtils.renderIntoDocument()]],
    * and asynchronously waits for the Future to complete before unmounting.
    */
  def withRenderedIntoDocumentAsync[M, A](u: Unmounted[M])(f: M => Future[A])(implicit ec: ExecutionContext): Future[A] =
    _withRenderedIntoDocumentAsync(raw.renderIntoDocument(u.raw))(mountedElement, f compose u.mountRaw)

  private def _withRenderedIntoDocumentAsync[A, B](a: A)(n: A => TopNode, use: A => Future[B])(implicit ec: ExecutionContext): Future[B] =
    attemptFuture(use(a)).andThen { case _ => ReactDOM unmountComponentAtNode n(a).parentNode }

  /** Renders a component into the document body via [[ReactDOM.render()]],
    * and asynchronously waits for the Future to complete before unmounting.
    */
  def withRenderedIntoBodyAsync[M, A](u: Unmounted[M])(f: M => Future[A])(implicit ec: ExecutionContext): Future[A] =
    _withRenderedIntoBodyAsync(RawReactDOM.render(u.raw, _))(mountedElement, f compose u.mountRaw)

  private def _withRenderedIntoBodyAsync[A, B](render: Element => A)(n: A => TopNode, use: A => Future[B])(implicit ec: ExecutionContext): Future[B] =
    withNewBodyElementAsync { parent =>
      val a = render(parent)
      attemptFuture(use(a)).andThen { case _ => ReactDOM unmountComponentAtNode n(a).parentNode }
    }

  // ===================================================================================================================

  def modifyProps[P, U <: GenericComponent.Unmounted[P, M], M <: GenericComponent.MountedImpure[P, _]]
      (c: GenericComponent[P, CtorType.Props, U], m: M)(f: P => P): M = {
    val container = m.getDOMNode.asMounted().node.parentNode
    val p2 = f(m.props)
    c(p2).renderIntoDOM(container.domCast[org.scalajs.dom.raw.Element])
  }

  def replaceProps[P, U <: GenericComponent.Unmounted[P, M], M <: GenericComponent.MountedImpure[P, _]]
      (c: GenericComponent[P, CtorType.Props, U], m: M)(p: P): M =
    modifyProps(c, m)(_ => p)

  private val reactDataAttrRegex = """\s+data-react\S*?\s*?=\s*?".*?"""".r
  private val reactTextCommentRegex = """<!-- /?react-text[: ].*?-->""".r

  /**
    * Turn `&lt;div data-reactroot=""&gt;hello&lt/div&gt;`
    * into `&lt;div&gt;hello&lt/div&gt;`
    */
  def removeReactInternals(html: String): String =
    reactDataAttrRegex.replaceAllIn(
      reactTextCommentRegex.replaceAllIn(
        html, ""), "")
}
