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
  @inline implicit def reactNodeInhabitablePC                (v: PropsChildren)      : ReactNode = v.asInstanceOf[ReactNode]

  // ===================================================================================================================

  @inline final implicit def autoJsCtor[P,S,B,N <: TopNode](c: ReactComponentC[P,S,B,N]): ReactComponentC_ = c.jsCtor

  // ===================================================================================================================

  @inline implicit def autoUnWrapObj[A](a: WrapObj[A]): A = a.v
  @inline implicit final class ReactExt_Any[A](private val a: A) extends AnyVal {
    @inline def wrap: WrapObj[A] = WrapObj(a)
  }

  import ComponentScope._

  @inline implicit final class ReactExt_HasProps[P](private val c: HasProps[P]) extends AnyVal {
    @inline def props         = c._props.v
    @inline def propsChildren = c._props.children
    @inline def propsDynamic  = c._props.asInstanceOf[js.Dynamic]
  }

  @inline implicit final class ReactExt_CanGetInitialState[P, S](private val c: CanGetInitialState[P, S]) extends AnyVal {
    @inline def getInitialState(p: P): S = c._getInitialState(WrapObj(p)).v
  }

  @inline implicit final class ReactExt_HasState[S](private val c: HasState[S]) extends AnyVal {
    @inline def state: S = c._state.v
  }

  @inline implicit final class ReactExt_IsMounted[N <: TopNode](private val c: IsMounted[N]) extends AnyVal {
    /**
     * Can be invoked on any mounted component when you know that some deeper aspect of the component's state has
     * changed without using this.setState().
     */
    def forceUpdate: Callback = Callback(c._forceUpdate())
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

  @inline implicit final class ReactExt_ReactComponentU[P,S,B,N <: TopNode](private val c: ReactComponentU[P,S,B,N]) extends AnyVal {
    @inline def render(container: dom.Node): ReactComponentM[P,S,B,N] =
      React.render(c, container)
    @inline def render(container: dom.Node, callback: ReactComponentM[P,S,B,N] => Callback): ReactComponentM[P,S,B,N] =
      React.render[P,S,B,N](c, container, callback.andThen(_.runNow()))
  }

  @inline implicit final class ReactExt_ReactDOMElement(private val e: ReactDOMElement) extends AnyVal {
    @inline def typ = e.`type`
  }

  @inline implicit final class ReactExt_ReactComponentU_(private val c: ReactComponentU_) extends AnyVal {
    @inline def dynamic = c.asInstanceOf[Dynamic]
  }

  @inline implicit final class ReactExt_UndefReactComponentM[N <: TopNode](private val u: UndefOr[ReactComponentM_[N]]) extends AnyVal {
    def tryFocus: Callback = Callback(
      u.foreach(_.getDOMNode() match {
        case e: html.Element => e.focus()
        case _               => ()
      }))
  }

  @inline implicit final class ReactExt_ReactComponentM[N <: TopNode](private val c: ReactComponentM_[N]) extends AnyVal {
    @inline def domType[N2 <: TopNode] = c.asInstanceOf[ReactComponentM_[N2]]
  }

  @inline implicit final class ReactExt_PropsChildren(private val c: PropsChildren) extends AnyVal {
    @inline def forEach[U](f: ReactNode => U): Unit =
      React.Children.forEach(c, (f:JFn).asInstanceOf[js.Function1[ReactNode, JAny]])

    @inline def forEach[U](f: (ReactNode, Int) => U): Unit =
      React.Children.forEach(c, (f:JFn).asInstanceOf[js.Function2[ReactNode, Int, JAny]])

    @inline def only: Option[ReactNode] =
      try { Some(React.Children.only(c))} catch { case t: Throwable => None}
  }

  // ===================================================================================================================

  @inline implicit def toCompStateAccessOps[C, S](c: C)(implicit a: CompStateAccess[C, S]) =
    new CompStateAccess.Ops[C, S](c)

  @inline implicit def autoFocusEntireState[C, S](c: C)(implicit a: CompStateAccess[C, S]): CompStateFocus[S] =
    c.lift
}
