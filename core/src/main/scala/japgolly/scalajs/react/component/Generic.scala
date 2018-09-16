package japgolly.scalajs.react.component

import org.scalajs.dom
import scala.scalajs.js
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.{Callback, CallbackTo, CtorType, ComponentDom, Key, PropsChildren, StateAccess, vdom, raw => RAW}
import scala.scalajs.js.|

object Generic {

  @inline implicit def toComponentCtor[P, CT[-p, +u] <: CtorType[p, u], U](c: ComponentSimple[P, CT, U]): CT[P, U] =
    c.ctor

  trait ComponentRaw {
    type Raw <: js.Any
    val raw: Raw
    def displayName: String
  }

  trait ComponentSimple[P, CT[-p, +u] <: CtorType[p, u], U] extends ComponentRaw {
    final type Props = P
    final type Unmounted = U

    def cmapCtorProps[P2](f: P2 => P): ComponentSimple[P2, CT, U]
    def mapUnmounted[U2](f: U => U2): ComponentSimple[P, CT, U2]
    def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT[P, U] => CT2[P, U])(implicit pf: Profunctor[CT2]): ComponentSimple[P, CT2, U]

    val ctor: CT[P, U]
    implicit def ctorPF: Profunctor[CT]


    /** Create a new JS component that wraps this component and its mappings.
      *
      * The props either need to be a subtype of js.Object, or a known singleton like Unit or Null.
      */
    final def toJsComponent(implicit c: JsFn.ToRawCtor[P, ctor.ChildrenType], r: JsFn.ToRawReactElement[U]): JsFn.Component[c.JS, c.cts.CT] =
      JsFn.fromJsFn[c.JS, ctor.ChildrenType]((p: c.JS with RAW.PropsWithChildren) =>
        r.run(ctor.applyGeneric(c(p))(ctor.liftChildren(p.children): _*))
      )(c.cts)
  }

  trait ComponentWithRoot[P1, CT1[-p, +u] <: CtorType[p, u], U1, P0, CT0[-p, +u] <: CtorType[p, u], U0] extends ComponentSimple[P1, CT1, U1] {
    type Root <: ComponentRoot[P0, CT0, U0]
    def root: Root

    override def cmapCtorProps[P2](f: P2 => P1): ComponentWithRoot[P2, CT1, U1, P0, CT0, U0]
    override def mapUnmounted[U2](f: U1 => U2): ComponentWithRoot[P1, CT1, U2, P0, CT0, U0]
    override def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT1[P1, U1] => CT2[P1, U1])(implicit pf: Profunctor[CT2]): ComponentWithRoot[P1, CT2, U1, P0, CT0, U0]
  }

  type ComponentRoot[P, CT[-p, +u] <: CtorType[p, u], U] = ComponentWithRoot[P, CT, U, P, CT, U]

  // ===================================================================================================================

  type Unmounted[P, M] = UnmountedSimple[P, M]

  trait UnmountedRaw {
    type Raw <: RAW.React.Element
    val raw: Raw
    def displayName: String
  }

  trait UnmountedSimple[P, M] extends UnmountedRaw {
    final type Props = P
    final type Mounted = M
    type Ref

    def mapUnmountedProps[P2](f: P => P2): UnmountedSimple[P2, M]
    def mapMounted[M2](f: M => M2): UnmountedSimple[P, M2]

    def vdomElement: vdom.VdomElement
    def key: Option[Key]
    def ref: Option[Ref]
    def props: Props
    def propsChildren: PropsChildren

    val mountRaw: RAW.React.ComponentUntyped => M // TODO Do better

    def renderIntoDOM(container: RAW.ReactDOM.Container, callback: Callback = Callback.empty): Mounted =
      mountRaw(RAW.ReactDOM.render(raw, container, callback.toJsFn))
  }

  trait UnmountedWithRoot[P1, M1, P0, M0] extends UnmountedSimple[P1, M1] {
    type Root <: UnmountedRoot[P0, M0]
    def root: Root

    override def mapUnmountedProps[P2](f: P1 => P2): UnmountedWithRoot[P2, M1, P0, M0]
    override def mapMounted[M2](f: M1 => M2): UnmountedWithRoot[P1, M2, P0, M0]
  }

  type UnmountedRoot[P, M] = UnmountedWithRoot[P, M, P, M]

  // ===================================================================================================================

  type Mounted[F[_], P, S] = MountedSimple[F, P, S]
  type MountedPure  [P, S] = MountedSimple[CallbackTo, P, S]
  type MountedImpure[P, S] = MountedSimple[Effect.Id, P, S]

  trait MountedRaw {
    type Raw <: RAW.React.ComponentUntyped // TODO Do better
    val raw: Raw
    def displayName: String
  }

  trait MountedSimple[F[_], P, S] extends MountedRaw with StateAccess[F, S] with StateAccess.WriteWithProps[F, P, S] {
    final type Props = P

    override type WithEffect[F2[_]]   <: MountedSimple[F2, P, S]
    override type WithMappedState[S2] <: MountedSimple[F, P, S2]
             type WithMappedProps[P2] <: MountedSimple[F, P2, S]
    def mapProps[P2](f: P => P2): WithMappedProps[P2]

    def getDOMNode: F[ComponentDom]
    def props: F[Props]
    def propsChildren: F[PropsChildren]

    def forceUpdate(callback: Callback): F[Unit]
    final def forceUpdate: F[Unit] = forceUpdate(Callback.empty)
  }

  trait MountedWithRoot[F[_], P1, S1, P0, S0] extends MountedSimple[F, P1, S1] {
    type Root <: MountedRoot[F, P0, S0]
    def root: Root

    override type WithEffect[F2[_]]   <: MountedWithRoot[F2, P1, S1, P0, S0]
    override type WithMappedState[S2] <: MountedWithRoot[F, P1, S2, P0, S0]
    override type WithMappedProps[P2] <: MountedWithRoot[F, P2, S1, P0, S0]
  }

  type MountedRoot[F[_], P, S] = MountedWithRoot[F, P, S, P, S]
}

