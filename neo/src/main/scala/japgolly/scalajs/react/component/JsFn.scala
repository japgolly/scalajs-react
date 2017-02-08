package japgolly.scalajs.react.component

import scalajs.js
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.{Callback, ChildrenArg, CtorType, Key, PropsChildren, vdom, raw => Raw}


object JsFn {

  type Component[P <: js.Object, CT[-p, +u] <: CtorType[p, u]] = UnderlyingComponent[P, CT, Unmounted[P]]
  type Unmounted[P <: js.Object]                               = UnderlyingUnmounted[P]
  type Mounted                                                 = Unit

  type UnderlyingComponent[P <: js.Object, CT[-p, +u] <: CtorType[p, u], U] = Component0[P, CT, U, P, CT, U]
  type UnderlyingUnmounted[P <: js.Object]                                  = Unmounted0[P, Mounted, P]

  // ===================================================================================================================

  def apply[P <: js.Object, C <: ChildrenArg](rc: Raw.ReactFunctionalComponent)
                                             (implicit s: CtorType.Summoner[P, C]): Component[P, s.CT] =
    underlyingComponent[P, s.CT, Unmounted[P]](rc, s.pf.rmap(s.summon(rc))(underlyingUnmounted))(s.pf)

  def apply[P <: js.Object, C <: ChildrenArg](name: String)
                                             (implicit s: CtorType.Summoner[P, C]): Component[P, s.CT] =
    apply[P, C](js.Dynamic.global.selectDynamic(name).asInstanceOf[Raw.ReactFunctionalComponent])(s)

  // ===================================================================================================================

  // TODO This section is a huge copy-and-paste
  // maybe use a type family & two instances

  // Difference between this and its Generic counterpart:
  // - P0 has an upper bound of js.Object.
  // - .raw
  sealed trait Component0[
      P1, CT1[-p, +u] <: CtorType[p, u], U1,
      P0 <: js.Object, CT0[-p, +u] <: CtorType[p, u], U0]
      extends Generic.Component0[P1, CT1, U1, P0, CT0, U0] {

    override final type Underlying = UnderlyingComponent[P0, CT0, U0]
    override def cmapCtorProps[P2](f: P2 => P1): Component0[P2, CT1, U1, P0, CT0, U0]
    override def mapUnmounted[U2](f: U1 => U2): Component0[P1, CT1, U2, P0, CT0, U0]
    override def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT1[P1, U1] => CT2[P1, U1])(implicit pf: Profunctor[CT2]): Component0[P1, CT2, U1, P0, CT0, U0]

    val raw: Raw.ReactFunctionalComponent
  }

  def underlyingComponent[P <: js.Object, CT[-p, +u] <: CtorType[p, u], U](rc: Raw.ReactFunctionalComponent, c: CT[P, U])
                                                                          (implicit pf: Profunctor[CT]): UnderlyingComponent[P, CT, U] =
    new UnderlyingComponent[P, CT, U] {
      override def underlying = this
      override val raw = rc
      override val ctor = c
      override implicit def ctorPF = pf

      override def cmapCtorProps[P2](f: P2 => P) = mappedC(this)(f, identity, identity, pf)
      override def mapUnmounted[U2](f: U => U2) = mappedC(this)(identity, identity, f, pf)
      override def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT[P, U] => CT2[P, U])(implicit pf: Profunctor[CT2]) =
        mappedC(this)(identity, f, identity, pf)
    }

  private def mappedC[
      P2, CT2[-p, +u] <: CtorType[p, u], U2,
      P1, CT1[-p, +u] <: CtorType[p, u], U1,
      P0 <: js.Object, CT0[-p, +u] <: CtorType[p, u], U0]
      (from: Component0[P1, CT1, U1, P0, CT0, U0])
      (cp: P2 => P1, mc: CT1[P1, U1] => CT2[P1, U1], mu: U1 => U2, pf: Profunctor[CT2])
      : Component0[P2, CT2, U2, P0, CT0, U0] =
    new Component0[P2, CT2, U2, P0, CT0, U0] {
      override def underlying = from.underlying

      override val raw = from.raw

      override implicit def ctorPF = pf

      override val ctor = mc(from.ctor).dimap(cp, mu)

      override def cmapCtorProps[P3](f: P3 => P2) = mappedC(from)(cp compose f, mc, mu, pf)
      override def mapUnmounted[U3](f: U2 => U3) = mappedC(from)(cp, mc, f compose mu, pf)
      override def mapCtorType[CT3[-p, +u] <: CtorType[p, u]](f: CT2[P2, U2] => CT3[P2, U2])(implicit pf3: Profunctor[CT3]) =
        mappedC(this)(identity, f, identity, pf3)
    }

  // ===================================================================================================================

  sealed trait Unmounted0[P1, M1, P0 <: js.Object] extends Generic.Unmounted0[P1, M1, P0, Mounted] {
    override final type Underlying = UnderlyingUnmounted[P0]
    override def mapUnmountedProps[P2](f: P1 => P2): Unmounted0[P2, M1, P0]
    override def mapMounted[M2](f: M1 => M2): Unmounted0[P1, M2, P0]

    val raw: Raw.ReactComponentElement
  }

  def underlyingUnmounted[P <: js.Object](r: Raw.ReactComponentElement): UnderlyingUnmounted[P] =
    new UnderlyingUnmounted[P] {

      override def underlying = this
      override def mapUnmountedProps[P2](f: P => P2) = mappedU(this)(f, identity)
      override def mapMounted[M2](f: Mounted => M2) = mappedU(this)(identity, f)

      override val raw = r

      override val reactElement =
        vdom.ReactElement(raw)

      override def key: Option[Key] =
        jsNullToOption(raw.key)

      override def ref: Option[String] =
        // orNullToOption(raw.ref)
        None

      override def props: P =
        raw.props.asInstanceOf[P]

      override def propsChildren: PropsChildren =
        PropsChildren(raw.props.children)

      override def renderIntoDOM(container: Raw.ReactDOM.Container, callback: Callback = Callback.empty): Mounted = {
        val result = Raw.ReactDOM.render(raw, container, callback.toJsFn)

        // Protect against future React change.
        assert(result eq null, "Expected rendered functional component to return null; not " + result)
      }
    }

  private def mappedU[P2, M2, P1, M1, P0 <: js.Object](from: Unmounted0[P1, M1, P0])
                                                      (mp: P1 => P2, mm: M1 => M2): Unmounted0[P2, M2, P0] =
    new Unmounted0[P2, M2, P0] {
      override def underlying    = from.underlying
      override val raw           = from.raw
      override def reactElement  = from.reactElement
      override def key           = from.key
      override def ref           = from.ref
      override def props         = mp(from.props)
      override def propsChildren = from.propsChildren
      override def mapUnmountedProps[P3](f: P2 => P3) = mappedU(from)(f compose mp, mm)
      override def mapMounted[M3](f: M2 => M3) = mappedU(from)(mp, f compose mm)
      override def renderIntoDOM(container: Raw.ReactDOM.Container, callback: Callback = Callback.empty) =
        mm(from.renderIntoDOM(container, callback))
    }
}
