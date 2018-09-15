package japgolly.scalajs.react

import japgolly.scalajs.react.{raw => Raw}
import japgolly.scalajs.react.internal.{Effect, identityFn}
import japgolly.scalajs.react.internal.JsUtil.jsNullToOption
import scala.scalajs.js
import scala.scalajs.js.|

object Ref {

  def apply[A]: Simple[A] =
    newMechanism[A]

  def fromJs[A](raw: Raw.React.RefHandle[A]): Simple[A] =
    Full(raw, identityFn, identityFn)

  def forwardedFromJs[A](f: raw.React.ForwardedRef[A]): Option[Simple[A]] =
    jsNullToOption(f).map(fromJs)

  private trait Mechanism {
    def apply[A]: Simple[A]
  }

  private[this] val newMechanism: Mechanism =
    if (js.isUndefined(Raw.React.asInstanceOf[js.Dynamic].createRef))
      // React ≤ 15
      new Mechanism {
        override def apply[A] = {
          val handle = js.Dynamic.literal("current" -> null).asInstanceOf[Raw.React.RefHandle[A]]
          fromJs(handle)
        }
      }
    else
      // React 16+
      new Mechanism {
        override def apply[A] = fromJs(Raw.React.createRef[A]())
      }

  type Simple[A] = Full[A, A, A]

  trait Handle[A] {
    val raw: Raw.React.RefHandle[A]
    final def root: Simple[A] = fromJs(raw)
  }

  trait Get[A] { self =>
    val get: CallbackOption[A]

    def map[B](f: A => B): Get[B]

    def widen[B >: A]: Get[B]

    final def foreach(f: A => Unit): Callback =
      foreachCB(a => Callback(f(a)))

    final def foreachCB(f: A => Callback): Callback =
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
    final def unsafeGet(): A =
      get.asCallback.runNow().getOrElse(sys error "Reference is empty")
  }

  trait Set[A] {
    val set: CallbackKleisli[Option[A], Unit]

    final lazy val rawSetFn: Raw.React.RefFn[A] =
      set.contramap[A | Null](jsNullToOption).toJsFn

    def contramap[B](f: B => A): Set[B]

    def narrow[B <: A]: Set[B]
  }

  trait Fn[I, O] extends Set[I] with Get[O] {
    override def contramap[X](f: X => I): Fn[X, O]
    override def narrow[X <: I]: Fn[X, O]
    override def map[X](f: O => X): Fn[I, X]
    override def widen[X >: O]: Fn[I, X]
  }

  trait Full[I, A, O] extends Handle[A] with Fn[I, O] {
    override def contramap[X](f: X => I): Full[X, A, O]
    override def narrow[X <: I]: Full[X, A, O]
    override def map[X](f: O => X): Full[I, A, X]
    override def widen[X >: O]: Full[I, A, X]
  }

  def Full[I, A, O](_raw: Raw.React.RefHandle[A], l: I => A, r: A => O): Full[I, A, O] =
    new Full[I, A, O] {

      override val raw = _raw

      override val set: CallbackKleisli[Option[I], Unit] =
        CallbackKleisli((oi: Option[I]) => Callback(raw.current = oi match {
          case Some(i) => l(i)
          case None => null
        }))

      override val get: CallbackOption[O] =
        CallbackOption(CallbackTo(jsNullToOption(raw.current).map(r)))

      override def contramap[X](f: X => I): Full[X, A, O] =
        Full(raw, l compose f, r)

      override def narrow[X <: I]: Full[X, A, O] =
        Full[X, A, O](raw, l, r)

      override def map[X](f: O => X): Full[I, A, X] =
        Full(raw, l, f compose r)

      override def widen[X >: O]: Full[I, A, X] =
        Full[I, A, X](raw, l, r)
    }

  // ===================================================================================================================

  final class ToComponent[I, R, O, C](ref: Full[I, R, O], val component: C) extends Full[I, R, O] {
    override val raw = ref.raw
    override val get = ref.get
    override val set = ref.set

    override def contramap[A](f: A => I): ToComponent[A, R, O, C] =
      ToComponent(ref.contramap(f), component)

    override def map[A](f: O => A): ToComponent[I, R, A, C] =
      ToComponent(ref.map(f), component)

    override def widen[A >: O]: ToComponent[I, R, A, C] =
      map[A](o => o)

    override def narrow[A <: I]: ToComponent[A, R, O, C] =
      contramap[A](a => a)
  }

  object ToComponent {

    def apply[I, R, O, C](ref: Full[I, R, O], c: C): ToComponent[I, R, O, C] =
      new ToComponent(ref, c)

    def inject[I, R, O, CT[-p, +u] <: CtorType[p, u], P, U](c: CT[P, U], ref: Full[I, R, O]): ToComponent[I, R, O, CT[P, U]] =
      apply(ref, CtorType.hackBackToSelf(c)(c.withRawProp("ref", ref.rawSetFn)))
  }

  // ===================================================================================================================

  type ToJsComponent[P <: js.Object, S <: js.Object, R <: JsComponent.RawMounted[P, S]] =
    Ref.Full[R, R, JsComponent.MountedWithRawType[P, S, R]]

  def toJsComponent[P <: js.Object, S <: js.Object]: ToJsComponent[P, S, JsComponent.RawMounted[P, S]] =
    apply[JsComponent.RawMounted[P, S]].map(JsComponent.mounted[P, S](_))

  def toJsComponentWithMountedFacade[P <: js.Object, S <: js.Object, F <: js.Object]: ToJsComponent[P, S, JsComponent.RawMounted[P, S] with F] =
    apply[JsComponent.RawMounted[P, S] with F].map(JsComponent.mounted[P, S](_).addFacade[F])

  type WithJsComponent[F[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object] =
    Ref.ToComponent[R, R, JsComponent.MountedWithRawType[P0, S0, R],
      CT1[P1, JsComponent.UnmountedMapped[F, P1, S1, R, P0, S0]]]

  def toJsComponent[F[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]]
      (a: WithJsComponentArg[F, P1, S1, CT1, R, P0, S0])
      : WithJsComponent[F, P1, S1, CT1, R, P0, S0] =
    a.value

  // Ridiculous that this is needed but Scala needs explicit help when F=Effect.Id
  final class WithJsComponentArg[F[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object]
      (val value: WithJsComponent[F, P1, S1, CT1, R, P0, S0]) extends AnyVal

  object WithJsComponentArg {
    implicit def direct[F[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]]
        (c: JsComponent.ComponentMapped[F, P1, S1, CT1, R, P0, S0, CT0])
        : WithJsComponentArg[F, P1, S1, CT1, R, P0, S0] =
      new WithJsComponentArg[F, P1, S1, CT1, R, P0, S0](ToComponent.inject(c, toJsComponentWithMountedFacade[P0, S0, R]))

    implicit def effectId[P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]]
        (c: JsComponent.ComponentMapped[Effect.Id, P1, S1, CT1, R, P0, S0, CT0])
        : WithJsComponentArg[Effect.Id, P1, S1, CT1, R, P0, S0] =
      new WithJsComponentArg[Effect.Id, P1, S1, CT1, R, P0, S0](ToComponent.inject(c, toJsComponentWithMountedFacade[P0, S0, R]))
  }

  // ===================================================================================================================

  type ToScalaComponent[P, S, B] =
    Ref.Full[
      ScalaComponent.RawMounted[P, S, B],
      ScalaComponent.RawMounted[P, S, B],
      ScalaComponent.MountedImpure[P, S, B]]

  def toScalaComponent[P, S, B]: ToScalaComponent[P, S, B] =
    apply[ScalaComponent.RawMounted[P, S, B]].map(_.mountedImpure)

  type WithScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]] =
    Ref.ToComponent[
      ScalaComponent.RawMounted[P, S, B],
      ScalaComponent.RawMounted[P, S, B],
      ScalaComponent.MountedImpure[P, S, B],
      CT[P, ScalaComponent.Unmounted[P, S, B]]]

  def toScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]]
                      (c: ScalaComponent[P, S, B, CT])
                      : WithScalaComponent[P, S, B, CT] =
    ToComponent.inject(c, toScalaComponent[P, S, B])

}