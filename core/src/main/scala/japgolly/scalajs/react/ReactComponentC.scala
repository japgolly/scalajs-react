package japgolly.scalajs.react

import scala.scalajs.js.{Any => JAny, Dynamic, UndefOr}

/**
 * Component constructor.
 */
sealed trait ReactComponentC[P, S, B, N <: TopNode] extends ReactComponentTypeAux[P, S, B, N] {
  final type Props     = P
  final type State     = S
  final type Backend   = B
  final type DomType   = N
  final type Mounted   = ReactComponentM[P, S, B, N]
  final type Unmounted = ReactComponentU[P, S, B, N]

  /**
   * Output of [[React.createClass()]].
   */
  val reactClass: ReactClass[P, S, B, N]

  /**
   * Output of [[React.createFactory()]].
   */
  val factory: ReactComponentCU[P, S, B, N]

  def displayName = reactClass.displayName
}

object ReactComponentC {

  sealed abstract class BaseCtor[P, S, B, N <: TopNode] extends ReactComponentC[P, S, B, N] {

    type This <: BaseCtor[P, S, B, N]

    protected val key: UndefOr[JAny]
    protected val ref: UndefOr[String]

    def set(key: UndefOr[JAny] = this.key, ref: UndefOr[String] = this.ref): This

    final def withKey(k: JAny)  : This = set(key = k)
    final def withRef(r: String): This = set(ref = r)

    protected def mkProps(props: P): WrapObj[P] = {
      val j = WrapObj(props)
      key.foreach(k => j.asInstanceOf[Dynamic].updateDynamic("key")(k))
      ref.foreach(r => j.asInstanceOf[Dynamic].updateDynamic("ref")(r))
      j
    }
  }

  /**
   * Constructor that requires props to be provided.
   */
  final class ReqProps[P, S, B, N <: TopNode](override val factory: ReactComponentCU[P, S, B, N],
                                              override val reactClass: ReactClass[P, S, B, N],
                                              override protected val key: UndefOr[JAny],
                                              override protected val ref: UndefOr[String]) extends BaseCtor[P, S, B, N] {
    override type This = ReqProps[P, S, B, N]
    def set(key: UndefOr[JAny] = this.key, ref: UndefOr[String] = this.ref): This =
      new ReqProps(factory, reactClass, key, ref)

    def apply(props: P, children: ReactNode*) = factory(mkProps(props), children: _*)

    def withProps       (p: => P) = new ConstProps  (factory, reactClass, key, ref, () => p)
    def withDefaultProps(p: => P) = new DefaultProps(factory, reactClass, key, ref, () => p)

    def noProps(implicit ev: UnitPropProof[P]): ConstProps[P, S, B, N] =
      new ConstProps(factory, reactClass, key, ref, fnUnit0)
  }

  type UnitPropProof[P] = (() => Unit) =:= (() => P)
  private[this] val fnUnit0 = () => ()

  /**
   * Constructor in which props can be provided or omitted.
   */
  final class DefaultProps[P, S, B, N <: TopNode](override val factory: ReactComponentCU[P, S, B, N],
                                                  override val reactClass: ReactClass[P, S, B, N],
                                                  override protected val key: UndefOr[JAny],
                                                  override protected val ref: UndefOr[String],
                                                  default: () => P) extends BaseCtor[P, S, B, N] {
    override type This = DefaultProps[P, S, B, N]
    def set(key: UndefOr[JAny] = this.key, ref: UndefOr[String] = this.ref): This =
      new DefaultProps(factory, reactClass, key, ref, default)

    def apply(props: Option[P], children: ReactNode*): ReactComponentU[P,S,B,N] =
      factory(mkProps(props getOrElse default()), children: _*)

    def apply(children: ReactNode*): ReactComponentU[P,S,B,N] =
      apply(None, children: _*)
  }

  /**
   * Constructor that doesn't require props to be provided.
   */
  final class ConstProps[P, S, B, N <: TopNode](override val factory: ReactComponentCU[P, S, B, N],
                                                override val reactClass: ReactClass[P, S, B, N],
                                                override protected val key: UndefOr[JAny],
                                                override protected val ref: UndefOr[String],
                                                props: () => P) extends BaseCtor[P, S, B, N] {
    override type This = ConstProps[P, S, B, N]
    def set(key: UndefOr[JAny] = this.key, ref: UndefOr[String] = this.ref): This =
      new ConstProps(factory, reactClass, key, ref, props)

    def apply(children: ReactNode*) = factory(mkProps(props()), children: _*)
  }
}

