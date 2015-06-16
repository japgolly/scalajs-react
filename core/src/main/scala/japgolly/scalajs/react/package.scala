package japgolly.scalajs

import org.scalajs.dom, dom.html
import scala.scalajs.js
import js.{Dynamic, UndefOr, undefined, Object, Any => JAny, Function => JFn}

package object react {

  type TopNode = dom.Element

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

  type ReactEventH            = SyntheticEvent           [html.Element]
  type ReactClipboardEventH   = SyntheticClipboardEvent  [html.Element]
  type ReactCompositionEventH = SyntheticCompositionEvent[html.Element]
  type ReactDragEventH        = SyntheticDragEvent       [html.Element]
  type ReactFocusEventH       = SyntheticFocusEvent      [html.Element]
  //type ReactInputEventH     = SyntheticInputEvent      [html.Element]
  type ReactKeyboardEventH    = SyntheticKeyboardEvent   [html.Element]
  type ReactMouseEventH       = SyntheticMouseEvent      [html.Element]
  type ReactTouchEventH       = SyntheticTouchEvent      [html.Element]
  type ReactUIEventH          = SyntheticUIEvent         [html.Element]
  type ReactWheelEventH       = SyntheticWheelEvent      [html.Element]

  type ReactEventI            = SyntheticEvent           [html.Input]
  type ReactClipboardEventI   = SyntheticClipboardEvent  [html.Input]
  type ReactCompositionEventI = SyntheticCompositionEvent[html.Input]
  type ReactDragEventI        = SyntheticDragEvent       [html.Input]
  type ReactFocusEventI       = SyntheticFocusEvent      [html.Input]
  //type ReactInputEventI     = SyntheticInputEvent      [html.Input]
  type ReactKeyboardEventI    = SyntheticKeyboardEvent   [html.Input]
  type ReactMouseEventI       = SyntheticMouseEvent      [html.Input]
  type ReactTouchEventI       = SyntheticTouchEvent      [html.Input]
  type ReactUIEventI          = SyntheticUIEvent         [html.Input]
  type ReactWheelEventI       = SyntheticWheelEvent      [html.Input]

  type ReactEventTA            = SyntheticEvent           [html.TextArea]
  type ReactClipboardEventTA   = SyntheticClipboardEvent  [html.TextArea]
  type ReactCompositionEventTA = SyntheticCompositionEvent[html.TextArea]
  type ReactDragEventTA        = SyntheticDragEvent       [html.TextArea]
  type ReactFocusEventTA       = SyntheticFocusEvent      [html.TextArea]
  //type ReactInputEventTA     = SyntheticInputEvent      [html.TextArea]
  type ReactKeyboardEventTA    = SyntheticKeyboardEvent   [html.TextArea]
  type ReactMouseEventTA       = SyntheticMouseEvent      [html.TextArea]
  type ReactTouchEventTA       = SyntheticTouchEvent      [html.TextArea]
  type ReactUIEventTA          = SyntheticUIEvent         [html.TextArea]
  type ReactWheelEventTA       = SyntheticWheelEvent      [html.TextArea]

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

  final class RefJSComp[M <: js.Object](_name: String) extends Ref(_name) {
    override type R = M
    protected override def resolve(r: RefsObject) = r[TopNode](name).asInstanceOf[UndefOr[M]]
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

    /** A reference to a pure JS component that has its own facade type. */
    def toJS[M <: js.Object](name: String) = new RefJSComp[M](name)
  }

  @inline implicit final class ReactExt_ScalaColl[A](val _as: TraversableOnce[A]) extends AnyVal {
    @inline def toJsArray: js.Array[A] =
      js.Array(_as.toSeq: _*)
    @inline def toReactNodeArray(implicit ev: A => ReactNode): js.Array[ReactNode] = {
      val r = new js.Array[ReactNode]()
      _as.foreach(a => r.push(ev(a)))
      r
    }
  }

  @inline implicit final class ReactExt_JsArray[A](val _as: js.Array[A]) extends AnyVal {
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
  @inline implicit final class ReactExt_Any[A](val _a: A) extends AnyVal {
    @inline def wrap: WrapObj[A] = WrapObj(_a)
  }

  @inline implicit final class ReactExt_ReactObj(val _r: React.type) extends AnyVal {
    @inline def renderC[P, S, B, N <: TopNode](c: ReactComponentU[P,S,B,N], n: dom.Node)(callback: ComponentScopeM[P,S,B,N] => Unit) =
      _r.render(c, n, callback)
  }

  @inline implicit final class ReactExt_ComponentScope_P[Props](val _c: ComponentScope_P[Props]) extends AnyVal {
    @inline def props = _c._props.v
    @inline def propsChildren = _c._props.children
    @inline def propsDynamic = _c._props.asInstanceOf[js.Dynamic]
  }

  @inline implicit final class ReactExt_ComponentScope_PS[Props, State](val _c: ComponentScope_PS[Props, State]) extends AnyVal {
    @inline def getInitialState(p: Props): State = _c._getInitialState(WrapObj(p)).v
  }

  @inline implicit final class ReactExt_ComponentScope_S[State](val _c: ComponentScope_S[State]) extends AnyVal {
    @inline def state = _c._state.v
  }

  val preventDefaultF  = (_: SyntheticEvent[dom.Node]).preventDefault()
  val stopPropagationF = (_: SyntheticEvent[dom.Node]).stopPropagation()

  @inline implicit final class ReactExt_ReactComponentU[P,S,B,N <: TopNode](val _c: ReactComponentU[P,S,B,N]) extends AnyVal {
    def render(n: dom.Node) = React.render(_c, n)
  }

  @inline implicit final class ReactExt_ReactDOMElement(val _v: ReactDOMElement) extends AnyVal {
    @inline def typ = _v.`type`
  }

  @inline implicit final class ReactExt_ReactComponentU_(val _v: ReactComponentU_) extends AnyVal {
    @inline def dynamic = this.asInstanceOf[Dynamic]
  }

  @inline implicit final class ReactExt_UndefReactComponentM[N <: TopNode](val _u: UndefOr[ReactComponentM_[N]]) extends AnyVal {
    def tryFocus(): Unit = _u.foreach(_.getDOMNode() match {
      case e: html.Element => e.focus()
      case _ =>
    })
  }

  @inline implicit final class ReactExt_ReactComponentM[N <: TopNode](val _c: ReactComponentM_[N]) extends AnyVal {
    @inline def domType[N2 <: TopNode] = _c.asInstanceOf[ReactComponentM_[N2]]
  }

  @inline implicit final class ReactExt_PropsChildren(val _c: PropsChildren) extends AnyVal {
    @inline def forEach[U](f: ReactNode => U): Unit =
      React.Children.forEach(_c, (f:JFn).asInstanceOf[js.Function1[ReactNode, JAny]])

    @inline def forEach[U](f: (ReactNode, Int) => U): Unit =
      React.Children.forEach(_c, (f:JFn).asInstanceOf[js.Function2[ReactNode, Int, JAny]])

    @inline def only: Option[ReactNode] =
      try { Some(React.Children.only(_c))} catch { case t: Throwable => None}
  }

  @inline implicit final class ReactExt_ComponentScope_C(val _c: ComponentScope_C) {
    @inline def context_ : js.UndefOr[Object] =
      _c._context
  }

  // ===================================================================================================================
  // Component state access

  type OpCallback = UndefOr[() => Unit]

  /**
   * Generic read & write access to a component's state, (whatever the type of state might be).
   */
  abstract class CompStateAccess[-C, S] {
    def state(c: C): S
    def setState(c: C, s: S, cb: OpCallback): Unit
  }

  @inline implicit def toCompStateAccessOps[C, S](c: C)(implicit a: CompStateAccess[C, S]) =
    new CompStateAccess.Ops[C, S](c)

  object CompStateAccess {

    /**
     * This is a hack to avoid creating new instances for each type of state.
     */
    abstract class HK[K[_]] extends CompStateAccess[K[Any], Any] {
      final type S = Any
      final type C = K[S]
      @inline final def force[S]: CompStateAccess[K[S], S] =
        this.asInstanceOf[CompStateAccess[K[S], S]]
    }

    object Focus extends HK[CompStateFocus] {
      override def state(c: C)                          = c.get()
      override def setState(c: C, s: S, cb: OpCallback) = c.set(s, cb)
    }

    object SS extends HK[ComponentScope_SS] {
      override def state(c: C)                          = c._state.v
      override def setState(c: C, s: S, cb: OpCallback) = c._setState(WrapObj(s), cb.map[JFn](f => f))
    }

    @inline implicit def focus[S]: CompStateAccess[CompStateFocus[S], S] =
      Focus.force

    @inline implicit def cm[P, S, B, N <: TopNode]: CompStateAccess[ComponentScopeM[P, S, B, N], S] =
      CompStateAccess.SS.force[S]

    @inline implicit def cu[P, S, B]: CompStateAccess[ComponentScopeU[P, S, B], S] =
      CompStateAccess.SS.force[S]

    @inline implicit def bs[P, S]: CompStateAccess[BackendScope[P, S], S] =
      CompStateAccess.SS.force[S]

    final class Ops[C, S](val _c: C) extends AnyVal {
      // This should really be a class param but then we lose the AnyVal
      type CC = CompStateAccess[C, S]

      @inline def state(implicit C: CC): S =
        C.state(_c)

      @inline def setState(s: S, cb: OpCallback = undefined)(implicit C: CC): Unit =
        C.setState(_c, s, cb)

      @inline def modState(f: S => S, cb: OpCallback = undefined)(implicit C: CC): Unit =
        setState(f(state), cb)

      def focusStateId(implicit C: CC) = new CompStateFocus[S](
        () => _c.state,
        (a: S, cb: OpCallback) => _c.setState(a, cb))

      /** Zoom-in on a subset of the state. */
      def focusState[T](f: S => T)(g: (S, T) => S)(implicit C: CC) = new CompStateFocus[T](
        () => f(_c.state),
        (b: T, cb: OpCallback) => _c.setState(g(_c.state, b), cb))
    }
  }

  /**
   * Read & write access to a specific subset of a specific component's state.
   *
   * @tparam S The type of state.
   */
  final class CompStateFocus[S] private[react](
    val get: () => S,
    val set: (S, OpCallback) => Unit) {
  }

  object CompStateFocus {
    @inline def apply[S](get: () => S)(set: (S, OpCallback) => Unit): CompStateFocus[S] =
      new CompStateFocus(get, set)
  }

  @inline implicit def autoFocusEntireState[C, S](c: C)(implicit a: CompStateAccess[C, S]): CompStateFocus[S] =
    c.focusStateId
}
