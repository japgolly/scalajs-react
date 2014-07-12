package japgolly.scalajs

import org.scalajs.dom
import scala.scalajs.js._

package object react {

  type MountedComponent[Props, State, Backend] = ComponentScopeM[Props, State, Backend]

  // ===================================================================================================================

  // TODO WrapObj was one of the first things I did when starting with ScalaJS. Reconsider.
  /** Allows Scala classes to be used in place of `Object`. */
  trait WrapObj[+A] extends Object { val v: A }
  def WrapObj[A](v: A) =
    Dynamic.literal("v" -> v.asInstanceOf[Any]).asInstanceOf[WrapObj[A]]

  /**
   * A named reference to an element in a React VDOM.
   */
  class Ref[T <: dom.Element](val name: String) {
    @inline final def apply(scope: ComponentScope_M): UndefOr[ReactComponentM[T]] = apply(scope.refs)
    @inline final def apply(refs: RefsObject): UndefOr[ReactComponentM[T]] = refs[T](name)
  }
  class RefP[I, T <: dom.Element](f: I => String) {
    @inline final def apply(i: I) = Ref[T](f(i))
    @inline final def get[S](s: ComponentScope_S[S] with ComponentScope_M)(implicit ev: S =:= I) = apply(ev(s.state))(s)
  }
  object Ref {
    def apply[T <: dom.Element](name: String) = new Ref[T](name)
    def param[I, T <: dom.Element](f: I => String) = new RefP[I, T](f)
  }

  class CompCtorP[Props, State, Backend](u: ComponentConstructor[Props, State, Backend]) {
    def apply(props: Props, children: VDom*) = u(WrapObj(props), children: _*)
    /** Workaround for what seems to be a Scala.js bug. */
    def apply2(props: Props, children: Seq[VDom]) = u(WrapObj(props), children: _*)
  }

  class CompCtorOP[Props, State, Backend](u: ComponentConstructor[Props, State, Backend], d: () => Props) {
    def apply(props: Option[Props], children: VDom*): ReactComponentU[Props, State, Backend] =
      u(WrapObj(props getOrElse d()), children: _*)

    def apply(children: VDom*): ReactComponentU[Props, State, Backend] =
      apply(None, children: _*)

    /** Workaround for what seems to be a Scala.js bug. */
    def apply2(props: Option[Props], children: Seq[VDom]) = u(WrapObj(props getOrElse d()), children: _*)

  }

  class CompCtorNP[Props, State, Backend](u: ComponentConstructor[Props, State, Backend], d: () => Props) {
    def apply(children: VDom*) = u(WrapObj(d()), children: _*)
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
    @inline def propsChildren = u.asInstanceOf[Dynamic].props.children.asInstanceOf[PropsChildren]
  }

  implicit final class ComponentScope_PS_Ext[Props, State](val u: ComponentScope_PS[Props, State]) extends AnyVal {
    @inline def getInitialState(p: Props): State = u._getInitialState(WrapObj(p)).v
  }

  implicit final class ComponentScope_S_Ext[State](val u: ComponentScope_S[State]) extends AnyVal {
    @inline def state = u._state.v
  }

  implicit final class ComponentScope_SS_Ext[State](val u: ComponentScope_SS[State]) extends AnyVal {
    @inline def setState(s: State, callback: UndefOr[Function] = undefined): Unit =
      u._setState(WrapObj(s), callback)
    @inline def setState(s: State, callback: => Unit): Unit =
      setState(s, (() => callback): Function)

    @inline def modState(f: State => State): Unit =
      setState(f(u.state))
    // @inline def modState(f: State => State, callback: UndefOr[Function]): Unit = setState(f(u.state), callback)
    // ↑ causes type inference issues with ↓
    @inline def modState(f: State => State, callback: => Unit): Unit =
      modState(f, (() => callback): Function)
  }

  implicit final class SyntheticEventExt[N <: dom.Node](val u: SyntheticEvent[N]) extends AnyVal {
    def keyboardEvent = u.nativeEvent.asInstanceOf[dom.KeyboardEvent]
    def messageEvent  = u.nativeEvent.asInstanceOf[dom.MessageEvent]
    def mouseEvent    = u.nativeEvent.asInstanceOf[dom.MouseEvent]
    def mutationEvent = u.nativeEvent.asInstanceOf[dom.MutationEvent]
    def storageEvent  = u.nativeEvent.asInstanceOf[dom.StorageEvent]
    def textEvent     = u.nativeEvent.asInstanceOf[dom.TextEvent]
    def touchEvent    = u.nativeEvent.asInstanceOf[dom.TouchEvent]
  }

  implicit final class UndefReactComponentMExt[T <: dom.HTMLElement](val u: UndefOr[ReactComponentM[T]]) extends AnyVal {
    def tryFocus(): Unit = u.foreach(_.getDOMNode().focus())
  }

  implicit final class ReactComponentUExt[Props, State, Backend](val u: ReactComponentU[Props, State, Backend]) extends AnyVal {
    def render(n: dom.Node) = React.renderComponent(u, n)
  }

  implicit final class PropsChildrenExt(val u: PropsChildren) extends AnyVal {
    @inline def forEach[U](f: VDom => U): Unit = React.Children.forEach(u, (f:Function).asInstanceOf[Function1[VDom, Any]])
    @inline def forEach[U](f: (VDom, Int) => U): Unit = React.Children.forEach(u, (f:Function).asInstanceOf[Function2[VDom, Number, Any]])
    @inline def only: Option[VDom] = try { Some(React.Children.only(u))} catch { case t: Throwable => None}
  }
}
