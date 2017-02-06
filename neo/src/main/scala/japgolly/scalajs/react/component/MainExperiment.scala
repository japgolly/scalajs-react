package experiment // TODO DELETE

import scala.language.reflectiveCalls
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
    def underlying: GenC[P0, U0]
    def apply(p: P): U
    def mapP[X](f: X => P): GenC0[X, U, P0, U0]
    def mapU[X](f: U => X): GenC0[P, X, P0, U0]
  }

  trait GenU0[P, M, P0, M0] {
    def underlying: GenU[P0, M0]
    def props: P
    def mount: M
    def mapP[X](f: P => X): GenU0[X, M, P0, M0]
    def mapM[X](f: M => X): GenU0[P, X, P0, M0]
  }

  trait GenM0[P, P0] {
    def underlying: GenM[P0]
    def props: P
    def mapP[X](f: P => X): GenM0[X, P0]
  }

  type GenC[P, U] = GenC0[P, U, P, U]
  type GenU[P, M] = GenU0[P, M, P, M]
  type GenM[P] = GenM0[P, P]

  // ==========================================================================

  sealed trait JsC0[P, U, P0 <: js.Object, U0] extends GenC0[P, U, P0, U0] {
    override def underlying: JsUnmappedC[P0, U0]
    override def mapP[X](f: X => P): JsC0[X, U, P0, U0]
    override def mapU[X](f: U => X): JsC0[P, X, P0, U0]
  }

  sealed trait JsU0[P, M, P0 <: js.Object, M0] extends GenU0[P, M, P0, M0] {
    override def underlying: JsUnmappedU[P0, M0]
    override def mapP[X](f: P => X): JsU0[X, M, P0, M0]
    override def mapM[X](f: M => X): JsU0[P, X, P0, M0]

    // useless due to R needing spec
//    final def addRawType[R <: RawReactComponent, T](implicit ev: M <:< JsM0[P, R, P0]): JsU0[P, JsM0[P, R with T, P0], P0, M0] =
//      mapM(ev(_).addRawType[T])
  }

  sealed trait RawReactComponent

  sealed trait JsM0[P, R <: RawReactComponent, P0 <: js.Object] extends GenM0[P, P0] {
    override def underlying: JsM[P0, RawReactComponent]
    override def mapP[X](f: P => X): JsM0[X, R, P0]
    val raw: R
    final def addRawType[T]: JsM0[P, R with T, P0] =
      this.asInstanceOf[JsM0[P, R with T, P0]]
  }

  type JsUnmappedC[P <: js.Object, U] = JsC0[P, U, P, U]
  type JsUnmappedU[P <: js.Object, M] = JsU0[P, M, P, M]
  type JsUnmappedM[P <: js.Object, R <: RawReactComponent] = JsM0[P, R, P]

  type JsC[P <: js.Object, R <: RawReactComponent] = JsUnmappedC[P, JsU[P, R]]
  type JsU[P <: js.Object, R <: RawReactComponent] = JsUnmappedU[P, JsM[P, R]]
  type JsM[P <: js.Object, R <: RawReactComponent] = JsUnmappedM[P, R]

  private def mapJsC[P2, U2, P1, U1, P0 <: js.Object, U0](from: JsC0[P1, U1, P0, U0])(cp: P2 => P1, mu: U1 => U2): JsC0[P2, U2, P0, U0] =
    new JsC0[P2, U2, P0, U0] {
      override def underlying          = from.underlying
      override def apply(p: P2)        = mu(from(cp(p)))
      override def mapP[X](f: X => P2) = mapJsC(from)(cp compose f, mu)
      override def mapU[X](f: U2 => X) = mapJsC(from)(cp, f compose mu)
    }

  private def mapJsU[P2, M2, P1, M1, P0 <: js.Object, M0](from: JsU0[P1, M1, P0, M0])(mp: P1 => P2, mm: M1 => M2): JsU0[P2, M2, P0, M0] =
    new JsU0[P2, M2, P0, M0] {
      override def underlying = from.underlying
      override def props = mp(from.props)
      override def mount = mm(from.mount)
      override def mapP[X](f: P2 => X) = mapJsU(from)(f compose mp, mm)
      override def mapM[X](f: M2 => X) = mapJsU(from)(mp, f compose mm)
    }

  private def mapJsM[P2, R <: RawReactComponent, P1, P0 <: js.Object](from: JsM0[P1, R, P0])(mp: P1 => P2): JsM0[P2, R, P0] =
    new JsM0[P2, R, P0] {
      override def underlying = from.underlying
      override val raw = from.raw
      override def props = mp(from.props)
      override def mapP[X](f: P2 => X) = mapJsM(from)(f compose mp)
    }

  def jsComponent[P <: js.Object](name: String): JsC[P, RawReactComponent] = {
    type R = RawReactComponent

    def jsC(rawC: js.Dynamic): JsC[P, R] =
      new JsC[P, R] {
        override def underlying        = this
        def apply(p: P)                = jsU(rawC(p))
        def mapP[X](f: X => P)         = mapJsC(this)(f, identity)
        def mapU[X](f: Unmounted => X) = mapJsC(this)(identity, f)
      }

    def jsU(rawU: js.Dynamic): JsU[P, R] =
      new JsU[P, R] {
        override def underlying                 = this
        override def mount                      = jsM(rawU.mount.asInstanceOf[RawReactComponent])
        override def props                      = rawU.asInstanceOf[P]
        override def mapP[X](f: P => X)         = mapJsU(this)(f, identity)
        override def mapM[X](f: JsM[P, R] => X) = mapJsU(this)(identity, f)
      }

    def jsM(rawM: RawReactComponent): JsM[P, R] =
      new JsM[P, R] {
        override val raw                = rawM
        override def underlying         = this
        override def props              = raw.asInstanceOf[js.Dynamic].props.asInstanceOf[P]
        override def mapP[X](f: P => X) = mapJsM(this)(f)
      }

    jsC(js.Dynamic.global.selectDynamic(name))
  }

  // ==========================================================================

  /*
  implicit class JsOpsUR[P, MP, R <: RawReactComponent, P0 <: js.Object, M0](self: JsU0[P, JsM0[MP, R, P0], P0, M0]) {
    def addRawType[T]: JsU0[P, JsM0[MP, R with T, P0], P0, M0] =
      self.mapM(_.addRawType[T])
  }
  implicit class JsOpsUP[P, R <: RawReactComponent, P0 <: js.Object, M0](self: JsU0[P, JsM0[P, R, P0], P0, M0]) {
    def mapPP[X](f: P => X): JsU0[X, JsM0[X, R, P0], P0, M0] =
      self.mapP(f).mapM(_ mapP f)
  }
  */

  type JsMappedC[P, R <: RawReactComponent, P0 <: js.Object] = JsC0[P, JsMappedU[P, R, P0], P0, JsU[P0, RawReactComponent]]
  type JsMappedU[P, R <: RawReactComponent, P0 <: js.Object] = JsU0[P, JsMappedM[P, R, P0], P0, JsM[P0, RawReactComponent]]
  type JsMappedM[P, R <: RawReactComponent, P0 <: js.Object] = JsM0[P, R, P0]

  implicit class JsOpsUP[P, R <: RawReactComponent, P0 <: js.Object](self: JsMappedU[P, R, P0]) {
    def addRawType[T]: JsMappedU[P, R with T, P0] =
      self.mapM(_.addRawType[T])
    def mapPP[X](f: P => X): JsMappedU[X, R, P0] =
      self.mapP(f).mapM(_ mapP f)
  }
  implicit class JsOpsCP[P, R <: RawReactComponent, P0 <: js.Object](self: JsMappedC[P, R, P0]) {
    def addRawType[T]: JsMappedC[P, R with T, P0] =
      self.mapU(_.addRawType[T])
    def mapPP[X](f: P => X)(g: X => P): JsMappedC[X, R, P0] =
      self.mapP(g).mapU(_ mapPP f)
  }

  // ==========================================================================

  @js.native sealed trait Raw1 extends js.Object { def raw1: Int }
  @js.native sealed trait Raw2 extends js.Object { def raw2: Int }
  @js.native sealed trait JsObj extends js.Object
  case class ScalaObj(js: JsObj)
  case class ScalaObj2(s: ScalaObj)

  val t1 =
    jsComponent[JsObj]("Test")
      .mapPP(ScalaObj)(_.js)
      .addRawType[Raw1]
      .mapPP(ScalaObj2)(_.s)
      .addRawType[Raw2]
  val t2 = t1: JsMappedC[ScalaObj2, RawReactComponent with Raw1 with Raw2, JsObj]
  val t3 = t2.underlying: JsC[JsObj, RawReactComponent]
  val t4 = t3.underlying: JsC[JsObj, RawReactComponent]

  val m = t1(ScalaObj2(ScalaObj(null))).mount
  m.props.s.js
  m.raw.raw1
  m.raw.raw2
}
