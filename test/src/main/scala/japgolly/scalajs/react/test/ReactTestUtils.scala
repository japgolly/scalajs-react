package japgolly.scalajs.react.test

import japgolly.scalajs.react._
import japgolly.scalajs.react.raw.{React => RawReact, ReactDOM => RawReactDOM}
import japgolly.scalajs.react.vdom.TopNode
import org.scalajs.dom
import org.scalajs.dom.html.Element
import org.scalajs.dom.{console, document}
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

object ReactTestUtils {

  @inline def raw = japgolly.scalajs.react.test.raw.ReactTestUtils

  type Unmounted[M] = GenericComponent.Unmounted[_, M]
  type Mounted      = GenericComponent.MountedRaw

  private type RawM = japgolly.scalajs.react.raw.React.ComponentUntyped
  type MountedOutput = JsComponent.Mounted[_ <: js.Object, _ <: js.Object]
  private def wrapMO(r: RawM): MountedOutput = JsComponent.mounted(r)

  type CompType = GenericComponent.ComponentRaw {type Raw <: japgolly.scalajs.react.raw.React.ComponentClassUntyped }

  /** When writing UI tests, tasks like rendering, user events, or data fetching can be considered as "units" of
    * interaction with a user interface. React provides a helper called act() that makes sure all updates related to
    * these "units" have been processed and applied to the DOM before you make any assertions:
    *
    * {{{
    *   act {
    *     // render components
    *   }
    *   // make assertions
    * }}}
    *
    * This helps make your tests run closer to what real users would experience when using your application.
    */
  def act(body: => Any): Unit = {
    raw.act(Callback(body).toJsFn)
    ()
  }

  /** When writing UI tests, tasks like rendering, user events, or data fetching can be considered as "units" of
    * interaction with a user interface. React provides a helper called act() that makes sure all updates related to
    * these "units" have been processed and applied to the DOM before you make any assertions:
    *
    * {{{
    *   await act(async () => {
    *     // render components
    *   });
    *   // make assertions
    * }}}
    *
    * This helps make your tests run closer to what real users would experience when using your application.
    */
  def actAsync[A](body: AsyncCallback[A]): AsyncCallback[Unit] =
    AsyncCallback.fromJsPromise(raw.actAsync(body.asCallbackToJsPromise.toJsFn))

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
  // Private helpers

  private def mountedElement(m: RawReact.ComponentUntyped) =
    ReactDOM.findDOMNode(m).get.asElement()

  private def attemptFuture[A](f: => Future[A]): Future[A] =
    try f catch { case err: Exception => Future.failed(err) }

  private def warnOnError(prefix: String)(a: => Any): Unit =
    try {
      a
      ()
    } catch {
      case t: Throwable =>
        console.warn(s"$prefix: $t")
    }

  private def unmount(container: dom.Node): Unit =
    warnOnError("Failed to unmount component") {
      ReactDOM.unmountComponentAtNode(container)
    }

  // ===================================================================================================================
  // Render into body

  def newBodyElement(): Element = {
    val cont = document.createElement("div").domAsHtml
    document.body.appendChild(cont)
    cont
  }

  def removeNewBodyElement(e: Element): Unit =
    warnOnError("Failed to unmount newBodyElement") {
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
        unmount(n(a).parentNode)
    }

  def withNewBodyElementFuture[A](use: Element => Future[A])(implicit ec: ExecutionContext): Future[A] = {
    val e = newBodyElement()
    attemptFuture(use(e)).andThen { case _ => removeNewBodyElement(e) }
  }

  /** Renders a component into the document body via [[ReactDOM.render()]],
    * and asynchronously waits for the Future to complete before unmounting.
    */
  def withRenderedIntoBodyFuture[M, A](u: Unmounted[M])(f: M => Future[A])(implicit ec: ExecutionContext): Future[A] =
    _withRenderedIntoBodyFuture(RawReactDOM.render(u.raw, _))(mountedElement, f compose u.mountRaw)

  private def _withRenderedIntoBodyFuture[A, B](render: Element => A)(n: A => TopNode, use: A => Future[B])(implicit ec: ExecutionContext): Future[B] =
    withNewBodyElementFuture { parent =>
      val a = render(parent)
      attemptFuture(use(a)).andThen { case _ => unmount(n(a).parentNode) }
    }

  def withNewBodyElementAsyncCallback[A](use: Element => AsyncCallback[A]): AsyncCallback[A] =
    AsyncCallback.delay(newBodyElement())
      .flatMap(e => use(e)
        .finallyRun(AsyncCallback.delay(removeNewBodyElement(e))))

  /** Renders a component into the document body via [[ReactDOM.render()]],
    * and asynchronously waits for the AsyncCallback to complete before unmounting.
    */
  def withRenderedIntoBodyAsyncCallback[M, A](u: Unmounted[M])(f: M => AsyncCallback[A]): AsyncCallback[A] =
    _withRenderedIntoBodyAsyncCallback(RawReactDOM.render(u.raw, _))(mountedElement, f compose u.mountRaw)

  private def _withRenderedIntoBodyAsyncCallback[A, B](render: Element => A)(n: A => TopNode, use: A => AsyncCallback[B]): AsyncCallback[B] =
    withNewBodyElementAsyncCallback(parent =>
      AsyncCallback.delay(render(parent))
        .flatMap(a => use(a).finallyRun(AsyncCallback.delay(unmount(n(a).parentNode)))))

  // ===================================================================================================================
  // Render into document

  def newDocumentElement(): Element = {
    document.createElement("div").domAsHtml
  }

  def removeNewDocumentElement(e: Element): Unit =
    warnOnError("Failed to unmount newDocumentElement") {
      // This DOM is detached so the best we can do (for memory) is remove its children
      while (e.hasChildNodes()) {
        val c = e.childNodes(0)
        unmount(c) // Doesn't matter if no component mounted here
        e.removeChild(c)
      }
    }

  def withNewDocumentElement[A](use: Element => A): A = {
    val e = newDocumentElement()
    try
      use(e)
    finally
      removeNewDocumentElement(e)
  }

  /** Renders a component into detached DOM via [[ReactTestUtils.renderIntoDocument()]],
    * then unmounts and cleans up after use.
    */
  def withRenderedIntoDocument[M, A](u: Unmounted[M])(f: M => A): A =
    _withRenderedIntoDocument(raw.renderIntoDocument(u.raw))(mountedElement, f compose u.mountRaw)

  private def _withRenderedIntoDocument[A, B](a: A)(n: A => TopNode, use: A => B): B = {
    try
      use(a)
    finally
      unmount(n(a).parentNode)
  }

  def withNewDocumentElementFuture[A](use: Element => Future[A])(implicit ec: ExecutionContext): Future[A] = {
    val e = newDocumentElement()
    attemptFuture(use(e)).andThen { case _ => removeNewDocumentElement(e) }
  }

  /** Renders a component into detached DOM via [[ReactTestUtils.renderIntoDocument()]],
    * and asynchronously waits for the Future to complete before unmounting.
    */
  def withRenderedIntoDocumentFuture[M, A](u: Unmounted[M])(f: M => Future[A])(implicit ec: ExecutionContext): Future[A] =
    _withRenderedIntoDocumentFuture(raw.renderIntoDocument(u.raw))(mountedElement, f compose u.mountRaw)

  private def _withRenderedIntoDocumentFuture[A, B](a: A)(n: A => TopNode, use: A => Future[B])(implicit ec: ExecutionContext): Future[B] =
    attemptFuture(use(a)).andThen { case _ => unmount(n(a).parentNode) }

  def withNewDocumentElementAsyncCallback[A](use: Element => AsyncCallback[A]): AsyncCallback[A] =
    AsyncCallback.delay(newDocumentElement())
      .flatMap(e => use(e)
        .finallyRun(AsyncCallback.delay(removeNewDocumentElement(e))))

  /** Renders a component into the document body via [[ReactDOM.render()]],
    * and asynchronously waits for the AsyncCallback to complete before unmounting.
    */
  def withRenderedIntoDocumentAsyncCallback[M, A](u: Unmounted[M])(f: M => AsyncCallback[A]): AsyncCallback[A] =
    _withRenderedIntoDocumentAsyncCallback(RawReactDOM.render(u.raw, _))(mountedElement, f compose u.mountRaw)

  private def _withRenderedIntoDocumentAsyncCallback[A, B](render: Element => A)(n: A => TopNode, use: A => AsyncCallback[B]): AsyncCallback[B] =
    withNewDocumentElementAsyncCallback(parent =>
      AsyncCallback.delay(render(parent))
        .flatMap(a => use(a).finallyRun(AsyncCallback.delay(unmount(n(a).parentNode)))))

  // ===================================================================================================================
  // Render into body/document

  /** Renders a component then unmounts and cleans up after use.
    *
    * @param intoBody Whether to use [[renderIntoBody()]] or [[ReactTestUtils.renderIntoDocument()]].
    */
  def withRendered[M, A](u: Unmounted[M], intoBody: Boolean)(f: M => A): A =
    if (intoBody)
      withRenderedIntoBody(u)(f)
    else
      withRenderedIntoDocument(u)(f)

  /** Renders a component then unmounts and cleans up after use.
    *
    * @param intoBody Whether to use [[renderIntoBodyFuture()]] or [[renderIntoDocumentFuture()]].
    */
  def withRenderedFuture[M, A](u: Unmounted[M], intoBody: Boolean)(f: M => Future[A])(implicit ec: ExecutionContext): Future[A] =
    if (intoBody)
      withRenderedIntoBodyFuture(u)(f)
    else
      withRenderedIntoDocumentFuture(u)(f)

  /** Renders a component then unmounts and cleans up after use.
    *
    * @param intoBody Whether to use [[renderIntoBodyAsyncCallback()]] or [[renderIntoDocumentAsyncCallback()]].
    */
  def withRenderedAsyncCallback[M, A](u: Unmounted[M], intoBody: Boolean)(f: M => AsyncCallback[A]): AsyncCallback[A] =
    if (intoBody)
      withRenderedIntoBodyAsyncCallback(u)(f)
    else
      withRenderedIntoDocumentAsyncCallback(u)(f)

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
