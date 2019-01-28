package japgolly.scalajs.react.component

import japgolly.scalajs.react.{raw => Raw, _}
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.internal.JsUtil.jsNullToOption
import scala.scalajs.js

object JsForwardRef {

  type Component[P <: js.Object, R, CT[-p, +u] <: CtorType[p, u]] = ComponentRoot[P, R, CT, Unmounted[P, R]]
  type Unmounted[P <: js.Object, R]                               = UnmountedRoot[P, R]
  type Mounted                                                    = Unit

  def apply[P <: js.Object, C <: Children, R]
           (raw: js.Any)
           (implicit s: CtorType.Summoner[P, C], where: sourcecode.FullName, line: sourcecode.Line): Component[P, R, s.CT] = {
    InspectRaw.assertValidJsForwardRefComponent(raw, where, line)
    force[P, C, R](raw)(s)
  }

  def force[P <: js.Object, C <: Children, R](raw: js.Any)(implicit s: CtorType.Summoner[P, C]): Component[P, R, s.CT] = {
    val rc = raw.asInstanceOf[Raw.React.ForwardRefComponent[P, R]]
    componentRoot[P, R, s.CT, Unmounted[P, R]](rc, s.pf.rmap(s.summon(rc))(u => unmountedRoot(u)))(s.pf)
  }

  @inline def fromRaw[P <: js.Object, C <: Children, R](r: Raw.React.ForwardRefComponent[P, R])
                                                       (implicit s: CtorType.Summoner[P, C]): Component[P, R, s.CT] =
    force[P, C, R](r)(s)

  // ===================================================================================================================

  // Aligned to https://github.com/facebook/react/pull/13615/files
  private def staticDisplayName = "ForwardRef"

  private def rawComponentDisplayName: Raw.React.ForwardRefComponent[_ <: js.Object, _] => String =
    _.displayName.toOption match {
      case Some(n) => s"ForwardRef($n)"
      case None    => staticDisplayName
    }

  sealed trait ComponentSimple[P, R, CT[-p, +u] <: CtorType[p, u], U] extends Generic.ComponentSimple[P, CT, U] {
    override final def displayName = rawComponentDisplayName(raw)

    override type Raw <: Raw.React.ForwardRefComponent[_ <: js.Object, R]
    override def mapRaw(f: Raw => Raw): ComponentSimple[P, R, CT, U]
    override def cmapCtorProps[P2](f: P2 => P): ComponentSimple[P2, R, CT, U]
    override def mapUnmounted[U2](f: U => U2): ComponentSimple[P, R, CT, U2]
    override def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT[P, U] => CT2[P, U])(implicit pf: Profunctor[CT2]): ComponentSimple[P, R, CT2, U]

    def withRef[RR >: R](ref: Ref.Handle[RR]): Generic.ComponentSimple[P, CT, U]
  }

  sealed trait ComponentWithRoot[
      P1, R, CT1[-p, +u] <: CtorType[p, u], U1,
      P0 <: js.Object, CT0[-p, +u] <: CtorType[p, u], U0]
      extends ComponentSimple[P1, R, CT1, U1] with Generic.ComponentWithRoot[P1, CT1, U1, P0, CT0, U0] {

    override final type Raw = Raw.React.ForwardRefComponent[P0, R]
    override final type Root = ComponentRoot[P0, R, CT0, U0]

    override def mapRaw(f: Raw => Raw): ComponentWithRoot[P1, R, CT1, U1, P0, CT0, U0]
    override def cmapCtorProps[P2](f: P2 => P1): ComponentWithRoot[P2, R, CT1, U1, P0, CT0, U0]
    override def mapUnmounted[U2](f: U1 => U2): ComponentWithRoot[P1, R, CT1, U2, P0, CT0, U0]
    override def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT1[P1, U1] => CT2[P1, U1])(implicit pf: Profunctor[CT2]): ComponentWithRoot[P1, R, CT2, U1, P0, CT0, U0]

    override def withRef[RR >: R](ref: Ref.Handle[RR]): Generic.ComponentWithRoot[P1, CT1, U1, P0, CT0, U0]
  }

  final type ComponentRoot[P <: js.Object, R, CT[-p, +u] <: CtorType[p, u], U] =
    ComponentWithRoot[P, R, CT, U, P, CT, U]

  final def componentRoot[P <: js.Object, R, CT[-p, +u] <: CtorType[p, u], U](rc: Raw.React.ForwardRefComponent[P, R], c: CT[P, U])
                                                                             (implicit pf: Profunctor[CT]): ComponentRoot[P, R, CT, U] =
    new ComponentRoot[P, R, CT, U] {
      override def root = this
      override val raw = rc
      override val ctor = c
      override implicit def ctorPF = pf
      override def mapRaw(f: Raw => Raw) = componentRoot(f(rc), c)(pf)
      override def cmapCtorProps[P2](f: P2 => P) = mappedC(this)(f, identityFn, identityFn, pf)
      override def mapUnmounted[U2](f: U => U2) = mappedC(this)(identityFn, identityFn, f, pf)
      override def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT[P, U] => CT2[P, U])(implicit pf: Profunctor[CT2]) =
        mappedC(this)(identityFn, f, identityFn, pf)

      override def withRef[RR >: R](ref: Ref.Handle[RR]) =
        componentRoot(rc, setRef(c, ref))(pf)
    }

  private def setRef[CT[-p, +u] <: CtorType[p, u], P, U](c: CT[P, U], ref: Ref.Handle[_]): CT[P, U] =
    CtorType.hackBackToSelf(c)(c.withRawProp("ref", ref.raw))

  protected final def mappedC[R,
      P2, CT2[-p, +u] <: CtorType[p, u], U2,
      P1, CT1[-p, +u] <: CtorType[p, u], U1,
      P0 <: js.Object, CT0[-p, +u] <: CtorType[p, u], U0]
      (from: ComponentWithRoot[P1, R, CT1, U1, P0, CT0, U0])
      (cp: P2 => P1, mc: CT1[P1, U1] => CT2[P1, U1], mu: U1 => U2, pf: Profunctor[CT2])
      : ComponentWithRoot[P2, R, CT2, U2, P0, CT0, U0] =
    new ComponentWithRoot[P2, R, CT2, U2, P0, CT0, U0] {
      override def root = from.root
      override val raw = from.raw
      override val ctor = mc(from.ctor).dimap(cp, mu)
      override implicit def ctorPF = pf
      override def mapRaw(f: Raw => Raw) = mappedC(from.mapRaw(f))(cp, mc, mu, pf)
      override def cmapCtorProps[P3](f: P3 => P2) = mappedC(from)(cp compose f, mc, mu, pf)
      override def mapUnmounted[U3](f: U2 => U3) = mappedC(from)(cp, mc, f compose mu, pf)
      override def mapCtorType[CT3[-p, +u] <: CtorType[p, u]](f: CT2[P2, U2] => CT3[P2, U2])(implicit pf3: Profunctor[CT3]) =
        mappedC(this)(identityFn, f, identityFn, pf3)

      override def withRef[RR >: R](ref: Ref.Handle[RR]) =
        from.withRef(ref).mapCtorType(mc)(pf).mapUnmounted(mu).cmapCtorProps(cp)
    }


  // ===================================================================================================================

  sealed trait UnmountedSimple[P, R, M] extends Generic.UnmountedSimple[P, M] {
    override type Raw <: Raw.React.ComponentElement[_ <: js.Object]
    override final type Ref = Ref.Simple[R]
    override final def displayName = staticDisplayName

    override def mapUnmountedProps[P2](f: P => P2): UnmountedSimple[P2, R, M]
    override def mapMounted[M2](f: M => M2): UnmountedSimple[P, R, M2]

    override final def renderIntoDOM(container: Raw.ReactDOM.Container, callback: Callback = Callback.empty): Mounted = {
      val result = Raw.ReactDOM.render(raw, container, callback.toJsFn)

      // Protect against future React change.
      assert(result eq null, s"Expected rendered $displayName to return null; not $result")

      mountRaw(result)
    }
  }

  sealed trait UnmountedWithRoot[P1, R, M1, P0 <: js.Object]
    extends UnmountedSimple[P1, R, M1] with Generic.UnmountedWithRoot[P1, M1, P0, Mounted] {
    override final type Raw = Raw.React.ComponentElement[P0]
    override final type Root = UnmountedRoot[P0, R]
    override def mapUnmountedProps[P2](f: P1 => P2): UnmountedWithRoot[P2, R, M1, P0]
    override def mapMounted[M2](f: M1 => M2): UnmountedWithRoot[P1, R, M2, P0]
  }

  type UnmountedRoot[P <: js.Object, R] = UnmountedWithRoot[P, R, Mounted, P]

  private val constUnit: Any => Unit = _ => ()

  def unmountedRoot[P <: js.Object, R](r: Raw.React.ComponentElement[P]): UnmountedRoot[P, R] =
    new UnmountedRoot[P, R] {
      override def mapUnmountedProps[P2](f: P => P2) = mappedU(this)(f, identityFn)
      override def mapMounted[M2](f: Mounted => M2) = mappedU(this)(identityFn, f)

      override def root          = this
      override val raw           = r
      override val mountRaw      = constUnit
      override def key           = jsNullToOption(raw.key)
      override def ref           = jsNullToOption(raw.ref).map(r => Ref.fromJs(r.asInstanceOf[Raw.React.RefHandle[R]]))
      override def props         = raw.props.asInstanceOf[P]
      override def propsChildren = PropsChildren.fromRawProps(raw.props)
    }

  private def mappedU[P2, R, M2, P1, M1, P0 <: js.Object](from: UnmountedWithRoot[P1, R, M1, P0])
                                                         (mp: P1 => P2, mm: M1 => M2): UnmountedWithRoot[P2, R, M2, P0] =
    new UnmountedWithRoot[P2, R, M2, P0] {
      override def root          = from.root
      override val raw           = from.raw
      override val mountRaw      = mm compose from.mountRaw
      override def key           = from.key
      override def ref           = from.ref
      override def props         = mp(from.props)
      override def propsChildren = from.propsChildren
      override def mapUnmountedProps[P3](f: P2 => P3) = mappedU(from)(f compose mp, mm)
      override def mapMounted[M3](f: M2 => M3) = mappedU(from)(mp, f compose mm)
    }

}