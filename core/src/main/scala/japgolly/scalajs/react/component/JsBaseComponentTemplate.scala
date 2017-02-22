package japgolly.scalajs.react.component

import scala.scalajs.js
import japgolly.scalajs.react.internal._
import japgolly.scalajs.react.CtorType

trait JsBaseComponentTemplate[RawComponent <: js.Any] {

  final type RootComponent[P <: js.Object, CT[-p, +u] <: CtorType[p, u], U] =
    BaseComponent[P, CT, U, P, CT, U]

  // Difference between this and its Generic counterpart:
  // - P0 has an upper bound of js.Object.
  // - Raw type specified
  sealed trait BaseComponent[
      P1, CT1[-p, +u] <: CtorType[p, u], U1,
      P0 <: js.Object, CT0[-p, +u] <: CtorType[p, u], U0]
      extends Generic.BaseComponent[P1, CT1, U1, P0, CT0, U0] {

    override final type Root = RootComponent[P0, CT0, U0]
    override final type Raw = RawComponent

    override def cmapCtorProps[P2](f: P2 => P1): BaseComponent[P2, CT1, U1, P0, CT0, U0]
    override def mapUnmounted[U2](f: U1 => U2): BaseComponent[P1, CT1, U2, P0, CT0, U0]
    override def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT1[P1, U1] => CT2[P1, U1])(implicit pf: Profunctor[CT2]): BaseComponent[P1, CT2, U1, P0, CT0, U0]
  }

  final def rootComponent[P <: js.Object, CT[-p, +u] <: CtorType[p, u], U](rc: RawComponent, c: CT[P, U])
                                                                          (implicit pf: Profunctor[CT]): RootComponent[P, CT, U] =
    new RootComponent[P, CT, U] {
      override def root = this
      override val raw = rc
      override val ctor = c
      override implicit def ctorPF = pf
      override def cmapCtorProps[P2](f: P2 => P) = mappedC(this)(f, identity, identity, pf)
      override def mapUnmounted[U2](f: U => U2) = mappedC(this)(identity, identity, f, pf)
      override def mapCtorType[CT2[-p, +u] <: CtorType[p, u]](f: CT[P, U] => CT2[P, U])(implicit pf: Profunctor[CT2]) =
        mappedC(this)(identity, f, identity, pf)
    }

  protected final def mappedC[
      P2, CT2[-p, +u] <: CtorType[p, u], U2,
      P1, CT1[-p, +u] <: CtorType[p, u], U1,
      P0 <: js.Object, CT0[-p, +u] <: CtorType[p, u], U0]
      (from: BaseComponent[P1, CT1, U1, P0, CT0, U0])
      (cp: P2 => P1, mc: CT1[P1, U1] => CT2[P1, U1], mu: U1 => U2, pf: Profunctor[CT2])
      : BaseComponent[P2, CT2, U2, P0, CT0, U0] =
    new BaseComponent[P2, CT2, U2, P0, CT0, U0] {
      override def root = from.root
      override val raw = from.raw
      override val ctor = mc(from.ctor).dimap(cp, mu)
      override implicit def ctorPF = pf
      override def cmapCtorProps[P3](f: P3 => P2) = mappedC(from)(cp compose f, mc, mu, pf)
      override def mapUnmounted[U3](f: U2 => U3) = mappedC(from)(cp, mc, f compose mu, pf)
      override def mapCtorType[CT3[-p, +u] <: CtorType[p, u]](f: CT2[P2, U2] => CT3[P2, U2])(implicit pf3: Profunctor[CT3]) =
        mappedC(this)(identity, f, identity, pf3)
    }
}
