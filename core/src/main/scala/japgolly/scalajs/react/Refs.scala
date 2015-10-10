package japgolly.scalajs.react

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import CompScope._

object Ref {
  @inline implicit def autoRefAsRefParam(r: Ref): String = r.name
  @inline implicit def autoRefAsRefParamU(r: Ref): UndefOr[String] = r.name

  /**
   * A reference to a plain DOM element like `div`, `input`, etc.
   *
   * For components use [[to()]].
   *
   * @param name Some arbitrary string that doesn't conflict with any other refs using in the same render call.
   * @tparam N The target ref type.
   */
  def apply[N <: TopNode](name: String): RefSimple[N] =
    new RefSimple[N](name)

  /**
   * Parameterised references to plain DOM elements like `div`, `input`, etc.
   *
   * For components use [[to()]].
   *
   * @param getName Return a reference name for a given input.
   * @tparam I Any input type that you want to use to key/index refs.
   * @tparam N The target ref type.
   */
  def param[I, N <: TopNode](getName: I => String): RefParam[I, RefSimple[N]] =
    new RefParam(i => Ref[N](getName(i)))

  /**
   * A reference to Scala component.
   *
   * For plain DOM elements like `div`, `input`, etc., use [[apply()]].
   *
   * @param name Some arbitrary string that doesn't conflict with any other refs using in the same render call.
   * @tparam N The target ref type.
   */
  def to[P, S, B, N <: TopNode](`type`: ReactComponentTypeAux[P, S, B, N], name: String): RefComp[P, S, B, N] =
    new RefComp[P, S, B, N](name)

  /** A reference to a pure JS component that has its own facade type. */
  def toJS[M <: js.Object](name: String) = new RefJSComp[M](name)
}


/**
 * A named reference to an element in a React VDOM.
 */
abstract class Ref(final val name: String) {
  /** The type being referred to. */
  type Target
  protected def resolve(r: RefsObject): UndefOr[Target]
  @inline final def apply(c: ReactComponentM_[_]): UndefOr[Target] = apply(c.refs)
  @inline final def apply(s: Mounted[_]         ): UndefOr[Target] = apply(s.refs)
  @inline final def apply(r: RefsObject         ): UndefOr[Target] = resolve(r)
}


final class RefSimple[N <: TopNode](_name: String) extends Ref(_name) {
  override type Target = N
  protected override def resolve(r: RefsObject) =
    // New behaviour as of React 0.14. Confirmed by RefTest.
    r[N](name).asInstanceOf[UndefOr[N]]
}


final class RefComp[P, S, B, N <: TopNode](_name: String) extends Ref(_name) {
  override type Target = ReactComponentM[P, S, B, N]
  protected override def resolve(r: RefsObject) = r[N](name).asInstanceOf[UndefOr[ReactComponentM[P, S, B, N]]]
}


final class RefParam[I, RefType <: Ref](f: I => RefType) {
  @inline def apply(i: I): RefType = f(i)
  @inline def get[S](s: HasState[S] with Mounted[_])(implicit ev: S =:= I) =
    apply(ev(s._state.v))(s) // TODO _state
}


final class RefJSComp[M <: js.Object](_name: String) extends Ref(_name) {
  override type Target = M
  protected override def resolve(r: RefsObject) = r[TopNode](name).asInstanceOf[UndefOr[M]]
}
