package japgolly.scalajs

import org.scalajs.dom
import scala.scalajs.js
import js.{Dynamic, UndefOr, undefined, Object, Number, Any => JAny, Function => JFn}

package object react {

  type TopNode = dom.HTMLElement

  type ReactEvent            = SyntheticEvent           [dom.Node]
  type ReactClipboardEvent   = SyntheticClipboardEvent  [dom.Node]
  type ReactCompositionEvent = SyntheticCompositionEvent[dom.Node]
  type ReactDragEvent        = SyntheticDragEvent       [dom.Node]
  type ReactFocusEvent       = SyntheticFocusEvent      [dom.Node]
  //type ReactInputEvent     = SyntheticInputEvent      [dom.Node]
  type ReactKeyboardEvent    = SyntheticKeyboardEvent   [dom.Node]
  type ReactMouseEvent       = SyntheticMouseEvent      [dom.Node]
  type ReactTouchEvent       = SyntheticTouchEvent      [dom.Node]
  type ReactUIEvent          = SyntheticUIEvent         [dom.Node]
  type ReactWheelEvent       = SyntheticWheelEvent      [dom.Node]

  type ReactEventH            = SyntheticEvent           [dom.HTMLElement]
  type ReactClipboardEventH   = SyntheticClipboardEvent  [dom.HTMLElement]
  type ReactCompositionEventH = SyntheticCompositionEvent[dom.HTMLElement]
  type ReactDragEventH        = SyntheticDragEvent       [dom.HTMLElement]
  type ReactFocusEventH       = SyntheticFocusEvent      [dom.HTMLElement]
  //type ReactInputEventH     = SyntheticInputEvent      [dom.HTMLElement]
  type ReactKeyboardEventH    = SyntheticKeyboardEvent   [dom.HTMLElement]
  type ReactMouseEventH       = SyntheticMouseEvent      [dom.HTMLElement]
  type ReactTouchEventH       = SyntheticTouchEvent      [dom.HTMLElement]
  type ReactUIEventH          = SyntheticUIEvent         [dom.HTMLElement]
  type ReactWheelEventH       = SyntheticWheelEvent      [dom.HTMLElement]

  type ReactEventI            = SyntheticEvent           [dom.HTMLInputElement]
  type ReactClipboardEventI   = SyntheticClipboardEvent  [dom.HTMLInputElement]
  type ReactCompositionEventI = SyntheticCompositionEvent[dom.HTMLInputElement]
  type ReactDragEventI        = SyntheticDragEvent       [dom.HTMLInputElement]
  type ReactFocusEventI       = SyntheticFocusEvent      [dom.HTMLInputElement]
  //type ReactInputEventI     = SyntheticInputEvent      [dom.HTMLInputElement]
  type ReactKeyboardEventI    = SyntheticKeyboardEvent   [dom.HTMLInputElement]
  type ReactMouseEventI       = SyntheticMouseEvent      [dom.HTMLInputElement]
  type ReactTouchEventI       = SyntheticTouchEvent      [dom.HTMLInputElement]
  type ReactUIEventI          = SyntheticUIEvent         [dom.HTMLInputElement]
  type ReactWheelEventI       = SyntheticWheelEvent      [dom.HTMLInputElement]

  @deprecated("React 0.12 has introduced ReactElement which is what VDom was created to represent. Replace VDom with ReactElement.", "0.6.0")
  type VDom = ReactElement

  // ===================================================================================================================

  // TODO WrapObj was one of the first things I did when starting with ScalaJS. Reconsider.
  /** Allows Scala classes to be used in place of `Object`. */
  trait WrapObj[+A] extends Object { val v: A }
  def WrapObj[A](v: A) =
    Dynamic.literal("v" -> v.asInstanceOf[JAny]).asInstanceOf[WrapObj[A]]

  sealed trait ComponentOrNode extends Object
  @inline final implicit def autoComponentOrNodeN(n: dom.Node): ComponentOrNode =
    n.asInstanceOf[ComponentOrNode]
  @inline final implicit def autoComponentOrNodeU(c: ReactComponentU_): ComponentOrNode =
    c.asInstanceOf[ComponentOrNode]
  @inline final implicit def autoComponentOrNodeM[N <: TopNode](c: ReactComponentM_[N]): ComponentOrNode =
    c.getDOMNode()

  /**
   * A named reference to an element in a React VDOM.
   */
  class Ref[+T <: TopNode](val name: String) {
    @inline final def apply(c: ReactComponentM_[_]) : UndefOr[ReactComponentM_[T]] = apply(c.refs)
    @inline final def apply(s: ComponentScope_M[_]): UndefOr[ReactComponentM_[T]] = apply(s.refs)
    @inline final def apply(r: RefsObject)         : UndefOr[ReactComponentM_[T]] = r[T](name)
  }
  class RefP[I, T <: TopNode](f: I => String) {
    @inline final def apply(i: I) = Ref[T](f(i))
    @inline final def get[S](s: ComponentScope_S[S] with ComponentScope_M[_])(implicit ev: S =:= I) =
      apply(ev(s.state))(s)
  }
  object Ref {
    def apply[T <: TopNode](name: String)      = new Ref[T](name)
    def param[I, T <: TopNode](f: I => String) = new RefP[I, T](f)
  }

  implicit final class ScalaColl_Ext[A](val as: TraversableOnce[A]) extends AnyVal {
    @inline def toJsArray: js.Array[A] =
      js.Array(as.toSeq: _*)
    @inline def toReactNodeArray(implicit ev: A => ReactNode): js.Array[ReactNode] = {
      val r = new js.Array[ReactNode]()
      as.foreach(a => r.push(ev(a)))
      r
    }
  }

  implicit final class JsArray_Ext[A](val as: js.Array[A]) extends AnyVal {
    @inline def toReactNodeArray(implicit ev: A => ReactNode): js.Array[ReactNode] =
      as.map(ev: js.Function1[A, ReactNode])
  }

  // Scalatags causes this to fail ↓
  //@inline implicit def reactNodeInhabitableN               (v: js.Number)          : ReactNode = v.asInstanceOf[ReactNode]
  @inline implicit def reactNodeInhabitableS                 (v: js.String)          : ReactNode = v.asInstanceOf[ReactNode]
  @inline implicit def reactNodeInhabitableAn                (v: js.Array[ReactNode]): ReactNode = v.asInstanceOf[ReactNode]
  @inline implicit def reactNodeInhabitableAt[T <% ReactNode](v: js.Array[T])        : ReactNode = v.toReactNodeArray
  @inline implicit def reactNodeInhabitableC[T <% ReactNode] (v: TraversableOnce[T]) : ReactNode = v.toReactNodeArray
  @inline implicit def reactNodeInhabitablePC                (v: PropsChildren)      : ReactNode = v.asInstanceOf[ReactNode]

  // ===================================================================================================================

  @inline final implicit def autoJsCtor[P,S,B,N <: TopNode](c: ReactComponentC[P,S,B,N]): ReactComponentC_ = c.jsCtor

  /** Component constructor. */
  sealed trait ReactComponentC[P, S, +B, +N <: TopNode] {
    val jsCtor: ReactComponentCU[P,S,B,N]
  }

  object ReactComponentC {
    private[this] def mkProps[P](props: P, key: Option[JAny]): WrapObj[P] = {
      val j = WrapObj(props)
      key.foreach(k => j.asInstanceOf[Dynamic].updateDynamic("key")(k))
      j
    }

    final class ReqProps[P, S, +B, +N <: TopNode](val jsCtor: ReactComponentCU[P,S,B,N], key: Option[JAny]) extends ReactComponentC[P,S,B,N] {
      def apply(props: P, children: ReactNode*) = jsCtor(mkProps(props, key), children: _*)
      def withKey(key: JAny) = new ReqProps(jsCtor, Some(key))
    }

    final class DefaultProps[P, S, +B, +N <: TopNode](val jsCtor: ReactComponentCU[P,S,B,N], key: Option[JAny], default: () => P) extends ReactComponentC[P,S,B,N] {
      def apply(props: Option[P], children: ReactNode*): ReactComponentU[P,S,B,N] =
        jsCtor(mkProps(props getOrElse default(), key), children: _*)

      def apply(children: ReactNode*): ReactComponentU[P,S,B,N] =
        apply(None, children: _*)

      def withKey(key: JAny) = new DefaultProps(jsCtor, Some(key), default)
    }

    final class ConstProps[P, S, +B, +N <: TopNode](val jsCtor: ReactComponentCU[P,S,B,N], key: Option[JAny], props: () => P) extends ReactComponentC[P,S,B,N] {
      def apply(children: ReactNode*) = jsCtor(mkProps(props(), key), children: _*)
      def withKey(key: JAny) = new ConstProps(jsCtor, Some(key), props)
    }
  }

  // ===================================================================================================================

  @inline implicit def autoUnWrapObj[A](a: WrapObj[A]): A = a.v
  implicit final class AnyExtReact[A](val a: A) extends AnyVal {
    @inline def wrap: WrapObj[A] = WrapObj(a)
  }

  implicit final class ReactExt(val u: React.type) extends AnyVal {
    @deprecated("React.renderComponentC will be deprecated in a future version. Use React.renderC instead.", "0.6.0")
    @inline def renderComponentC[P, S, B, N <: TopNode](c: ReactComponentU[P,S,B,N], n: dom.Node)(callback: ComponentScopeMN[P,S,B,N] => Unit) =
      u.render(c, n, callback)
    @inline def renderC[P, S, B, N <: TopNode](c: ReactComponentU[P,S,B,N], n: dom.Node)(callback: ComponentScopeMN[P,S,B,N] => Unit) =
      u.render(c, n, callback)
  }

  implicit final class ComponentScope_P_Ext[Props](val u: ComponentScope_P[Props]) extends AnyVal {
    @inline def props = u._props.v
    @inline def propsChildren = u._props.children
  }

  implicit final class ComponentScope_PS_Ext[Props, State](val u: ComponentScope_PS[Props, State]) extends AnyVal {
    @inline def getInitialState(p: Props): State = u._getInitialState(WrapObj(p)).v
  }

  implicit final class ComponentScope_S_Ext[State](val u: ComponentScope_S[State]) extends AnyVal {
    @inline def state = u._state.v
  }

  val preventDefaultF  = (_: SyntheticEvent[dom.Node]).preventDefault()
  val stopPropagationF = (_: SyntheticEvent[dom.Node]).stopPropagation()

  implicit final class ReactComponentUExt[P,S,B,N <: TopNode](val u: ReactComponentU[P,S,B,N]) extends AnyVal {
    def render(n: dom.Node) = React.render(u, n)
  }

  implicit final class UndefReactComponentM_Ext[N <: TopNode](val u: UndefOr[ReactComponentM_[N]]) extends AnyVal {
    def tryFocus(): Unit = u.foreach(_.getDOMNode().focus())
  }

  implicit final class ReactComponentM_Ext[N <: TopNode](val u: ReactComponentM_[N]) extends AnyVal {
    def domType[N2 <: TopNode] = u.asInstanceOf[ReactComponentM_[N2]]
  }

  implicit final class PropsChildrenExt(val u: PropsChildren) extends AnyVal {
    @inline def forEach[U](f: ReactNode => U): Unit =
      React.Children.forEach(u, (f:JFn).asInstanceOf[js.Function1[ReactNode, JAny]])

    @inline def forEach[U](f: (ReactNode, Int) => U): Unit =
      React.Children.forEach(u, (f:JFn).asInstanceOf[js.Function2[ReactNode, Number, JAny]])

    @inline def only: Option[ReactNode] =
      try { Some(React.Children.only(u))} catch { case t: Throwable => None}
  }

  // ===================================================================================================================
  // Component state access

  type OpCallback = UndefOr[() => Unit]

  trait CompStateAccess[C[_]] {
    def state[A](f: C[A]): A
    def setState[A](f: C[A], a: A, cb: OpCallback): Unit
  }

  implicit object CompStateAccess_SS extends CompStateAccess[ComponentScope_SS] {
    override def state[A](u: ComponentScope_SS[A]): A =
      u._state.v

    override def setState[A](u: ComponentScope_SS[A], a: A, cb: OpCallback = undefined): Unit =
      u._setState(WrapObj(a), cb.map[JFn](f => f))
  }

  // CompStateAccess[C] should really be a class param but then we lose the AnyVal
  implicit final class CompStateAccessOps[C[_], A](val c: C[A]) extends AnyVal {
    type CC = CompStateAccess[C]
    
    @inline def state(implicit C: CC): A =
      C.state(c)

    @inline def setState(a: A, cb: OpCallback = undefined)(implicit C: CC): Unit =
      C.setState(c, a, cb)

    @inline def modState(f: A => A, cb: OpCallback = undefined)(implicit C: CC): Unit =
      setState(f(state), cb)

    @inline def modStateO(f: A => Option[A], cb: OpCallback = undefined)(implicit C: CC): Unit =
      f(state).fold(())(setState(_, cb))

    @inline def modStateU(f: A => UndefOr[A], cb: OpCallback = undefined)(implicit C: CC): Unit =
      f(state).fold(())(setState(_, cb))

    @inline def focusStateId(implicit C: CC) = new ComponentStateFocus[A](
      () => c.state,
      (a: A, cb: OpCallback) => c.setState(a, cb))

    @inline def focusState[B](f: A => B)(g: (A, B) => A)(implicit C: CC) = new ComponentStateFocus[B](
      () => f(c.state),
      (b: B, cb: OpCallback) => c.setState(g(c.state, b), cb))
  }

  final class ComponentStateFocus[B] private[react](
    private[react] val _g: () => B,
    private[react] val _s: (B, OpCallback) => Unit)

  implicit object ComponentStateFocusAccess extends CompStateAccess[ComponentStateFocus] {
    override def state[A](u: ComponentStateFocus[A]): A = u._g()
    override def setState[A](u: ComponentStateFocus[A], a: A, cb: OpCallback = undefined): Unit = u._s(a, cb)
  }

  @inline final implicit def autoFocusEntireState[S](s: ComponentScope_SS[S]): ComponentStateFocus[S] = s.focusStateId
  @inline final implicit def autoFocusEntireState[P,S](b: BackendScope[P,S]): ComponentStateFocus[S] = b.focusStateId
  @inline final implicit def autoFocusEntireState[P,S,B](b: ComponentScopeU[P,S,B]): ComponentStateFocus[S] = b.focusStateId

  // If Scala were a horse it'd die of thirst as you held its head under water.
  @inline final implicit def scalaHandHolding1[P,S](b: BackendScope[P,S]): CompStateAccessOps[ComponentScope_SS, S] = (b: ComponentScope_SS[S])
  @inline final implicit def scalaHandHolding2[P,S,B](b: ComponentScopeU[P,S,B]): CompStateAccessOps[ComponentScope_SS, S] = (b: ComponentScope_SS[S])
}
