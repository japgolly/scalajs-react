package japgolly.scalajs.react.component

import scala.scalajs.js
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.internal.JsUtil.jsNullToOption
import japgolly.scalajs.react.{Callback, Children, CtorType, ComponentDom, PropsChildren, vdom, raw => RAW}
import scala.scalajs.js.|

object Js extends JsBaseComponentTemplate[RAW.React.ComponentClassP] {

  def apply[P <: js.Object, C <: Children, S <: js.Object]
           (raw: js.Any)
           (implicit s: CtorType.Summoner[P, C], where: sourcecode.FullName, line: sourcecode.Line): Component[P, S, s.CT] = {
    InspectRaw.assertValidJsComponent(raw, where, line)
    force[P, C, S](raw)(s)
  }

  def force[P <: js.Object, C <: Children, S <: js.Object](raw: js.Any)(implicit s: CtorType.Summoner[P, C]): Component[P, S, s.CT] = {
    val rc = raw.asInstanceOf[RAW.React.ComponentClass[P, S]]
    component[P, C, S](rc)(s)
  }

  // ===================================================================================================================

  type RawMounted[P <: js.Object, S <: js.Object] = RAW.React.Component[P, S]

  type Component[P <: js.Object, S <: js.Object, CT[-p, +u] <: CtorType[p, u]] = ComponentWithRawType[P, S, RawMounted[P, S], CT]
  type Unmounted[P <: js.Object, S <: js.Object]                               = UnmountedWithRawType[P, S, RawMounted[P, S]]
  type Mounted  [P <: js.Object, S <: js.Object]                               = MountedWithRawType  [P, S, RawMounted[P, S]]

  type ComponentWithFacade[P <: js.Object, S <: js.Object, F <: js.Object, CT[-p, +u] <: CtorType[p, u]] = ComponentWithRawType[P, S, RawMounted[P, S] with F, CT]
  type UnmountedWithFacade[P <: js.Object, S <: js.Object, F <: js.Object]                               = UnmountedWithRawType[P, S, RawMounted[P, S] with F]
  type   MountedWithFacade[P <: js.Object, S <: js.Object, F <: js.Object]                               =   MountedWithRawType[P, S, RawMounted[P, S] with F]

  type ComponentWithRawType[P <: js.Object, S <: js.Object, R <: RawMounted[P, S], CT[-p, +u] <: CtorType[p, u]] = ComponentRoot[P, CT, UnmountedWithRawType[P, S, R]]
  type UnmountedWithRawType[P <: js.Object, S <: js.Object, R <: RawMounted[P, S]]                               = UnmountedRoot[P, MountedWithRawType[P, S, R]]
  type   MountedWithRawType[P <: js.Object, S <: js.Object, R <: RawMounted[P, S]]                               = MountedRoot[Effect.Id, P, S, R]

  private def readDisplayName(a: RAW.HasDisplayName): String =
    a.displayName.getOrElse("")

  override protected val rawComponentDisplayName: RAW.React.ComponentClassUntyped => String =
    readDisplayName

  // ===================================================================================================================

  sealed trait UnmountedSimple[P, M] extends Generic.UnmountedSimple[P, M] {
    override def mapUnmountedProps[P2](f: P => P2): UnmountedSimple[P2, M]
    override def mapMounted[M2](f: M => M2): UnmountedSimple[P, M2]

    override type Raw <: RAW.React.ComponentElement[_ <: js.Object]
    override final type Ref = RAW.React.Ref
    override final def displayName = readDisplayName(raw.`type`)
  }

  sealed trait UnmountedWithRoot[P1, M1, P0 <: js.Object, M0] extends UnmountedSimple[P1, M1] with Generic.UnmountedWithRoot[P1, M1, P0, M0] {
    override final type Root = UnmountedRoot[P0, M0]
    override final type Raw = RAW.React.ComponentElement[P0]
    override def mapUnmountedProps[P2](f: P1 => P2): UnmountedWithRoot[P2, M1, P0, M0]
    override def mapMounted[M2](f: M1 => M2): UnmountedWithRoot[P1, M2, P0, M0]
  }

  type UnmountedRoot[P <: js.Object, M] = UnmountedWithRoot[P, M, P, M]

  def unmountedRoot[P <: js.Object, S <: js.Object, M](r: RAW.React.ComponentElement[P], m: RAW.React.Component[P, S] => M): UnmountedRoot[P, M] =
    new UnmountedRoot[P, M] {
      override def root                              = this
      override val raw                               = r
      override val mountRaw                          = m.asInstanceOf[RAW.React.ComponentUntyped => M] // TODO Do better
      override def key                               = jsNullToOption(raw.key)
      override def ref                               = jsNullToOption(raw.ref)
      override def props                             = raw.props
      override def propsChildren                     = PropsChildren.fromRawProps(raw.props)
      override def mapUnmountedProps[P2](f: P => P2) = mappedU(this)(f, identityFn)
      override def mapMounted[M2](f: M => M2)        = mappedU(this)(identityFn, f)
    }

  private def mappedU[P2, M2, P1, M1, P0 <: js.Object, M0](from: UnmountedWithRoot[P1, M1, P0, M0])
                                                          (mp: P1 => P2, mm: M1 => M2)
      : UnmountedWithRoot[P2, M2, P0, M0] =
    new UnmountedWithRoot[P2, M2, P0, M0] {
      override def root                               = from.root
      override def props                              = mp(from.props)
      override val raw                                = from.raw
      override def key                                = from.key
      override def ref                                = from.ref
      override def propsChildren                      = from.propsChildren
      override def mapUnmountedProps[P3](f: P2 => P3) = mappedU(from)(f compose mp, mm)
      override def mapMounted[M3](f: M2 => M3)        = mappedU(from)(mp, f compose mm)
      override val mountRaw                           = mm compose from.mountRaw
    }

  // ===================================================================================================================

  sealed trait MountedSimple[F[_], P, S, R <: RawMounted[_ <: js.Object, _ <: js.Object]] extends Generic.MountedSimple[F, P, S] {

    override type WithEffect[F2[_]]    <: MountedSimple[F2, P, S, R]
    override type WithMappedProps[P2]  <: MountedSimple[F, P2, S, R]
    override type WithMappedState[S2]  <: MountedSimple[F, P, S2, R]

    override final type Raw = R
    override final def displayName = readDisplayName(raw.constructor)

    def addFacade[T <: js.Object]: MountedSimple[F, P, S, R with T]
    // def getDefaultProps: Props
    // def getInitialState: js.Object | Null
    // def render(): React.Element
  }

  sealed trait MountedWithRoot[F[_], P1, S1, R <: RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object]
      extends MountedSimple[F, P1, S1, R] with Generic.MountedWithRoot[F, P1, S1, P0, S0] {
    override final type Root                          = MountedRoot[F, P0, S0, R]
    override final type WithEffect[F2[_]]             = MountedWithRoot[F2, P1, S1, R, P0, S0]
    override final type WithMappedProps[P2]           = MountedWithRoot[F, P2, S1, R, P0, S0]
    override final type WithMappedState[S2]           = MountedWithRoot[F, P1, S2, R, P0, S0]

    final def withRawType[R2 <: RawMounted[P0, S0]]: MountedWithRoot[F, P1, S1, R2, P0, S0] =
      this.asInstanceOf[MountedWithRoot[F, P1, S1, R2, P0, S0]]

    override final def addFacade[T <: js.Object]: MountedWithRoot[F, P1, S1, R with T, P0, S0] =
      withRawType[R with T]
  }

  type MountedRoot[F[_], P <: js.Object, S <: js.Object, R <: RawMounted[P, S]] = MountedWithRoot[F, P, S, R, P, S]

  def mountedRoot[P <: js.Object, S <: js.Object, R <: RawMounted[P, S]](r: R): MountedRoot[Effect.Id, P, S, R] =
    new Template.MountedWithRoot[Effect.Id, P, S] with MountedRoot[Effect.Id, P, S, R] {
      override implicit def F    = Effect.idInstance
      override def root          = this
      override val raw           = r
      override def props         = raw.props
      override def propsChildren = PropsChildren.fromRawProps(raw.props)
      override def state         = raw.state
      override def getDOMNode    = ComponentDom.findDOMNode(raw)

      override def setState(state: S, callback: Callback): Unit =
        raw.setState(state, callback.toJsFn)

      override def modState(mod: S => S, callback: Callback): Unit = {
        val jsFn1 = mod: js.Function1[S, S]
        val jsFn2 = jsFn1.asInstanceOf[js.Function2[S, P, S | Null]]
        raw.modState(jsFn2, callback.toJsFn)
      }

      override def modState(mod: (S, P) => S, callback: Callback): Unit = {
        val jsFn1 = mod: js.Function2[S, P, S]
        val jsFn2 = jsFn1.asInstanceOf[js.Function2[S, P, S | Null]]
        raw.modState(jsFn2, callback.toJsFn)
      }

      override def setStateOption(state: Option[S], callback: Callback): Unit =
        setState(state getOrElse null.asInstanceOf[S], callback)

      override def modStateOption(mod: S => Option[S], callback: Callback): Unit =
        modState(mod(_) getOrElse null.asInstanceOf[S], callback)

      override def modStateOption(mod: (S, P) => Option[S], callback: Callback): Unit =
        modState(mod(_, _) getOrElse null.asInstanceOf[S], callback)

      override def forceUpdate(callback: Callback): Unit =
        raw.forceUpdate(callback.toJsFn)

      override type Mapped[F1[_], P1, S1] = MountedWithRoot[F1, P1, S1, R, P, S]
      override def mapped[F[_], P1, S1](mp: P => P1, ls: Lens[S, S1])(implicit ft: Effect.Trans[Effect.Id, F]) =
        mappedM(this)(mp, ls)
    }

  private def mappedM[F[_], P2, S2, P1, S1, R <: RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object]
      (from: MountedWithRoot[Effect.Id, P1, S1, R, P0, S0])
      (mp: P1 => P2, ls: Lens[S1, S2])
      (implicit ft: Effect.Trans[Effect.Id, F])
      : MountedWithRoot[F, P2, S2, R, P0, S0] =
    new Template.MountedMapped[F, P2, S2, P1, S1, P0, S0](from)(mp, ls) with MountedWithRoot[F, P2, S2, R, P0, S0] {
      override def root = from.root.withEffect[F]
      override val raw = from.raw
      override type Mapped[F3[_], P3, S3] = MountedWithRoot[F3, P3, S3, R, P0, S0]
      override def mapped[F3[_], P3, S3](mp: P1 => P3, ls: Lens[S1, S3])(implicit ft: Effect.Trans[Effect.Id, F3]) = mappedM(from)(mp, ls)(ft)
    }

  // ===================================================================================================================

  def component[P <: js.Object, C <: Children, S <: js.Object](rc: RAW.React.ComponentClass[P, S])(implicit s: CtorType.Summoner[P, C]): Component[P, S, s.CT] =
    componentRoot[P, s.CT, Unmounted[P, S]](rc, s.pf.rmap(s.summon(rc))(unmounted))(s.pf)

  def unmounted[P <: js.Object, S <: js.Object](r: RAW.React.ComponentElement[P]): Unmounted[P, S] =
    unmountedRoot(r, mounted)

  def mounted[P <: js.Object, S <: js.Object](r: RawMounted[P, S]): Mounted[P, S] =
    mountedRoot(r)

  // ===================================================================================================================

  type ComponentMapped[F[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]] =
    ComponentWithRoot[P1, CT1, UnmountedMapped[F, P1, S1, R, P0, S0], P0, CT0, UnmountedWithRawType[P0, S0, R]]

  type UnmountedMapped[F[_], P1, S1, R <: RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object] =
    UnmountedWithRoot[P1, MountedMapped[F, P1, S1, R, P0, S0], P0, MountedWithRawType[P0, S0, R]]

  type MountedMapped[F[_], P1, S1, R <: RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object] =
    MountedWithRoot[F, P1, S1, R, P0, S0]

  implicit def toJsUnmountedOps[F[_], P1, S1, R <: RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object](x: UnmountedMapped[F, P1, S1, R, P0, S0]): JsUnmountedOps[F, P1, S1, R, P0, S0] = new JsUnmountedOps(x)
  implicit def toJsComponentOps[F[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]](x: ComponentMapped[F, P1, S1, CT1, R, P0, S0, CT0]): JsComponentOps[F, P1, S1, CT1, R, P0, S0, CT0] = new JsComponentOps(x)

  // Scala bug requires special help for the Effect.Id case
  implicit def toJsUnmountedOpsI[P1, S1, R <: RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object](x: UnmountedMapped[Effect.Id, P1, S1, R, P0, S0]): JsUnmountedOps[Effect.Id, P1, S1, R, P0, S0] = new JsUnmountedOps(x)
  implicit def toJsComponentOpsI[P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]](x: ComponentMapped[Effect.Id, P1, S1, CT1, R, P0, S0, CT0]): JsComponentOps[Effect.Id, P1, S1, CT1, R, P0, S0, CT0] = new JsComponentOps(x)

  final class JsUnmountedOps[
      F[_],
      P1, S1,
      R <: RawMounted[P0, S0],
      P0 <: js.Object, S0 <: js.Object](private val self: UnmountedMapped[F, P1, S1, R, P0, S0]) extends AnyVal {

    def withRawType[R2 <: RawMounted[P0, S0]]: UnmountedMapped[F, P1, S1, R2, P0, S0] =
      self.asInstanceOf[UnmountedMapped[F, P1, S1, R2, P0, S0]]
    def addFacade[T <: js.Object]: UnmountedMapped[F, P1, S1, R with T, P0, S0] =
      withRawType[R with T]
    def mapProps[P2](f: P1 => P2): UnmountedMapped[F, P2, S1, R, P0, S0] =
      self.mapUnmountedProps(f).mapMounted(_ mapProps f)
    def xmapState[S2](f: S1 => S2)(g: S2 => S1): UnmountedMapped[F, P1, S2, R, P0, S0] =
      self.mapMounted(_.xmapState(f)(g))
    def zoomState[S2](get: S1 => S2)(set: S2 => S1 => S1): UnmountedMapped[F, P1, S2, R, P0, S0] =
      self.mapMounted(_.zoomState(get)(set))
  }

  final class JsComponentOps[
      F[_],
      P1, S1, CT1[-p, +u] <: CtorType[p, u],
      R <: RawMounted[P0, S0],
      P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]]
      (private val self: ComponentMapped[F, P1, S1, CT1, R, P0, S0, CT0]) extends AnyVal {

    def withRawType[R2 <: RawMounted[P0, S0]]: ComponentMapped[F, P1, S1, CT1, R2, P0, S0, CT0] =
      self.asInstanceOf[ComponentMapped[F, P1, S1, CT1, R2, P0, S0, CT0]]
    def addFacade[T <: js.Object]: ComponentMapped[F, P1, S1, CT1, R with T, P0, S0, CT0] =
      withRawType[R with T]
    def xmapProps[P2](f: P1 => P2)(g: P2 => P1): ComponentMapped[F, P2, S1, CT1, R, P0, S0, CT0] =
      self.cmapCtorProps(g).mapUnmounted(_ mapProps f)
    def xmapState[S2](f: S1 => S2)(g: S2 => S1): ComponentMapped[F, P1, S2, CT1, R, P0, S0, CT0] =
      self.mapUnmounted(_.xmapState(f)(g))
    def zoomState[S2](get: S1 => S2)(set: S2 => S1 => S1): ComponentMapped[F, P1, S2, CT1, R, P0, S0, CT0] =
      self.mapUnmounted(_.zoomState(get)(set))
    def mapMounted[M2](f: MountedMapped[F, P1, S1, R, P0, S0] => M2) =
      self.mapUnmounted(_ mapMounted f)

    import japgolly.scalajs.react.Ref

    def withRef(ref: Ref.Handle[R]): ComponentMapped[F, P1, S1, CT1, R, P0, S0, CT0] =
      self.mapCtorType(ct => CtorType.hackBackToSelf(ct)(ct.withRawProp("ref", ref.raw)))(self.ctorPF)

    def withRef(r: Option[Ref.Handle[R]]): ComponentMapped[F, P1, S1, CT1, R, P0, S0, CT0] =
      r match {
        case None    => self
        case Some(h) => withRef(h)
      }
  }
}
