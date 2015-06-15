package japgolly.scalajs.react

import scala.scalajs.js.{Any => JAny, Dynamic, UndefOr}

/**
 * Component constructor.
 */
sealed trait ReactComponentC[P, S, +B, +N <: TopNode] extends ReactComponentTypeAux[P, S, B, N] {
  val jsCtor: ReactComponentCU[P,S,B,N]
}

object ReactComponentC {

  sealed abstract class BaseCtor[P, S, +B, +N <: TopNode] extends ReactComponentC[P, S, B, N] {

    // "Your scientists were so preoccupied with whether or not they could that they didn't stop to think if they should."
    type This[+B, +N <: TopNode] <: BaseCtor[P, S, B, N]

    val jsCtor: ReactComponentCU[P, S, B, N]

    protected val key: UndefOr[JAny]
    protected val ref: UndefOr[String]

    def set(key: UndefOr[JAny] = this.key, ref: UndefOr[String] = this.ref): This[B, N]

    final def withKey(k: JAny)  : This[B,N] = set(key = k)
    final def withRef(r: String): This[B,N] = set(ref = r)

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
  final class ReqProps[P, S, +B, +N <: TopNode](override val jsCtor: ReactComponentCU[P, S, B, N],
                                                override protected val key: UndefOr[JAny],
                                                override protected val ref: UndefOr[String]) extends BaseCtor[P, S, B, N] {
    override type This[+B, +N <: TopNode] = ReqProps[P, S, B, N]
    def set(key: UndefOr[JAny] = this.key, ref: UndefOr[String] = this.ref): This[B, N] =
      new ReqProps(jsCtor, key, ref)

    def apply(props: P, children: ReactNode*) = jsCtor(mkProps(props), children: _*)

    def withProps       (p: => P) = new ConstProps  (jsCtor, key, ref, () => p)
    def withDefaultProps(p: => P) = new DefaultProps(jsCtor, key, ref, () => p)
  }

  /**
   * Constructor in which props can be provided or omitted.
   */
  final class DefaultProps[P, S, +B, +N <: TopNode](override val jsCtor: ReactComponentCU[P, S, B, N],
                                                    override protected val key: UndefOr[JAny],
                                                    override protected val ref: UndefOr[String],
                                                    default: () => P) extends BaseCtor[P, S, B, N] {
    override type This[+B, +N <: TopNode] = DefaultProps[P, S, B, N]
    def set(key: UndefOr[JAny] = this.key, ref: UndefOr[String] = this.ref): This[B, N] =
      new DefaultProps(jsCtor, key, ref, default)

    def apply(props: Option[P], children: ReactNode*): ReactComponentU[P,S,B,N] =
      jsCtor(mkProps(props getOrElse default()), children: _*)

    def apply(children: ReactNode*): ReactComponentU[P,S,B,N] =
      apply(None, children: _*)
  }

  /**
   * Constructor that doesn't require props to be provided.
   */
  final class ConstProps[P, S, +B, +N <: TopNode](override val jsCtor: ReactComponentCU[P, S, B, N],
                                                  override protected val key: UndefOr[JAny],
                                                  override protected val ref: UndefOr[String],
                                                  props: () => P) extends BaseCtor[P, S, B, N] {
    override type This[+B, +N <: TopNode] = ConstProps[P, S, B, N]
    def set(key: UndefOr[JAny] = this.key, ref: UndefOr[String] = this.ref): This[B, N] =
      new ConstProps(jsCtor, key, ref, props)

    def apply(children: ReactNode*) = jsCtor(mkProps(props()), children: _*)
  }
}

