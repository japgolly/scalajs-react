package japgolly.scalajs.react.component

import scala.scalajs.js
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.CtorType

trait JsBaseComponentTemplate[RawComponent[_ <: js.Object] <: js.Any] {

  protected def rawComponentDisplayName: RawComponent[_ <: js.Object] => String

  // Difference between this and its Generic counterpart:
  // - P0 has an upper bound of js.Object.
  // - Raw type specified

  sealed trait ComponentSimple[P, CT[-p, +u] <: CtorType[p, u], U] extends Generic.ComponentSimple[P, CT, U] {
    override final def displayName = rawComponentDisplayName(raw)

    override type Raw <: RawComponent[_ <: js.Object]
    override def mapRaw(f: Raw => Raw): ComponentSimple[P, CT, U]
    override def cmapCtorProps[P2](f: P2 => P): ComponentSimple[P2, CT, U]
    override def mapUnmounted[U2](f: U => U2): ComponentSimple[P, CT, U2]
    override def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT[P, U] => CT2[P, U])(implicit pf: Profunctor[CT2]): ComponentSimple[P, CT2, U]
  }

  sealed trait ComponentWithRoot[
      P1, CT1[-p, +u] <: CtorType[p, u], U1,
      P0 <: js.Object, CT0[-p, +u] <: CtorType[p, u], U0]
      extends ComponentSimple[P1, CT1, U1] with Generic.ComponentWithRoot[P1, CT1, U1, P0, CT0, U0] {

    override final type Raw = RawComponent[P0]
    override final type Root = ComponentRoot[P0, CT0, U0]

    override def mapRaw(f: Raw => Raw): ComponentWithRoot[P1, CT1, U1, P0, CT0, U0]
    override def cmapCtorProps[P2](f: P2 => P1): ComponentWithRoot[P2, CT1, U1, P0, CT0, U0]
    override def mapUnmounted[U2](f: U1 => U2): ComponentWithRoot[P1, CT1, U2, P0, CT0, U0]
    override def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT1[P1, U1] => CT2[P1, U1])(implicit pf: Profunctor[CT2]): ComponentWithRoot[P1, CT2, U1, P0, CT0, U0]
  }

  final type ComponentRoot[P <: js.Object, CT[-p, +u] <: CtorType[p, u], U] =
    ComponentWithRoot[P, CT, U, P, CT, U]

  final def componentRoot[P <: js.Object, CT[-p, +u] <: CtorType[p, u], U](rc: RawComponent[P], c: CT[P, U])
                                                                          (implicit pf: Profunctor[CT]): ComponentRoot[P, CT, U] =
    new ComponentRoot[P, CT, U] {
      override def root = this
      override val raw = rc
      override val ctor = c
      override implicit def ctorPF = pf
      override def mapRaw(f: Raw => Raw) = componentRoot(f(rc), c)(pf)
      override def cmapCtorProps[P2](f: P2 => P) = mappedC(this)(f, identityFn, identityFn, pf)
      override def mapUnmounted[U2](f: U => U2) = mappedC(this)(identityFn, identityFn, f, pf)
      override def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT[P, U] => CT2[P, U])(implicit pf: Profunctor[CT2]) =
        mappedC(this)(identityFn, f, identityFn, pf)
    }

  protected final def mappedC[
      P2, CT2[-p, +u] <: CtorType[p, u], U2,
      P1, CT1[-p, +u] <: CtorType[p, u], U1,
      P0 <: js.Object, CT0[-p, +u] <: CtorType[p, u], U0]
      (from: ComponentWithRoot[P1, CT1, U1, P0, CT0, U0])
      (cp: P2 => P1, mc: CT1[P1, U1] => CT2[P1, U1], mu: U1 => U2, pf: Profunctor[CT2])
      : ComponentWithRoot[P2, CT2, U2, P0, CT0, U0] =
    new ComponentWithRoot[P2, CT2, U2, P0, CT0, U0] {
      override def root = from.root
      override val raw = from.raw
      override val ctor = mc(from.ctor).dimap(cp, mu)
      override implicit def ctorPF = pf
      override def mapRaw(f: Raw => Raw) = mappedC(from.mapRaw(f))(cp, mc, mu, pf)
      override def cmapCtorProps[P3](f: P3 => P2) = mappedC(from)(cp compose f, mc, mu, pf)
      override def mapUnmounted[U3](f: U2 => U3) = mappedC(from)(cp, mc, f compose mu, pf)
      override def mapCtorType[CT3[-p, +u] <: CtorType[p, u]](f: CT2[P2, U2] => CT3[P2, U2])(implicit pf3: Profunctor[CT3]) =
        mappedC(this)(identityFn, f, identityFn, pf3)
    }
}
