package japgolly.scalajs

import org.scalajs.dom, dom.html, dom.raw.HTMLElement
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import js.{Dynamic, Object, Any => JAny, Function => JFn}

package object react extends ReactEventAliases {

  type TopNode = dom.Element

  /** Type of components' `this.props.children` */
  type PropsChildren = js.Array[ReactNode]

  type Callback  = CallbackTo[Unit]
  type CallbackB = CallbackTo[Boolean]

  @deprecated("Use CompState.AccessD.", "0.10.2")
  type StateAccessDirect[S] = CompState.AccessD[S]

  @deprecated("CompState.Access", "0.10.2")
  type StateAccessCB    [S] = CompState.ReadCallbackWriteCallbackOps[S]

  /**
   * These exist for type inference.
   * If P,S,B,N types are needed and there's another object that has them, this is used to bridge for type inference.
   */
  trait ReactComponentTypeAux[P, S, +B, +N <: TopNode]
  @js.native
  trait ReactComponentTypeAuxJ[P, S, +B, +N <: TopNode] extends js.Object
  implicit def reactComponentTypeAuxJ[P, S, B, N <: TopNode](a: ReactComponentTypeAuxJ[P,S,B,N]): ReactComponentTypeAux[P,S,B,N] =
    a.asInstanceOf[ReactComponentTypeAux[P,S,B,N]]

  @inline final implicit def autoScalaCompToJsCompC[P, S, B, N <: TopNode](c: ReactComponentC[P, S, B, N]): ReactComponentC_ =
    c.factory

  // ===================================================================================================================

  // TODO WrapObj was one of the first things I did when starting with ScalaJS. Reconsider.
  /** Allows Scala classes to be used in place of `Object`. */
  @js.native
  trait WrapObj[+A] extends Object { val v: A = js.native }
  def WrapObj[A](v: A) =
    Dynamic.literal("v" -> v.asInstanceOf[JAny]).asInstanceOf[WrapObj[A]]

  @inline implicit final class ReactExt_ScalaColl[A](private val as: TraversableOnce[A]) extends AnyVal {
    @inline def toJsArray: js.Array[A] =
      js.Array(as.toSeq: _*)
    @inline def toReactNodeArray(implicit ev: A => ReactNode): js.Array[ReactNode] = {
      val r = new js.Array[ReactNode]()
      as.foreach(a => r.push(ev(a)))
      r
    }
  }

  @inline implicit final class ReactExt_JsArray[A](private val as: js.Array[A]) extends AnyVal {
    @inline def toReactNodeArray(implicit ev: A => ReactNode): js.Array[ReactNode] =
      as.map(ev: js.Function1[A, ReactNode])
  }

  @inline implicit def reactNodeInhabitableL                 (v: Long)               : ReactNode = v.toString.asInstanceOf[ReactNode]
  @inline implicit def reactNodeInhabitableI                 (v: Int)                : ReactNode = v.asInstanceOf[ReactNode]
  @inline implicit def reactNodeInhabitableSh                (v: Short)              : ReactNode = v.asInstanceOf[ReactNode]
  @inline implicit def reactNodeInhabitableB                 (v: Byte)               : ReactNode = v.asInstanceOf[ReactNode]
  @inline implicit def reactNodeInhabitableD                 (v: Double)             : ReactNode = v.asInstanceOf[ReactNode]
  @inline implicit def reactNodeInhabitableF                 (v: Float)              : ReactNode = v.asInstanceOf[ReactNode]
  @inline implicit def reactNodeInhabitableS                 (v: String)             : ReactNode = v.asInstanceOf[ReactNode]
  @inline implicit def reactNodeInhabitableAn                (v: js.Array[ReactNode]): ReactNode = v.asInstanceOf[ReactNode]
  @inline implicit def reactNodeInhabitableAt[T <% ReactNode](v: js.Array[T])        : ReactNode = v.toReactNodeArray
  @inline implicit def reactNodeInhabitableC [T <% ReactNode](v: TraversableOnce[T]) : ReactNode = v.toReactNodeArray

  // ===================================================================================================================

  import CompScope._

  @inline implicit final class ReactExt_CanGetInitialState[P, S](private val c: CanGetInitialState[P, S]) extends AnyVal {
    def getInitialState(p: P): S = c._getInitialState(WrapObj(p)).v
  }

  // Yes, the below can be reduced by `type Id[A] = A` and F[_] = Id | CallbackTo but Id causes problems with
  // type inference, red all over IntelliJ, and will just confuse most consumers of this lib.
  // I will bear the copy-paste burden. It's static, small and contained.

  @inline implicit final class ReactExt_HasPropsD[P](private val c: HasProps[P] with ReadDirect) extends AnyVal {
    @inline def props        : P             = c._props.v
    @inline def propsChildren: PropsChildren = c._props.children
    @inline def propsDynamic : js.Dynamic    = c._props.asInstanceOf[js.Dynamic]

    @inline def propsCB        : CallbackTo[P]             = CallbackTo(props)
    @inline def propsChildrenCB: CallbackTo[PropsChildren] = CallbackTo(propsChildren)
    @inline def propsDynamicCB : CallbackTo[js.Dynamic]    = CallbackTo(propsDynamic)
  }

  @inline implicit final class ReactExt_HasPropsCB[P](private val c: HasProps[P] with ReadCallback) extends AnyVal {
    @inline private def direct = new ReactExt_HasPropsD(c.asInstanceOf[HasProps[P] with ReadDirect])
    @inline def props        : CallbackTo[P]             = direct.propsCB
    @inline def propsChildren: CallbackTo[PropsChildren] = direct.propsChildrenCB
    @inline def propsDynamic : CallbackTo[js.Dynamic]    = direct.propsDynamicCB
  }

  @inline implicit final class ReactExt_WillUpdateOps[S](private val $: WillUpdate[_, S, _, _] with ReadDirect) extends AnyVal {
    @inline def state  : S             = $._state.v
    @inline def stateCB: CallbackTo[S] = CallbackTo(state)
  }

  @inline implicit final class ReactExt_Mounted[N <: TopNode](private val c: Mounted[N]) extends AnyVal {
    // See comments in [[Mounted]].
    @inline def getDOMNode(): N = ReactDOM.findDOMNode(c)

    def displayName: String =
      c.asInstanceOf[js.Dynamic].constructor.displayName.asInstanceOf[String]
  }

  @inline implicit final class ReactExt_MountedD[N <: TopNode](private val c: Mounted[N] with WriteDirect) extends AnyVal {
    /**
     * Can be invoked on any mounted component when you know that some deeper aspect of the component's state has
     * changed without using this.setState().
     */
    @inline def forceUpdate(): Unit = c._forceUpdate()
    /**
     * Can be invoked on any mounted component when you know that some deeper aspect of the component's state has
     * changed without using this.setState().
     */
    @inline def forceUpdateCB: Callback = CallbackTo(forceUpdate())
  }
  @inline implicit final class ReactExt_MountedCB[N <: TopNode](private val c: Mounted[N] with WriteCallback) extends AnyVal {
    @inline private def direct = new ReactExt_MountedD(c.asInstanceOf[Mounted[N] with WriteDirect])
    /**
     * Can be invoked on any mounted component when you know that some deeper aspect of the component's state has
     * changed without using this.setState().
     */
    @inline def forceUpdate: Callback = direct.forceUpdateCB
  }

  @inline implicit final class ReactExt_ReactComponentU[P,S,B,N <: TopNode](private val c: ReactComponentU[P,S,B,N]) extends AnyVal {
    @inline def render(container: dom.Node): ReactComponentM[P,S,B,N] =
      ReactDOM.render(c, container)
    @inline def render(container: dom.Node, callback: ReactComponentM[P,S,B,N] => Callback): ReactComponentM[P,S,B,N] =
      ReactDOM.render[P,S,B,N](c, container, callback.andThen(_.runNow()))
  }

  /**
   * Extensions to plain old DOM.
   */
  @inline implicit final class ReactExt_DomNode(private val n: dom.raw.Node) extends AnyVal {

    @inline def domCast[N <: dom.raw.Node]: N =
      n.asInstanceOf[N]

    @inline def domAsHtml: html.Element =
      domCast

    @inline def domToHtml: Option[html.Element] = n match {
      case e: html.Element => Some(e)
      case _ => None
    }
  }

  implicit final class ReactExt_DomNodeO[O[_], N <: dom.raw.Node](o: O[N])(implicit O: OptionLike[O]) {
    def tryTo(f: HTMLElement => Unit): Callback = Callback(O.toOption(o).flatMap(_.domToHtml).foreach(f))
    def tryFocus: Callback = tryTo(_.focus())
  }

  @inline implicit final class ReactExt_ReactComponentM[N <: TopNode](private val c: ReactComponentM_[N]) extends AnyVal {
    @inline def domType[N2 <: TopNode] = c.asInstanceOf[ReactComponentM_[N2]]
  }

  import CompState._

  implicit def ReadDirectWriteDirect[S]($: CanSetState[S] with ReadDirect with WriteDirect): ReadDirectWriteDirectOps[S] =
    new ReadDirectWriteDirect[CanSetState[S], S]($, RootAccessor[S])

  implicit def ReadCallbackWriteCallback[S]($: CanSetState[S] with ReadCallback with WriteCallback): ReadCallbackWriteCallbackOps[S] =
    new ReadCallbackWriteCallback[CanSetState[S], S]($, RootAccessor[S])

  implicit def ReadDirectWriteCallback[S]($: CanSetState[S] with ReadDirect with WriteCallback): ReadDirectWriteCallbackOps[S] =
    new ReadDirectWriteCallback[CanSetState[S], S]($, RootAccessor[S])

  // ===================================================================================================================

  @inline implicit final class ReactExt_domEventExt(private val e: dom.Event) extends AnyVal {
    /**
     * Stops the default action of an element from happening.
     * For example: Prevent a submit button from submitting a form Prevent a link from following the URL
     */
    @inline def preventDefaultCB = Callback(e.preventDefault())

    /**
     * Stops the bubbling of an event to parent elements, preventing any parent event handlers from being executed.
     */
    @inline def stopPropagationCB = Callback(e.stopPropagation())
  }

  @inline implicit final class ReactExt_ReactEventExt[E <: ReactEvent](private val e: E) extends AnyVal {
    /**
     * Stops the default action of an element from happening.
     * For example: Prevent a submit button from submitting a form Prevent a link from following the URL
     */
    @inline def preventDefaultCB = Callback(e.preventDefault())

    /**
     * Stops the bubbling of an event to parent elements, preventing any parent event handlers from being executed.
     */
    @inline def stopPropagationCB = Callback(e.stopPropagation())

    /**
     * If you want to access the event properties in an asynchronous way (eg. in a `modState(…)` function),
     * React will have recycled the event by the time the asynchronous call executes.
     *
     * This convenience function extracts a value from the event synchronously (i.e. now!) and so that it is
     * available to the asynchronous code.
     */
    @inline def extract[A, B](getNow: E => A)(useAsync: A => B): B = {
      val a = getNow(e)
      useAsync(a)
    }
  }

  @deprecated("Use e.preventDefaultCB.", "0.11.0")
  def preventDefault (e: ReactEvent): Callback = e.preventDefaultCB

  @deprecated("Use e.stopPropagationCB.", "0.11.0")
  def stopPropagation(e: ReactEvent): Callback = e.stopPropagationCB

  @inline implicit final class ReactExt_ReactDOMElement(private val e: ReactDOMElement) extends AnyVal {
    @inline def typ = e.`type`
  }

  @inline implicit final class ReactExt_ReactComponentU_(private val c: ReactComponentU_) extends AnyVal {
    @inline def dynamic = c.asInstanceOf[Dynamic]
  }

  @inline implicit final class ReactExt_PropsChildren(private val c: PropsChildren) extends AnyVal {
    @inline def forEach[U](f: ReactNode => U): Unit =
      React.Children.forEach(c, (f:JFn).asInstanceOf[js.Function1[ReactNode, JAny]])

    @inline def forEach[U](f: (ReactNode, Int) => U): Unit =
      React.Children.forEach(c, (f:JFn).asInstanceOf[js.Function2[ReactNode, Int, JAny]])

    @inline def only: Option[ReactNode] =
      try { Some(React.Children.only(c))} catch { case t: Throwable => None}
  }

  @inline implicit def ReactExt_CallbackToFuture[A](c: CallbackTo[Future[A]]) =
    new CallbackTo.ReactExt_CallbackToFuture(() => c.runNow())

  // ===================================================================================================================

  @inline implicit def ReactExt_ReactKeyboardEvent[N <: dom.Node](e: SyntheticKeyboardEvent[N]) =
    new ReactKeyboardEventOps(e)
}
