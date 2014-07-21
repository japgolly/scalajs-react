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
  class CompCtorP[P, S, B](u: ComponentConstructor[P, S, B], key: Option[JAny]) {
    def apply(props: P, children: VDom*)      = u(mkProps(props, key), children: _*)
    def apply2(props: P, children: Seq[VDom]) = u(mkProps(props, key), children: _*)
    /** ↑ Workaround for what seems to be a Scala.js bug. ↑ */

    def withKey(key: JAny) = new CompCtorP(u, Some(key))
  }

  /**
   * Component constructor. Properties optional.
   */
  class CompCtorOP[P, S, B](u: ComponentConstructor[P, S, B], key: Option[JAny], d: () => P) {
    def apply(props: Option[P], children: VDom*): ReactComponentU[P, S, B] =
      u(mkProps(props getOrElse d(), key), children: _*)

    def apply(children: VDom*): ReactComponentU[P, S, B] =
      apply(None, children: _*)

    /** Workaround for what seems to be a Scala.js bug. */
    def apply2(props: Option[P], children: Seq[VDom]) = u(mkProps(props getOrElse d(), key), children: _*)

    def withKey(key: JAny) = new CompCtorOP(u, Some(key), d)
  }

  /**
   * Component constructor. Properties not required.
   */
  class CompCtorNP[P, S, B](u: ComponentConstructor[P, S, B], key: Option[JAny], d: () => P) {
    def apply(children: VDom*) = u(mkProps(d(), key), children: _*)
    def withKey(key: JAny) = new CompCtorNP(u, Some(key), d)
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

  implicit final class ComponentScope_SS_Ext[State](val u: ComponentScope_SS[State]) extends AnyVal {
    @inline def setState(s: State, callback: UndefOr[JFn] = undefined): Unit =
      u._setState(WrapObj(s), callback)
    @inline def setState(s: State, callback: => Unit): Unit =
      setState(s, (() => callback): JFn)

    @inline def modState(f: State => State): Unit =
      setState(f(u.state))
    // @inline def modState(f: State => State, callback: UndefOr[JFn]): Unit = setState(f(u.state), callback)
    // ↑ causes type inference issues with ↓
    @inline def modState(f: State => State, callback: => Unit): Unit =
      setState(f(u.state), (() => callback): JFn)
  }

  implicit final class SyntheticEventExt[N <: dom.Node](val u: SyntheticEvent[N]) extends AnyVal {
    def keyboardEvent = u.nativeEvent.asInstanceOf[UndefOr[dom.KeyboardEvent]]
    def messageEvent  = u.nativeEvent.asInstanceOf[UndefOr[dom.MessageEvent]]
    def mouseEvent    = u.nativeEvent.asInstanceOf[UndefOr[dom.MouseEvent]]
    def mutationEvent = u.nativeEvent.asInstanceOf[UndefOr[dom.MutationEvent]]
    def storageEvent  = u.nativeEvent.asInstanceOf[UndefOr[dom.StorageEvent]]
    def textEvent     = u.nativeEvent.asInstanceOf[UndefOr[dom.TextEvent]]
    def touchEvent    = u.nativeEvent.asInstanceOf[UndefOr[dom.TouchEvent]]
  }

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
}
