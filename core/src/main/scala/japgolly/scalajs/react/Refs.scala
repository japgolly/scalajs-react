package japgolly.scalajs.react

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import CompScope._

object Ref {
  @inline implicit def autoRefAsRefParam(r: Ref): String = r.name
  @inline implicit def autoRefAsRefParamU(r: Ref): UndefOr[String] = r.name

  def apply[N <: TopNode](name: String): RefSimple[N] =
    new RefSimple[N](name)

  def param[I, N <: TopNode](f: I => String): RefParam[I, RefSimple[N]] =
    new RefParam(i => Ref[N](f(i)))

  /** A reference to a Scala component. */
  def to[P, S, B, N <: TopNode](types: ReactComponentTypeAux[P, S, B, N], name: String): RefComp[P, S, B, N] =
    new RefComp[P, S, B, N](name)

  /** A reference to a pure JS component that has its own facade type. */
  def toJS[M <: js.Object](name: String) = new RefJSComp[M](name)
}


/**
 * A named reference to an element in a React VDOM.
 */
abstract class Ref(final val name: String) {
  type R
  protected def resolve(r: RefsObject): UndefOr[R]
  @inline final def apply(c: ReactComponentM_[_]): UndefOr[R] = apply(c.refs)
  @inline final def apply(s: Mounted[_]         ): UndefOr[R] = apply(s.refs)
  @inline final def apply(r: RefsObject         ): UndefOr[R] = resolve(r)
}


final class RefSimple[N <: TopNode](_name: String) extends Ref(_name) {
  override type R = ReactComponentM_[N]
  protected override def resolve(r: RefsObject) = r[N](name)
}


final class RefComp[P, S, B, N <: TopNode](_name: String) extends Ref(_name) {
  override type R = ReactComponentM[P, S, B, N]
  protected override def resolve(r: RefsObject) = r[N](name).asInstanceOf[UndefOr[ReactComponentM[P, S, B, N]]]
}


final class RefParam[I, RefType <: Ref](f: I => RefType) {
  @inline def apply(i: I): RefType = f(i)
  @inline def get[S](s: HasState[S] with Mounted[_])(implicit ev: S =:= I) =
    apply(ev(s._state.v))(s) // TODO _state
}


final class RefJSComp[M <: js.Object](_name: String) extends Ref(_name) {
  override type R = M
  protected override def resolve(r: RefsObject) = r[TopNode](name).asInstanceOf[UndefOr[M]]
}
