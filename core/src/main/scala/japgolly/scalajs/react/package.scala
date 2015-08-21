package japgolly.scalajs

import org.scalajs.dom, dom.html
import scala.scalajs.js
import js.{Dynamic, UndefOr, Object, Any => JAny, Function => JFn}

package object react extends ReactEventAliases {

  type TopNode = dom.Element

  type Callback  = CallbackTo[Unit]
  type CallbackB = CallbackTo[Boolean]

  /**
   * These exist for type inference.
   * If P,S,B,N types are needed and there's another object that has them, this is used to bridge for type inference.
   */
  trait ReactComponentTypeAux[P, S, +B, +N <: TopNode]
  trait ReactComponentTypeAuxJ[P, S, +B, +N <: TopNode] extends js.Object
  implicit def reactComponentTypeAuxJ[P, S, B, N <: TopNode](a: ReactComponentTypeAuxJ[P,S,B,N]): ReactComponentTypeAux[P,S,B,N] =
    a.asInstanceOf[ReactComponentTypeAux[P,S,B,N]]

  // ===================================================================================================================

  // TODO WrapObj was one of the first things I did when starting with ScalaJS. Reconsider.
  /** Allows Scala classes to be used in place of `Object`. */
  trait WrapObj[+A] extends Object { val v: A = js.native }
  def WrapObj[A](v: A) =
    Dynamic.literal("v" -> v.asInstanceOf[JAny]).asInstanceOf[WrapObj[A]]

  @inline implicit final class ReactExt_ScalaColl[A](private val _as: TraversableOnce[A]) extends AnyVal {
    @inline def toJsArray: js.Array[A] =
      js.Array(_as.toSeq: _*)
    @inline def toReactNodeArray(implicit ev: A => ReactNode): js.Array[ReactNode] = {
      val r = new js.Array[ReactNode]()
      _as.foreach(a => r.push(ev(a)))
      r
    }
  }

  @inline implicit final class ReactExt_JsArray[A](private val _as: js.Array[A]) extends AnyVal {
    @inline def toReactNodeArray(implicit ev: A => ReactNode): js.Array[ReactNode] =
      _as.map(ev: js.Function1[A, ReactNode])
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
  @inline implicit def reactNodeInhabitablePC                (v: PropsChildren)      : ReactNode = v.asInstanceOf[ReactNode]

  // ===================================================================================================================

  @inline final implicit def autoJsCtor[P,S,B,N <: TopNode](c: ReactComponentC[P,S,B,N]): ReactComponentC_ = c.jsCtor

  // ===================================================================================================================

  @inline implicit def autoUnWrapObj[A](a: WrapObj[A]): A = a.v
  @inline implicit final class ReactExt_Any[A](private val _a: A) extends AnyVal {
    @inline def wrap: WrapObj[A] = WrapObj(_a)
  }

  @inline implicit final class ReactExt_ReactObj(private val _r: React.type) extends AnyVal {
    @inline def renderC[P, S, B, N <: TopNode](c: ReactComponentU[P,S,B,N], n: dom.Node)(callback: ComponentScopeM[P,S,B,N] => Unit) =
      _r.render(c, n, callback)
  }

  @inline implicit final class ReactExt_ComponentScope_P[Props](private val _c: ComponentScope_P[Props]) extends AnyVal {
    @inline def props = _c._props.v
    @inline def propsChildren = _c._props.children
    @inline def propsDynamic = _c._props.asInstanceOf[js.Dynamic]
  }

  @inline implicit final class ReactExt_ComponentScope_PS[Props, State](private val _c: ComponentScope_PS[Props, State]) extends AnyVal {
    @inline def getInitialState(p: Props): State = _c._getInitialState(WrapObj(p)).v
  }

  @inline implicit final class ReactExt_ComponentScope_S[State](private val _c: ComponentScope_S[State]) extends AnyVal {
    @inline def state = _c._state.v
  }

  @inline implicit final class ReactExt_ReactEventExt(private val e: ReactEvent) extends AnyVal {
    @inline def preventDefaultCB = Callback(e.preventDefault())
    @inline def stopPropagationCB = Callback(e.stopPropagation())
  }
  def preventDefault (e: ReactEvent): Callback = e.preventDefaultCB
  def stopPropagation(e: ReactEvent): Callback = e.stopPropagationCB

  @inline implicit final class ReactExt_domEventExt(private val e: dom.Event) extends AnyVal {
    @inline def preventDefaultCB = Callback(e.preventDefault())
    @inline def stopPropagationCB = Callback(e.stopPropagation())
  }

  @inline implicit final class ReactExt_ReactComponentU[P,S,B,N <: TopNode](private val _c: ReactComponentU[P,S,B,N]) extends AnyVal {
    def render(n: dom.Node) = React.render(_c, n)
  }

  @inline implicit final class ReactExt_ComponentScope_M[N <: TopNode](private val _c: ComponentScope_M[N]) extends AnyVal {
    /**
     * Can be invoked on any mounted component when you know that some deeper aspect of the component's state has
     * changed without using this.setState().
     */
    def forceUpdate: Callback = Callback(_c._forceUpdate())
  }

  @inline implicit final class ReactExt_ReactDOMElement(private val _v: ReactDOMElement) extends AnyVal {
    @inline def typ = _v.`type`
  }

  @inline implicit final class ReactExt_ReactComponentU_(private val _v: ReactComponentU_) extends AnyVal {
    @inline def dynamic = this.asInstanceOf[Dynamic]
  }

  @inline implicit final class ReactExt_UndefReactComponentM[N <: TopNode](private val _u: UndefOr[ReactComponentM_[N]]) extends AnyVal {
    def tryFocus: Callback = Callback(
      _u.foreach(_.getDOMNode() match {
        case e: html.Element => e.focus()
        case _               => ()
      }))
  }

  @inline implicit final class ReactExt_ReactComponentM[N <: TopNode](private val _c: ReactComponentM_[N]) extends AnyVal {
    @inline def domType[N2 <: TopNode] = _c.asInstanceOf[ReactComponentM_[N2]]
  }

  @inline implicit final class ReactExt_PropsChildren(private val _c: PropsChildren) extends AnyVal {
    @inline def forEach[U](f: ReactNode => U): Unit =
      React.Children.forEach(_c, (f:JFn).asInstanceOf[js.Function1[ReactNode, JAny]])

    @inline def forEach[U](f: (ReactNode, Int) => U): Unit =
      React.Children.forEach(_c, (f:JFn).asInstanceOf[js.Function2[ReactNode, Int, JAny]])

    @inline def only: Option[ReactNode] =
      try { Some(React.Children.only(_c))} catch { case t: Throwable => None}
  }

  // ===================================================================================================================

  @inline implicit def toCompStateAccessOps[C, S](c: C)(implicit a: CompStateAccess[C, S]) =
    new CompStateAccess.Ops[C, S](c)

  @inline implicit def autoFocusEntireState[C, S](c: C)(implicit a: CompStateAccess[C, S]): CompStateFocus[S] =
    c.lift
}
