package experiment

import scala.language.reflectiveCalls
import TestUtil._
import scalajs.js

/*
GenC:
  apply :: P -> U

GenU
  props :: P
  mount :: M

GenM
  props :: P

*/

object MainExperiment {

  trait GenC0[P, U, P0, U0] {
    final type Unmounted = U
    def apply(p: P): U
    def mapP[X](f: X => P): GenC0[X, U, P0, U0]
    def mapU[X](f: U => X): GenC0[P, X, P0, U0]
  }

  trait GenU0[P, M, P0, M0] {
    def props: P
    def mount: M
    def mapP[X](f: P => X): GenU0[X, M, P0, M0]
    def mapM[X](f: M => X): GenU0[P, X, P0, M0]
  }

  trait GenM0[P, P0] {
    def props: P
    def mapP[X](f: P => X): GenM0[X, P0]
  }

  type GenC[P, U] = GenC0[P, U, P, U]
  type GenU[P, M] = GenU0[P, M, P, M]
  type GenM[P] = GenM0[P, P]

  // ==========================================================================

  sealed trait JsC0[P, U, P0 <: js.Object, U0] extends GenC0[P, U, P0, U0] {
    override def mapP[X](f: X => P): JsC0[X, U, P0, U0]
    override def mapU[X](f: U => X): JsC0[P, X, P0, U0]
  }

  sealed trait JsU0[P, M, P0 <: js.Object, M0] extends GenU0[P, M, P0, M0] {
    override def mapP[X](f: P => X): JsU0[X, M, P0, M0]
    override def mapM[X](f: M => X): JsU0[P, X, P0, M0]
  }

  sealed trait RawReactComponent

  sealed trait JsM0[P, R <: RawReactComponent, P0 <: js.Object] extends GenM0[P, P0] {
    override def mapP[X](f: P => X): JsM0[X, R, P0]
    val raw: R
    def addRawType[T] = this.asInstanceOf[JsM0[P, R with T, P0]]
  }

  type JsUnmappedC[P <: js.Object, U] = JsC0[P, U, P, U]
  type JsUnmappedU[P <: js.Object, M] = JsU0[P, M, P, M]
  type JsUnmappedM[P <: js.Object, R <: RawReactComponent] = JsM0[P, R, P]

  type JsC[P <: js.Object, R <: RawReactComponent] = JsUnmappedC[P, JsU[P, R]]
  type JsU[P <: js.Object, R <: RawReactComponent] = JsUnmappedU[P, JsM[P, R]]
  type JsM[P <: js.Object, R <: RawReactComponent] = JsUnmappedM[P, R]

  private def mapJsC[P2, U2, P1, U1, P0 <: js.Object, U0](from: JsC0[P1, U1, P0, U0])(cp: P2 => P1, mu: U1 => U2): JsC0[P2, U2, P0, U0] =
    new JsC0[P2, U2, P0, U0] {
      override def apply(p: P2)        = mu(from(cp(p)))
      override def mapP[X](f: X => P2) = mapJsC(from)(cp compose f, mu)
      override def mapU[X](f: U2 => X) = mapJsC(from)(cp, f compose mu)
    }

  private def mapJsU[P2, M2, P1, M1, P0 <: js.Object, M0](from: JsU0[P1, M1, P0, M0])(mp: P1 => P2, mm: M1 => M2): JsU0[P2, M2, P0, M0] =
    new JsU0[P2, M2, P0, M0] {
      override def props = mp(from.props)
      override def mount = mm(from.mount)
      override def mapP[X](f: P2 => X) = mapJsU(from)(f compose mp, mm)
      override def mapM[X](f: M2 => X) = mapJsU(from)(mp, f compose mm)
    }

  private def mapJsM[P2, R <: RawReactComponent, P1, P0 <: js.Object](from: JsM0[P1, R, P0])(mp: P1 => P2): JsM0[P2, R, P0] =
    new JsM0[P2, R, P0] {
      override val raw = from.raw
      override def props = mp(from.props)
      override def mapP[X](f: P2 => X) = mapJsM(from)(f compose mp)
    }

  def jsComponent[P <: js.Object](name: String): JsC[P, RawReactComponent] = {
    type R = RawReactComponent

    def jsC(rawC: js.Dynamic): JsC[P, R] =
      new JsC[P, R] {
        def apply(p: P)                = jsU(rawC(p))
        def mapP[X](f: X => P)         = mapJsC(this)(f, identity)
        def mapU[X](f: Unmounted => X) = mapJsC(this)(identity, f)
      }

    def jsU(rawU: js.Dynamic): JsU[P, R] =
      new JsU[P, R] {
        override def mount                      = jsM(rawU.mount.asInstanceOf[RawReactComponent])
        override def props                      = rawU.asInstanceOf[P]
        override def mapP[X](f: P => X)         = mapJsU(this)(f, identity)
        override def mapM[X](f: JsM[P, R] => X) = mapJsU(this)(identity, f)
      }

    def jsM(rawM: RawReactComponent): JsM[P, R] =
      new JsM[P, R] {
        override val raw                = rawM
        override def props              = raw.asInstanceOf[js.Dynamic].props.asInstanceOf[P]
        override def mapP[X](f: P => X) = mapJsM(this)(f)
      }

    jsC(js.Dynamic.global.selectDynamic(name))
  }

  // ==========================================================================

}
