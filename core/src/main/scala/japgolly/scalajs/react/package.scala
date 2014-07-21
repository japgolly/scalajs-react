package japgolly.scalajs

import org.scalajs.dom
import scala.scalajs.js
import js.{Dynamic, UndefOr, undefined, Object, Number, Any => JAny, Function => JFn}

package object react {

  final type MountedComponent[Props, State, Backend] = ComponentScopeM[Props, State, Backend]

  final type AnchorEvent   = SyntheticEvent[dom.HTMLAnchorElement]
  final type ButtonEvent   = SyntheticEvent[dom.HTMLButtonElement]
  final type FormEvent     = SyntheticEvent[dom.HTMLFormElement]
  final type ImageEvent    = SyntheticEvent[dom.HTMLImageElement]
  final type InputEvent    = SyntheticEvent[dom.HTMLInputElement]
  final type LabelEvent    = SyntheticEvent[dom.HTMLLabelElement]
  final type OptionEvent   = SyntheticEvent[dom.HTMLOptionElement]
  final type SelectEvent   = SyntheticEvent[dom.HTMLSelectElement]
  final type TextAreaEvent = SyntheticEvent[dom.HTMLTextAreaElement]

  // ===================================================================================================================

  // TODO WrapObj was one of the first things I did when starting with ScalaJS. Reconsider.
  /** Allows Scala classes to be used in place of `Object`. */
  trait WrapObj[+A] extends Object { val v: A }
  def WrapObj[A](v: A) =
    Dynamic.literal("v" -> v.asInstanceOf[JAny]).asInstanceOf[WrapObj[A]]

  /**
   * A named reference to an element in a React VDOM.
   */
  class Ref[T <: dom.Element](val name: String) {
    @inline final def apply(scope: ComponentScope_M): UndefOr[ReactComponentM[T]] = apply(scope.refs)
    @inline final def apply(refs: RefsObject)       : UndefOr[ReactComponentM[T]] = refs[T](name)
  }
  class RefP[I, T <: dom.Element](f: I => String) {
    @inline final def apply(i: I) = Ref[T](f(i))
    @inline final def get[S](s: ComponentScope_S[S] with ComponentScope_M)(implicit ev: S =:= I) = apply(ev(s.state))(s)
  }
  object Ref {
    def apply[T <: dom.Element](name: String) = new Ref[T](name)
    def param[I, T <: dom.Element](f: I => String) = new RefP[I, T](f)
  }

  // ===================================================================================================================

  private[this] def mkProps[P](props: P, key: Option[JAny]): WrapObj[P] = {
    val j = WrapObj(props)
    key.foreach(k => j.asInstanceOf[Dynamic].updateDynamic("key")(k))
    j
  }

  /**
   * Component constructor. Properties required.
   */
  class CompCtorP[P, S, B](val jsCtor: ComponentConstructor[P, S, B], key: Option[JAny]) {
    def apply(props: P, children: VDom*)      = jsCtor(mkProps(props, key), children: _*)
    def apply2(props: P, children: Seq[VDom]) = jsCtor(mkProps(props, key), children: _*)
    /** ↑ Workaround for what seems to be a Scala.js bug. ↑ */

    def withKey(key: JAny) = new CompCtorP(jsCtor, Some(key))
  }

  /**
   * Component constructor. Properties optional.
   */
  class CompCtorOP[P, S, B](val jsCtor: ComponentConstructor[P, S, B], key: Option[JAny], d: () => P) {
    def apply(props: Option[P], children: VDom*): ReactComponentU[P, S, B] =
      jsCtor(mkProps(props getOrElse d(), key), children: _*)

    def apply(children: VDom*): ReactComponentU[P, S, B] =
      apply(None, children: _*)

    /** Workaround for what seems to be a Scala.js bug. */
    def apply2(props: Option[P], children: Seq[VDom]) = jsCtor(mkProps(props getOrElse d(), key), children: _*)

    def withKey(key: JAny) = new CompCtorOP(jsCtor, Some(key), d)
  }

  /**
   * Component constructor. Properties not required.
   */
  class CompCtorNP[P, S, B](val jsCtor: ComponentConstructor[P, S, B], key: Option[JAny], d: () => P) {
    def apply(children: VDom*) = jsCtor(mkProps(d(), key), children: _*)
    def withKey(key: JAny) = new CompCtorNP(jsCtor, Some(key), d)
  }
//  class CompCtorNP[Props, State, Backend](u: ComponentConstructor[Props, State, Backend]) {
//    def apply(children: VDom*) = u(null, children: _*)
//  }

  // ===================================================================================================================

  @inline implicit def autoUnWrapObj[A](a: WrapObj[A]): A = a.v
  implicit final class AnyExtReact[A](val a: A) extends AnyVal {
    @inline def wrap: WrapObj[A] = WrapObj(a)
  }

  implicit final class ReactExt(val u: React.type) extends AnyVal {
    @inline def renderComponentC[P, S, B](c: ReactComponentU[P, S, B], n: dom.Node)(callback: ComponentScopeM[P, S, B] => Unit) =
      u.renderComponent(c, n, callback)
  }

  implicit final class ComponentScope_P_Ext[Props](val u: ComponentScope_P[Props]) extends AnyVal {
    @inline def props = u._props.v
    @inline def propsKey = u._props.key
    @inline def propsChildren = u._props.children
  }

  implicit final class ComponentScope_PS_Ext[Props, State](val u: ComponentScope_PS[Props, State]) extends AnyVal {
    @inline def getInitialState(p: Props): State = u._getInitialState(WrapObj(p)).v
  }

  implicit final class ComponentScope_S_Ext[State](val u: ComponentScope_S[State]) extends AnyVal {
    @inline def state = u._state.v
  }

  implicit final class SyntheticEventExt[N <: dom.Node](val u: SyntheticEvent[N]) extends AnyVal {
    def dragEvent     = u.nativeEvent.asInstanceOf[UndefOr[dom.DragEvent]]
    def keyboardEvent = u.nativeEvent.asInstanceOf[UndefOr[dom.KeyboardEvent]]
    def messageEvent  = u.nativeEvent.asInstanceOf[UndefOr[dom.MessageEvent]]
    def mouseEvent    = u.nativeEvent.asInstanceOf[UndefOr[dom.MouseEvent]]
    def mutationEvent = u.nativeEvent.asInstanceOf[UndefOr[dom.MutationEvent]]
    def storageEvent  = u.nativeEvent.asInstanceOf[UndefOr[dom.StorageEvent]]
    def textEvent     = u.nativeEvent.asInstanceOf[UndefOr[dom.TextEvent]]
    def touchEvent    = u.nativeEvent.asInstanceOf[UndefOr[dom.TouchEvent]]
  }

  val preventDefaultF  = (_: SyntheticEvent[dom.Node]).preventDefault()
  val stopPropagationF = (_: SyntheticEvent[dom.Node]).stopPropagation()

  implicit final class UndefReactComponentMExt[T <: dom.HTMLElement](val u: UndefOr[ReactComponentM[T]]) extends AnyVal {
    def tryFocus(): Unit = u.foreach(_.getDOMNode().focus())
  }

  implicit final class ReactComponentUExt[Props, State, Backend](val u: ReactComponentU[Props, State, Backend]) extends AnyVal {
    def render(n: dom.Node) = React.renderComponent(u, n)
  }

  implicit final class PropsChildrenExt(val u: PropsChildren) extends AnyVal {
    @inline def forEach[U](f: VDom => U): Unit =
      React.Children.forEach(u, (f:JFn).asInstanceOf[js.Function1[VDom, JAny]])

    @inline def forEach[U](f: (VDom, Int) => U): Unit =
      React.Children.forEach(u, (f:JFn).asInstanceOf[js.Function2[VDom, Number, JAny]])

    @inline def only: Option[VDom] =
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
