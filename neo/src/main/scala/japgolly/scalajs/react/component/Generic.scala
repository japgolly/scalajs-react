package japgolly.scalajs.react.component

import org.scalajs.dom
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.vdom
import japgolly.scalajs.react.{raw => Raw}
import japgolly.scalajs.react.{Callback, CtorType, Key, PropsChildren}

object Generic {

  type Component[P, CT[-p, +u] <: CtorType[p, u], U] = BaseComponent[P, CT, U, P, CT, U]
  type Unmounted[P, M]                               = BaseUnmounted[P, M, P, M]
  type Mounted[F[+_], P, S]                          = BaseMounted[F, P, S, P, S]

  @inline implicit def componentCtorOps[
      P1, CT1[-p, +u] <: CtorType[p, u], U1,
      P0, CT0[-p, +u] <: CtorType[p, u], U0](base: BaseComponent[P1, CT1, U1, P0, CT0, U0]): CT1[P1, U1] =
    base.ctor

  trait BaseComponent[
      P1, CT1[-p, +u] <: CtorType[p, u], U1,
      P0, CT0[-p, +u] <: CtorType[p, u], U0] {

    final type Props = P1
    final type Unmounted = U1

    type Root <: Component[P0, CT0, U0]
    def root: Root

    def cmapCtorProps[P2](f: P2 => P1): BaseComponent[P2, CT1, U1, P0, CT0, U0]
    def mapUnmounted[U2](f: U1 => U2): BaseComponent[P1, CT1, U2, P0, CT0, U0]
    def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT1[P1, U1] => CT2[P1, U1])(implicit pf: Profunctor[CT2]): BaseComponent[P1, CT2, U1, P0, CT0, U0]

    val ctor: CT1[P1, U1]
    implicit def ctorPF: Profunctor[CT1]
  }

  trait BaseUnmounted[P1, M1, P0, M0] {
    final type Props = P1
    final type Mounted = M1

    type Root <: Unmounted[P0, M0]
    def root: Root

    def mapUnmountedProps[P2](f: P1 => P2): BaseUnmounted[P2, M1, P0, M0]
    def mapMounted[M2](f: M1 => M2): BaseUnmounted[P1, M2, P0, M0]

    def reactElement: vdom.ReactElement
    def key: Option[Key]
    def ref: Option[String]
    def props: Props
    def propsChildren: PropsChildren

    def renderIntoDOM(container: Raw.ReactDOM.Container, callback: Callback = Callback.empty): Mounted
  }

  trait BaseMounted[F[+ _], P1, S1, P0, S0] {
    final type Props = P1
    final type State = S1

    protected[component] implicit def F: Effect[F]

    type Root <: Mounted[F, P0, S0]
    def root: Root

    def mapProps[P2](f: P1 => P2): BaseMounted[F, P2, S1, P0, S0]
    def xmapState[S2](f: S1 => S2)(g: S2 => S1): BaseMounted[F, P1, S2, P0, S0]
    def zoomState[S2](get: S1 => S2)(set: S2 => S1 => S1): BaseMounted[F, P1, S2, P0, S0]
    def withEffect[F2[+_]](implicit t: Effect.Trans[F, F2]): BaseMounted[F2, P1, S1, P0, S0]

    def isMounted: F[Boolean]
    def getDOMNode: F[dom.Element]
    def forceUpdate(callback: Callback = Callback.empty): F[Unit]

    def props: F[Props]
    def propsChildren: F[PropsChildren]

    def state: F[State]
    def setState(newState: State, callback: Callback = Callback.empty): F[Unit]
    def modState(mod: State => State, callback: Callback = Callback.empty): F[Unit]
  }
}

