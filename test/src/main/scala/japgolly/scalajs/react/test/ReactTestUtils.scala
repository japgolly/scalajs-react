package japgolly.scalajs.react.test

import japgolly.scalajs.react._
import japgolly.scalajs.react.hooks.Hooks
import japgolly.scalajs.react.internal.JsUtil
import japgolly.scalajs.react.raw.{React => RawReact, ReactDOM => RawReactDOM}
import org.scalajs.dom
import org.scalajs.dom.html.Element
import org.scalajs.dom.{console, document}
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.|

object ReactTestUtils {

  @inline def raw = japgolly.scalajs.react.test.raw.ReactTestUtils

  type Unmounted[M] = GenericComponent.Unmounted[_, M]
  type Mounted      = GenericComponent.MountedRaw

  private type RawM = japgolly.scalajs.react.raw.React.ComponentUntyped
  type MountedOutput = JsComponent.Mounted[js.Object, js.Object]
  private def wrapMO(r: RawM | Null): MountedOutput =
    if (r == null)
      null
    else {
      val r2 = JsUtil.notNull[RawM](r) // TODO: https://github.com/lampepfl/dotty/issues/12739
      val x = JsComponent.mounted(r2)
      x.asInstanceOf[MountedOutput]
    }

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
  def act[A](body: => A): A = {
    var a = Option.empty[A]
    val cb = Callback{ a = Some(body) }
    raw.act(cb.toJsFn)
    a.getOrElse(throw new RuntimeException("React's TestUtils.act didn't seem to complete."))
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
  def actAsync[A](body: AsyncCallback[A]): AsyncCallback[A] =
    for {
      ref <- AsyncCallback.delay(new Hooks.Var(Option.empty[A]))
      _   <- AsyncCallback.fromJsPromise(raw.actAsync(body.flatMapSync(a => ref.set(Some(a))).asCallbackToJsPromise.toJsFn))
    } yield ref.value.getOrElse(throw new RuntimeException("React's TestUtils.act didn't seem to complete."))

  /** Render a component into a detached DOM node in the document. This function requires a DOM. */
  def renderIntoDocument[M](unmounted: Unmounted[M]): M = {
    val c = act(raw.renderIntoDocument(unmounted.raw))
    unmounted.mountRawOrNull(c)
  }

  def renderIntoDocument(e: vdom.VdomElement): MountedOutput =
    wrapMO(act(raw.renderIntoDocument(e.rawElement)))

  /**
   * Traverse all components in tree and accumulate all components where test(component) is true.
   * This is not that useful on its own, but it's used as a primitive for other test utils.
   */
  def findAllInRenderedTree(tree: Mounted, test: MountedOutput => Boolean): Vector[MountedOutput] =
    raw.findAllInRenderedTree(tree.raw, (m: RawM) => test(wrapMO(m))).iterator.map(wrapMO(_)).toVector

  /**
   * Finds all instance of components in the rendered tree that are DOM components with the class name
   * matching className.
   */
  def scryRenderedDOMComponentsWithClass(tree: Mounted, className: String): Vector[MountedOutput] =
    raw.scryRenderedDOMComponentsWithClass(tree.raw, className).iterator.map(wrapMO(_)).toVector

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
    raw.scryRenderedDOMComponentsWithTag(tree.raw, tagName).iterator.map(wrapMO(_)).toVector

  /**
   * Like [[scryRenderedDOMComponentsWithTag()]] but expects there to be one result, and returns that one result, or
   * throws exception if there is any other number of matches besides one.
   */
  def findRenderedDOMComponentWithTag(tree: Mounted, tagName: String): MountedOutput =
    wrapMO(raw.findRenderedDOMComponentWithTag(tree.raw, tagName))

  /** Finds all instances of components with type equal to componentClass. */
  def scryRenderedComponentsWithType(tree: Mounted, c: CompType): Vector[MountedOutput] =
    raw.scryRenderedComponentsWithType(tree.raw, c.raw).iterator.map(wrapMO(_)).toVector

  /**
   * Same as [[scryRenderedComponentsWithType()]] but expects there to be one result and returns that one result, or throws
   * exception if there is any other number of matches besides one.
   */
  def findRenderedComponentWithType(tree: Mounted, c: CompType): MountedOutput =
    wrapMO(raw.findRenderedComponentWithType(tree.raw, c.raw))

  trait WithRenderedDsl[M, R] {
    def apply     [A](f: (M, R) => A): A
    def apply     [A](f: M      => A): A = apply((m, _) => f(m))
    def withParent[A](f: R      => A): A = apply((_, r) => f(r))
  }

  trait WithRenderedDslF[F[_], M, R] {
    def apply     [A](f: (M, R) => F[A]): F[A]
    def apply     [A](f: M      => F[A]): F[A] = apply((m, _) => f(m))
    def withParent[A](f: R      => F[A]): F[A] = apply((_, r) => f(r))
  }

  def unmountRawComponent(c: RawReact.ComponentUntyped | Null): Unit = {
    val p = parentNode(c)
    if (p != null)
      unmount(p)
  }

  // ===================================================================================================================
  // Private helpers

  private def mountedElement(c: RawReact.ComponentUntyped | Null) =
    if (c == null) null else ReactDOM.findDOMNode(c).get.asElement()

  private def parentNode(c: RawReact.ComponentUntyped | Null) = {
    val m = mountedElement(c)
    if (m == null) null else m.parentNode
  }

  private def parentElement(c: RawReact.ComponentUntyped | Null) = {
    val p = parentNode(c)
    if (p == null) null else p.domCast[Element]
  }

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
      act(removeNewBodyElement(e))
  }

  /** Renders a component into the document body via [[ReactDOM.render()]],
    * then unmounts and cleans up after use.
    *
    * Unlike [[ReactTestUtils.renderIntoDocument()]], this allows DOM focus to work.
    */
  def withRenderedIntoBody[M](u: Unmounted[M]): WithRenderedDsl[M, Element] =
    new WithRenderedDsl[M, Element] {
      override def apply[A](f: (M, Element) => A): A =
        withNewBodyElement { parent =>
          val c = act(RawReactDOM.render(u.raw, parent))
          try
            f(u.mountRawOrNull(c), parent)
          finally
            unmountRawComponent(c)
        }
    }

  /** Renders a component into the document body via [[ReactDOM.render()]].
    *
    * Unlike [[ReactTestUtils.renderIntoDocument()]], this allows DOM focus to work.
    */
  def renderIntoBody[M](u: Unmounted[M]): M = {
    val c = act(RawReactDOM.render(u.raw, newBodyElement()))
    u.mountRawOrNull(c)
  }

  def withNewBodyElementFuture[A](use: Element => Future[A])(implicit ec: ExecutionContext): Future[A] = {
    val e = newBodyElement()
    attemptFuture(use(e)).andThen { case _ => act(removeNewBodyElement(e)) }
  }

  /** Renders a component into the document body via [[ReactDOM.render()]],
    * and asynchronously waits for the Future to complete before unmounting.
    */
  def withRenderedIntoBodyFuture[M, A](u: Unmounted[M])(f: M => Future[A])(implicit ec: ExecutionContext): Future[A] =
    withNewBodyElementFuture { parent =>
      val c = act(RawReactDOM.render(u.raw, parent))
      val m = u.mountRawOrNull(c)
      attemptFuture(f(m)).andThen { case _ => act(unmountRawComponent(c)) }
    }

  def withNewBodyElementAsyncCallback[A](use: Element => AsyncCallback[A]): AsyncCallback[A] =
    AsyncCallback.delay(newBodyElement())
      .flatMap(e => use(e).finallyRun(AsyncCallback.delay(act(removeNewBodyElement(e)))))

  /** Renders a component into the document body via [[ReactDOM.render()]],
    * and asynchronously waits for the AsyncCallback to complete before unmounting.
    */
  def withRenderedIntoBodyAsyncCallback[M](u: Unmounted[M]): WithRenderedDslF[AsyncCallback, M, Element] =
    new WithRenderedDslF[AsyncCallback, M, Element] {
      override def apply[A](f: (M, Element) => AsyncCallback[A]): AsyncCallback[A] =
        withNewBodyElementAsyncCallback(parent =>
          for {
            c <- AsyncCallback.delay(act(RawReactDOM.render(u.raw, parent)))
            m <- AsyncCallback.pure(u.mountRawOrNull(c))
            a <- f(m, parent).finallyRun(AsyncCallback.delay(act(unmountRawComponent(c))))
          } yield a
        )
    }

  // ===================================================================================================================
  // Render into document

  def newDocumentElement(): Element =
    document.createElement("div").domAsHtml

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
      act(removeNewDocumentElement(e))
  }

  /** Renders a component into detached DOM via [[ReactTestUtils.renderIntoDocument()]],
    * then unmounts and cleans up after use.
    */
  def withRenderedIntoDocument[M](u: Unmounted[M]): WithRenderedDsl[M, Element] =
    new WithRenderedDsl[M, Element] {
      override def apply[A](f: (M, Element) => A): A = {
        val c = act(raw.renderIntoDocument(u.raw))
        try {
          val p = parentElement(c)
          val m = u.mountRawOrNull(c)
          f(m, p)
        } finally
          act(unmountRawComponent(c))
      }
  }

  def withNewDocumentElementFuture[A](use: Element => Future[A])(implicit ec: ExecutionContext): Future[A] = {
    val e = newDocumentElement()
    attemptFuture(use(e)).andThen { case _ => act(removeNewDocumentElement(e)) }
  }

  /** Renders a component into detached DOM via [[ReactTestUtils.renderIntoDocument()]],
    * and asynchronously waits for the Future to complete before unmounting.
    */
  def withRenderedIntoDocumentFuture[M, A](u: Unmounted[M])(f: M => Future[A])(implicit ec: ExecutionContext): Future[A] = {
    val c = act(raw.renderIntoDocument(u.raw))
    val m = u.mountRawOrNull(c)
    attemptFuture(f(m)).andThen { case _ => act(unmountRawComponent(c)) }
  }

  def withNewDocumentElementAsyncCallback[A](use: Element => AsyncCallback[A]): AsyncCallback[A] =
    AsyncCallback.delay(newDocumentElement())
      .flatMap(e => use(e).finallyRun(AsyncCallback.delay(act(removeNewDocumentElement(e)))))

  /** Renders a component into the document body via [[ReactDOM.render()]],
    * and asynchronously waits for the AsyncCallback to complete before unmounting.
    */
  def withRenderedIntoDocumentAsyncCallback[M](u: Unmounted[M]): WithRenderedDslF[AsyncCallback, M, Element] =
    new WithRenderedDslF[AsyncCallback, M, Element] {
      override def apply[A](f: (M, Element) => AsyncCallback[A]): AsyncCallback[A] =
        withNewDocumentElementAsyncCallback(parent =>
          for {
            c <- AsyncCallback.delay(act(RawReactDOM.render(u.raw, parent)))
            m <- AsyncCallback.pure(u.mountRawOrNull(c))
            a <- f(m, parent).finallyRun(AsyncCallback.delay(act(unmountRawComponent(c))))
          } yield a
        )
  }

  // ===================================================================================================================
  // Render into body/document

  /** Renders a component then unmounts and cleans up after use.
    *
    * @param intoBody Whether to use [[renderIntoBody()]] or [[ReactTestUtils.renderIntoDocument()]].
    */
  def withRendered[M](u: Unmounted[M], intoBody: Boolean): WithRenderedDsl[M, Element] =
    if (intoBody)
      withRenderedIntoBody(u)
    else
      withRenderedIntoDocument(u)

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
  def withRenderedAsyncCallback[M](u: Unmounted[M], intoBody: Boolean): WithRenderedDslF[AsyncCallback, M, Element] =
    if (intoBody)
      withRenderedIntoBodyAsyncCallback(u)
    else
      withRenderedIntoDocumentAsyncCallback(u)

  // ===================================================================================================================

  def modifyProps[P, U <: GenericComponent.Unmounted[P, M], M <: GenericComponent.MountedImpure[P, _]]
      (c: GenericComponent[P, CtorType.Props, U], m: M)(f: P => P): M = {
    val container = m.getDOMNode.asMounted().node.parentNode
    val p2 = f(m.props)
    act(c(p2).renderIntoDOM(container.domCast[org.scalajs.dom.raw.Element]))
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
