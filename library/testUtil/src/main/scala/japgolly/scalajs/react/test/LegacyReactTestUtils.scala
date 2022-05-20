package japgolly.scalajs.react.test

import japgolly.scalajs.react._
import japgolly.scalajs.react.facade.{React => RawReact, ReactDOM => RawReactDOM}
import japgolly.scalajs.react.hooks.Hooks
import japgolly.scalajs.react.internal.CoreGeneral._
import japgolly.scalajs.react.test.ReactTestUtilsConfig.aroundReact
import japgolly.scalajs.react.util.DefaultEffects.{Async => DA, Sync => DS}
import japgolly.scalajs.react.util.Effect._
import japgolly.scalajs.react.util.JsUtil
import org.scalajs.dom
import org.scalajs.dom.html.Element
import org.scalajs.dom.{console, document}
import scala.annotation.nowarn
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.|

@nowarn("cat=deprecation")
object LegacyReactTestUtils extends LegacyReactTestUtils {
  @inline def raw = japgolly.scalajs.react.test.facade.ReactTestUtils

  @deprecated("Use .withNewDocumentElementAsync", "2.0.0")
  def withNewDocumentElementAsyncCallback[F[_], A](use: Element => F[A])(implicit F: Async[F]): F[A] =
    withNewDocumentElementAsync(use)

  @deprecated("Use .withRenderedIntoDocumentAsync", "2.0.0")
  def withRenderedIntoDocumentAsyncCallback[M](u: Unmounted[M]): WithRenderedDslF[DA, M, Element] =
    withRenderedIntoDocumentAsync(u)

  @deprecated("Use .withNewBodyElementAsync", "2.0.0")
  def withNewBodyElementAsyncCallback[F[_], A](use: Element => F[A])(implicit F: Async[F]): F[A] =
    withNewBodyElementAsync(use)

  @deprecated("Use .withRenderedIntoBodyAsync", "2.0.0")
  def withRenderedIntoBodyAsyncCallback[M](u: Unmounted[M]): WithRenderedDslF[DA, M, Element] =
    withRenderedIntoBodyAsync(u)

  @deprecated("Use .withRenderedAsync", "2.0.0")
  def withRenderedAsyncCallback[M](u: Unmounted[M], intoBody: Boolean): WithRenderedDslF[DA, M, Element] =
    withRenderedAsync(u, intoBody)

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

  private[LegacyReactTestUtils] object Internals {

    val reactDataAttrRegex = """\s+data-react\S*?\s*?=\s*?".*?"""".r
    val reactTextCommentRegex = """<!-- /?react-text[: ].*?-->""".r

    type RawM = japgolly.scalajs.react.facade.React.ComponentUntyped

    def wrapMO(r: RawM | Null): MountedOutput =
      if (r == null)
        null
      else {
        val r2 = JsUtil.notNull[RawM](r) // TODO: https://github.com/lampepfl/dotty/issues/12739
        val x = JsComponent.mounted(r2)
        x.asInstanceOf[MountedOutput]
      }

    def mountedElement(c: RawReact.ComponentUntyped | Null) =
      if (c == null) null else ReactDOM.findDOMNode(c).get.asElement()

    def parentNode(c: RawReact.ComponentUntyped | Null) = {
      val m = mountedElement(c)
      if (m == null) null else m.parentNode
    }

    def parentElement(c: RawReact.ComponentUntyped | Null) = {
      val p = parentNode(c)
      if (p == null) null else p.domCast[Element]
    }

    def attemptFuture[A](f: => Future[A]): Future[A] =
      try f catch { case err: Exception => Future.failed(err) }

    def warnOnError(prefix: String)(a: => Any): Unit =
      try {
        a
        ()
      } catch {
        case t: Throwable =>
          console.warn(s"$prefix: $t")
      }

    def unmount(container: dom.Node): Unit =
      warnOnError("Failed to unmount component") {
        ReactDOM.unmountComponentAtNode(container)
      }

    def _withNewElementAsync[F[_], A](create: => Element,
                                      use   : Element => F[A],
                                      remove: Element => Unit,
                                    )(implicit F: Async[F]): F[A] =
      F.flatMap(F.delay(create))(e =>
        F.finallyRun(use(e), F.delay(act(remove(e)))))

    def _withRenderedAsync[F[_], M, A](u: Unmounted[M], parent: Element, f: (M, Element) => F[A])
                                      (implicit F: Async[F]): F[A] =
      aroundReactAsync {
        F.flatMap(F.delay(act(RawReactDOM.render(u.raw, parent)))) { c =>
          val m = u.mountRawOrNull(c)
          F.finallyRun(f(m, parent), F.delay(act(unmountRawComponent(c))))
        }
      }

    def aroundReactAsync[F[_], A](body: F[A])(implicit F: Async[F]): F[A] = {
      val start = F.delay {
        val stop = aroundReact.start()
        F.delay(stop())
      }
      F.flatMap(start) { stop =>
        F.finallyRun(body, stop)
      }
    }

    def aroundReactFuture[A](body: => Future[A])(implicit ec: ExecutionContext): Future[A] = {
      val stop = aroundReact.start()
      val f    = body
      f.onComplete { _ => stop() }
      f
    }

  } // Internals
}

@nowarn("cat=deprecation")
trait LegacyReactTestUtils extends japgolly.scalajs.react.test.internal.ReactTestUtilExtensions {
  import LegacyReactTestUtils._
  import LegacyReactTestUtils.Internals._

  type Unmounted[M] = GenericComponent.Unmounted[_, M]
  type Mounted      = GenericComponent.MountedRaw

  type MountedOutput = JsComponent.Mounted[js.Object, js.Object]

  type CompType = GenericComponent.ComponentRaw {type Raw <: japgolly.scalajs.react.facade.React.ComponentClassUntyped }

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
    raw.act(() => { a = Some(body) })
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
  def actAsync[F[_], A](body: F[A])(implicit F: Async[F]): F[A] = {
    F.flatMap(F.delay(new Hooks.Var(Option.empty[A]))) { ref =>
      def setAsync(a: A): F[Unit] = F.delay(DS.runSync(ref.set(Some(a))))
      val body2 = F.flatMap(body)(setAsync)
      val body3 = F.fromJsPromise(raw.actAsync(F.toJsPromise(body2)))
      F.map(body3)(_ => ref.value.getOrElse(throw new RuntimeException("React's TestUtils.act didn't seem to complete.")))
    }
  }

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

  def unmountRawComponent(c: RawReact.ComponentUntyped | Null): Unit = {
    val p = parentNode(c)
    if (p != null)
      unmount(p)
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
    * Unlike [[LegacyReactTestUtils.renderIntoDocument()]], this allows DOM focus to work.
    */
  def withRenderedIntoBody[M](u: Unmounted[M]): WithRenderedDsl[M, Element] =
    new WithRenderedDsl[M, Element] {
      override def apply[A](f: (M, Element) => A): A =
        withNewBodyElement { parent =>
          aroundReact {
            val c = act(RawReactDOM.render(u.raw, parent))
            try
              f(u.mountRawOrNull(c), parent)
            finally
              unmountRawComponent(c)
          }
        }
    }

  /** Renders a component into the document body via [[ReactDOM.render()]].
    *
    * Unlike [[LegacyReactTestUtils.renderIntoDocument()]], this allows DOM focus to work.
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
      aroundReactFuture {
        val c = act(RawReactDOM.render(u.raw, parent))
        val m = u.mountRawOrNull(c)
        attemptFuture(f(m)).andThen { case _ => act(unmountRawComponent(c)) }
      }
    }

  def withNewBodyElementAsync[F[_], A](use: Element => F[A])(implicit F: Async[F]): F[A] =
    _withNewElementAsync(newBodyElement(), use, removeNewBodyElement)

  /** Renders a component into the document body via [[ReactDOM.render()]],
    * and asynchronously waits for the Async to complete before unmounting.
    */
  def withRenderedIntoBodyAsync[M](u: Unmounted[M]): WithRenderedDslF[DA, M, Element] =
    new WithRenderedDslF[DA, M, Element] {
      override def apply[A](f: (M, Element) => DA[A]) =
        withNewBodyElementAsync(_withRenderedAsync(u, _, f))
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

  /** Renders a component into detached DOM via [[LegacyReactTestUtils.renderIntoDocument()]],
    * then unmounts and cleans up after use.
    */
  def withRenderedIntoDocument[M](u: Unmounted[M]): WithRenderedDsl[M, Element] =
    new WithRenderedDsl[M, Element] {
      override def apply[A](f: (M, Element) => A): A =
        aroundReact {
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

  /** Renders a component into detached DOM via [[LegacyReactTestUtils.renderIntoDocument()]],
    * and asynchronously waits for the Future to complete before unmounting.
    */
  def withRenderedIntoDocumentFuture[M, A](u: Unmounted[M])(f: M => Future[A])(implicit ec: ExecutionContext): Future[A] =
    aroundReactFuture {
      val c = act(raw.renderIntoDocument(u.raw))
      val m = u.mountRawOrNull(c)
      attemptFuture(f(m)).andThen { case _ => act(unmountRawComponent(c)) }
    }

  def withNewDocumentElementAsync[F[_], A](use: Element => F[A])(implicit F: Async[F]): F[A] =
    _withNewElementAsync(newDocumentElement(), use, removeNewDocumentElement)

  /** Renders a component into the document body via [[ReactDOM.render()]],
    * and asynchronously waits for the Async to complete before unmounting.
    */
  def withRenderedIntoDocumentAsync[M](u: Unmounted[M]): WithRenderedDslF[DA, M, Element] =
    new WithRenderedDslF[DA, M, Element] {
      override def apply[A](f: (M, Element) => DA[A]) =
        withNewDocumentElementAsync(_withRenderedAsync(u, _, f))
  }

  // ===================================================================================================================
  // Render into body/document

  /** Renders a component then unmounts and cleans up after use.
    *
    * @param intoBody Whether to use [[renderIntoBody()]] or [[LegacyReactTestUtils.renderIntoDocument()]].
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
    * @param intoBody Whether to use [[renderIntoBodyAsync()]] or [[renderIntoDocumentAsync()]].
    */
  def withRenderedAsync[M](u: Unmounted[M], intoBody: Boolean): WithRenderedDslF[DA, M, Element] =
    if (intoBody)
      withRenderedIntoBodyAsync(u)
    else
      withRenderedIntoDocumentAsync(u)

  // ===================================================================================================================

  def modifyProps[P, U <: GenericComponent.Unmounted[P, M], M <: GenericComponent.MountedImpure[P, _]]
      (c: GenericComponent[P, CtorType.Props, U], m: M)(f: P => P): M = {
    val container = m.getDOMNode.asMounted().node.parentNode
    val p2 = f(m.props)
    act(c(p2).renderIntoDOM(container.domCast[org.scalajs.dom.Element]))
  }

  def replaceProps[P, U <: GenericComponent.Unmounted[P, M], M <: GenericComponent.MountedImpure[P, _]]
      (c: GenericComponent[P, CtorType.Props, U], m: M)(p: P): M =
    modifyProps(c, m)(_ => p)

  /**
    * Turn `&lt;div data-reactroot=""&gt;hello&lt/div&gt;`
    * into `&lt;div&gt;hello&lt/div&gt;`
    */
  def removeReactInternals(html: String): String =
    reactDataAttrRegex.replaceAllIn(
      reactTextCommentRegex.replaceAllIn(
        html,
      ""),
    "")
}
