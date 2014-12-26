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
  trait WrapObj[+A] extends Object { val v: A }
  def WrapObj[A](v: A) =
    Dynamic.literal("v" -> v.asInstanceOf[JAny]).asInstanceOf[WrapObj[A]]

  /**
   * A named reference to an element in a React VDOM.
   */
  abstract class Ref(final val name: String) {
    type R
    protected def resolve(r: RefsObject): UndefOr[R]
    @inline final def apply(c: ReactComponentM_[_]): UndefOr[R] = apply(c.refs)
    @inline final def apply(s: ComponentScope_M[_]): UndefOr[R] = apply(s.refs)
    @inline final def apply(r: RefsObject)         : UndefOr[R] = resolve(r)
  }

  final class RefSimple[N <: TopNode](_name: String) extends Ref(_name) {
    override type R = ReactComponentM_[N]
    protected override def resolve(r: RefsObject) = r[N](name)
  }

  final class RefComp[P, S, B, N <: TopNode](_name: String) extends Ref(_name) {
    override type R = ReactComponentM[P, S, B, N]
    protected override def resolve(r: RefsObject) = r[N](name).asInstanceOf[UndefOr[ReactComponentM[P, S, B, N]]]
  }

  final class RefParam[I, RefType <: Ref](f: I => RefType) {
    @inline final def apply(i: I): RefType = f(i)
    @inline final def get[S](s: ComponentScope_S[S] with ComponentScope_M[_])(implicit ev: S =:= I) =
      apply(ev(s.state))(s)
  }

  object Ref {
    implicit def refAsARefParam(r: Ref): UndefOr[String] = r.name

    def apply[N <: TopNode](name: String): RefSimple[N] =
      new RefSimple[N](name)

    def param[I, N <: TopNode](f: I => String): RefParam[I, RefSimple[N]] =
      new RefParam(i => Ref[N](f(i)))

    /** A reference to a Scala component. */
    def to[P, S, B, N <: TopNode](types: ReactComponentTypeAux[P, S, B, N], name: String): RefComp[P, S, B, N] =
      new RefComp[P, S, B, N](name)
  }

  implicit final class ReactExt_ScalaColl[A](val as: TraversableOnce[A]) extends AnyVal {
    @inline def toJsArray: js.Array[A] =
      js.Array(as.toSeq: _*)
    @inline def toReactNodeArray(implicit ev: A => ReactNode): js.Array[ReactNode] = {
      val r = new js.Array[ReactNode]()
      as.foreach(a => r.push(ev(a)))
      r
    }
  }

  implicit final class ReactExt_JsArray[A](val as: js.Array[A]) extends AnyVal {
    @inline def toReactNodeArray(implicit ev: A => ReactNode): js.Array[ReactNode] =
      as.map(ev: js.Function1[A, ReactNode])
  }

  @inline implicit def reactNodeInhabitableN [T <% js.Number](v: T)                  : ReactNode = (v: js.Number).asInstanceOf[ReactNode]
  @inline implicit def reactNodeInhabitableS                 (v: js.String)          : ReactNode = v.asInstanceOf[ReactNode]
  @inline implicit def reactNodeInhabitableAn                (v: js.Array[ReactNode]): ReactNode = v.asInstanceOf[ReactNode]
  @inline implicit def reactNodeInhabitableAt[T <% ReactNode](v: js.Array[T])        : ReactNode = v.toReactNodeArray
  @inline implicit def reactNodeInhabitableC [T <% ReactNode](v: TraversableOnce[T]) : ReactNode = v.toReactNodeArray
  @inline implicit def reactNodeInhabitablePC                (v: PropsChildren)      : ReactNode = v.asInstanceOf[ReactNode]

  // ===================================================================================================================

  @inline final implicit def autoJsCtor[P,S,B,N <: TopNode](c: ReactComponentC[P,S,B,N]): ReactComponentC_ = c.jsCtor

  /** Component constructor. */
  sealed trait ReactComponentC[P, S, +B, +N <: TopNode] extends ReactComponentTypeAux[P, S, B, N] {
    val jsCtor: ReactComponentCU[P,S,B,N]
  }

  object ReactComponentC {

    sealed abstract class BaseCtor[P, S, +B, +N <: TopNode] extends ReactComponentC[P, S, B, N] {

      // "Your scientists were so preoccupied with whether or not they could that they didn't stop to think if they should."
      type This[+B, +N <: TopNode] <: BaseCtor[P, S, B, N]

      val jsCtor: ReactComponentCU[P, S, B, N]

      protected val key: UndefOr[JAny]
      protected val ref: UndefOr[String]
      def set(key: UndefOr[JAny] = this.key, ref: UndefOr[String] = this.ref): This[B, N]

      final def withKey(k: JAny)  : This[B,N] = set(key = k)
      final def withRef(r: String): This[B,N] = set(ref = r)

      protected def mkProps(props: P): WrapObj[P] = {
        val j = WrapObj(props)
        key.foreach(k => j.asInstanceOf[Dynamic].updateDynamic("key")(k))
        ref.foreach(r => j.asInstanceOf[Dynamic].updateDynamic("ref")(r))
        j
      }
    }

    final class ReqProps[P, S, +B, +N <: TopNode](override val jsCtor: ReactComponentCU[P, S, B, N],
                                                  override protected val key: UndefOr[JAny],
                                                  override protected val ref: UndefOr[String]) extends BaseCtor[P, S, B, N] {
      override type This[+B, +N <: TopNode] = ReqProps[P, S, B, N]
      def set(key: UndefOr[JAny] = this.key, ref: UndefOr[String] = this.ref): This[B, N] =
        new ReqProps(jsCtor, key, ref)

      def apply(props: P, children: ReactNode*) = jsCtor(mkProps(props), children: _*)

      def withProps       (p: => P) = new ConstProps  (jsCtor, key, ref, () => p)
      def withDefaultProps(p: => P) = new DefaultProps(jsCtor, key, ref, () => p)
    }

    final class DefaultProps[P, S, +B, +N <: TopNode](override val jsCtor: ReactComponentCU[P, S, B, N],
                                                      override protected val key: UndefOr[JAny],
                                                      override protected val ref: UndefOr[String],
                                                      default: () => P) extends BaseCtor[P, S, B, N] {
      override type This[+B, +N <: TopNode] = DefaultProps[P, S, B, N]
      def set(key: UndefOr[JAny] = this.key, ref: UndefOr[String] = this.ref): This[B, N] =
        new DefaultProps(jsCtor, key, ref, default)

      def apply(props: Option[P], children: ReactNode*): ReactComponentU[P,S,B,N] =
        jsCtor(mkProps(props getOrElse default()), children: _*)

      def apply(children: ReactNode*): ReactComponentU[P,S,B,N] =
        apply(None, children: _*)
    }

    final class ConstProps[P, S, +B, +N <: TopNode](override val jsCtor: ReactComponentCU[P, S, B, N],
                                                    override protected val key: UndefOr[JAny],
                                                    override protected val ref: UndefOr[String],
                                                    props: () => P) extends BaseCtor[P, S, B, N] {
      override type This[+B, +N <: TopNode] = ConstProps[P, S, B, N]
      def set(key: UndefOr[JAny] = this.key, ref: UndefOr[String] = this.ref): This[B, N] =
        new ConstProps(jsCtor, key, ref, props)

      def apply(children: ReactNode*) = jsCtor(mkProps(props()), children: _*)
    }
  }

  // ===================================================================================================================

  @inline implicit def autoUnWrapObj[A](a: WrapObj[A]): A = a.v
  implicit final class ReactExt_Any[A](val a: A) extends AnyVal {
    @inline def wrap: WrapObj[A] = WrapObj(a)
  }

  implicit final class ReactExt_ReactObj(val u: React.type) extends AnyVal {
    @deprecated("React.renderComponentC will be deprecated in a future version. Use React.renderC instead.", "0.6.0")
    @inline def renderComponentC[P, S, B, N <: TopNode](c: ReactComponentU[P,S,B,N], n: dom.Node)(callback: ComponentScopeMN[P,S,B,N] => Unit) =
      u.render(c, n, callback)
    @inline def renderC[P, S, B, N <: TopNode](c: ReactComponentU[P,S,B,N], n: dom.Node)(callback: ComponentScopeMN[P,S,B,N] => Unit) =
      u.render(c, n, callback)
  }

  implicit final class ReactExt_ComponentScope_P[Props](val u: ComponentScope_P[Props]) extends AnyVal {
    @inline def props = u._props.v
    @inline def propsChildren = u._props.children
  }

  implicit final class ReactExt_ComponentScope_PS[Props, State](val u: ComponentScope_PS[Props, State]) extends AnyVal {
    @inline def getInitialState(p: Props): State = u._getInitialState(WrapObj(p)).v
  }

  implicit final class ReactExt_ComponentScope_S[State](val u: ComponentScope_S[State]) extends AnyVal {
    @inline def state = u._state.v
  }

  val preventDefaultF  = (_: SyntheticEvent[dom.Node]).preventDefault()
  val stopPropagationF = (_: SyntheticEvent[dom.Node]).stopPropagation()

  implicit final class ReactExt_ReactComponentU[P,S,B,N <: TopNode](val u: ReactComponentU[P,S,B,N]) extends AnyVal {
    def render(n: dom.Node) = React.render(u, n)
  }

  implicit final class ReactExt_UndefReactComponentM[N <: TopNode](val u: UndefOr[ReactComponentM_[N]]) extends AnyVal {
    def tryFocus(): Unit = u.foreach(_.getDOMNode().focus())
  }

  implicit final class ReactExt_ReactComponentM[N <: TopNode](val u: ReactComponentM_[N]) extends AnyVal {
    def domType[N2 <: TopNode] = u.asInstanceOf[ReactComponentM_[N2]]
  }

  implicit final class ReactExt_PropsChildren(val u: PropsChildren) extends AnyVal {
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
