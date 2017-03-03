package japgolly.scalajs.react.component

import org.scalajs.dom
import scala.scalajs.js
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.vdom
import japgolly.scalajs.react.{raw => RAW}
import japgolly.scalajs.react.{Callback, CtorType, Key, PropsChildren, StateAccess}

object Generic {

  type ComponentRoot[P, CT[-p, +u] <: CtorType[p, u], U] = ComponentWithRoot[P, CT, U, P, CT, U]
  type UnmountedRoot[P, M]                               = UnmountedWithRoot[P, M, P, M]
  type MountedRoot[F[_], P, S]                           = MountedWithRoot[F, P, S, P, S]

  @inline implicit def componentCtorOps[
      P1, CT1[-p, +u] <: CtorType[p, u], U1,
      P0, CT0[-p, +u] <: CtorType[p, u], U0](base: ComponentWithRoot[P1, CT1, U1, P0, CT0, U0]): CT1[P1, U1] =
    base.ctor

  trait ComponentRaw {
    type Raw <: js.Any
    val raw: Raw
    def displayName: String
  }

  trait ComponentWithRoot[
      P1, CT1[-p, +u] <: CtorType[p, u], U1,
      P0, CT0[-p, +u] <: CtorType[p, u], U0] extends ComponentRaw {

    final type Props = P1
    final type Unmounted = U1

    type Root <: ComponentRoot[P0, CT0, U0]
    def root: Root

    def cmapCtorProps[P2](f: P2 => P1): ComponentWithRoot[P2, CT1, U1, P0, CT0, U0]
    def mapUnmounted[U2](f: U1 => U2): ComponentWithRoot[P1, CT1, U2, P0, CT0, U0]
    def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT1[P1, U1] => CT2[P1, U1])(implicit pf: Profunctor[CT2]): ComponentWithRoot[P1, CT2, U1, P0, CT0, U0]

    val ctor: CT1[P1, U1]
    implicit def ctorPF: Profunctor[CT1]
  }

  trait UnmountedRaw {
    type Raw <: RAW.ReactElement
    val raw: Raw
    def displayName: String
  }

  trait UnmountedWithRoot[P1, M1, P0, M0] extends UnmountedRaw {
    final type Props = P1
    final type Mounted = M1

    type Root <: UnmountedRoot[P0, M0]
    def root: Root

    def mapUnmountedProps[P2](f: P1 => P2): UnmountedWithRoot[P2, M1, P0, M0]
    def mapMounted[M2](f: M1 => M2): UnmountedWithRoot[P1, M2, P0, M0]

    def vdomElement: vdom.VdomElement
    def key: Option[Key]
    def ref: Option[String]
    def props: Props
    def propsChildren: PropsChildren

    val mountRaw: RAW.ReactComponent => M1

    def renderIntoDOM(container: RAW.ReactDOM.Container, callback: Callback = Callback.empty): Mounted =
      mountRaw(RAW.ReactDOM.render(raw, container, callback.toJsFn))
  }

  trait MountedRaw {
    type Raw <: RAW.ReactComponent
    val raw: Raw
    def displayName: String
  }

  trait MountedWithRoot[F[_], P1, S1, P0, S0] extends MountedRaw with StateAccess[F, S1] {
    final type Props = P1

    type Root <: MountedRoot[F, P0, S0]
    def root: Root

    override type WithEffect[F2[_]] <: MountedWithRoot[F2, P1, S1, P0, S0]
    override type WithMappedState[S2] <: MountedWithRoot[F, P1, S2, P0, S0]
    type WithMappedProps[P2] <: MountedWithRoot[F, P2, S1, P0, S0]
    def mapProps[P2](f: P1 => P2): WithMappedProps[P2]

    def isMounted: F[Boolean]
    def getDOMNode: F[dom.Element]
    def props: F[Props]
    def propsChildren: F[PropsChildren]

    def forceUpdate(callback: Callback): F[Unit]
    final def forceUpdate: F[Unit] = forceUpdate(Callback.empty)
  }
}

