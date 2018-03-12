package japgolly.scalajs.react

import japgolly.scalajs.react.internal.jsNullToOption
import scala.scalajs.js
import scala.scalajs.js.|

trait Ref[I, O] { self =>

  val get: CallbackOption[O]
  val set: CallbackKleisli[Option[I], Unit]

  final lazy val rawSetFn: raw.React.RefFn[I] =
    set.contramap[I | Null](jsNullToOption).toJsFn

  def contramap[A](f: A => I): Ref[A, O] =
    new Ref[A, O] {
      override val get = self.get
      override val set = self.set.contramap[Option[A]](_ map f)
    }

  def map[A](f: O => A): Ref[I, A] =
    new Ref[I, A] {
      override val get = self.get.map(f)
      override val set = self.set
    }

  def widen[A >: O]: Ref[I, A] =
    this.asInstanceOf[Ref[I, A]] // map[A](o => o)

  def narrow[A <: I]: Ref[A, O] =
    this.asInstanceOf[Ref[A, O]] // contramap[A](a => a)

  final def foreach(f: O => Unit): Callback =
    foreachCB(a => Callback(f(a)))

  final def foreachCB(f: O => Callback): Callback =
    get.flatMapCB(f).toCallback

  /** Get the reference immediately.
    *
    * ONLY USE THIS IN UNIT TESTS. DO NOT USE THIS IN PRODUCTION CODE.
    *
    * Unsafe for two reasons:
    *
    * 1. It reads an underlying variable. (impurity)
    * 2. It throws an exception when the ref is empty (partiality)
    */
  final def unsafeGet(): O =
    get.asCallback.runNow().getOrElse(sys error "Reference is empty")
}

object Ref {

  def apply[A]: Ref[A, A] =
    new Ref[A, A] {
      private[this] var ref = Option.empty[A]
      override val get = CallbackOption(CallbackTo(ref))
      override val set = CallbackKleisli((r: Option[A]) => Callback{ref = r})
    }

  // ===================================================================================================================

  trait ToComponent[I, O, C] extends Ref[I, O] {
    val component: C

    override def contramap[A](f: A => I): Ref.ToComponent[A, O, C] =
      ToComponent(super.contramap(f), component)

    override def map[A](f: O => A): Ref.ToComponent[I, A, C] =
      ToComponent(super.map(f), component)

    override def widen[A >: O]: Ref.ToComponent[I, A, C] =
      this.asInstanceOf[ToComponent[I, A, C]] // map[A](o => o)

    override def narrow[A <: I]: Ref.ToComponent[A, O, C] =
      this.asInstanceOf[ToComponent[A, O, C]] // contramap[A](a => a)
  }

  object ToComponent {

    def apply[I, O, C](ref: Ref[I, O], c: C): ToComponent[I, O, C] =
      new ToComponent[I, O, C] {
        override val get = ref.get
        override val set = ref.set
        override val component = c
      }

    def inject[I, O, CT[-p, +u] <: CtorType[p, u], P2, U2](c: CT[P2, U2], ref: Ref[I, O]): ToComponent[I, O, CT[P2, U2]] =
      apply(ref, CtorType.hackBackToSelf(c)(c.withRawProp("ref", ref.rawSetFn)))
  }

  // ===================================================================================================================

  type ToJsComponent[F[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object] =
    Ref.ToComponent[
      JsComponent.RawMounted[P0, S0] with R,
      JsComponent.MountedWithRawType[P0, S0, R],
      CT1[P1, JsComponent.UnmountedMapped[F, P1, S1, R, P0, S0]]]

  def toJsComponent[F[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]]
                   (c: JsComponent.ComponentMapped[F, P1, S1, CT1, R, P0, S0, CT0])
                   : ToJsComponent[F, P1, S1, CT1, R, P0, S0] =
    ToComponent.inject(c,
      apply[JsComponent.RawMounted[P0, S0] with R].map(JsComponent.mounted[P0, S0](_).withRawType[R]))

  // ===================================================================================================================

  type ToScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]] =
    Ref.ToComponent[
      ScalaComponent.RawMounted[P, S, B],
      ScalaComponent.MountedImpure[P, S, B],
      CT[P, ScalaComponent.Unmounted[P, S, B]]]

  def toScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]]
                      (c: ScalaComponent[P, S, B, CT])
                      : ToScalaComponent[P, S, B, CT] =
    ToComponent.inject(c,
      apply[ScalaComponent.RawMounted[P, S, B]].map(_.mountedImpure))

}