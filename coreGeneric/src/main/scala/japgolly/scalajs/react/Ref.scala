package japgolly.scalajs.react

import japgolly.scalajs.react.util.DefaultEffects
import japgolly.scalajs.react.util.Effect._
import japgolly.scalajs.react.util.JsUtil.jsNullToOption
import japgolly.scalajs.react.util.Util.identityFn
import japgolly.scalajs.react.vdom.TopNode
import scala.reflect.ClassTag
import scala.scalajs.js
import scala.scalajs.js.|

object Ref {
  import japgolly.scalajs.react.component.{Js => JsComponent, Scala => ScalaComponent}

  def apply[A]: Simple[A] =
    fromJs(facade.React.createRef[A | Null]())

  def fromJs[A](raw: facade.React.RefHandle[A | Null]): Simple[A] =
    Full(raw, identityFn, Some(_))

  def forwardedFromJs[A](f: facade.React.ForwardedRef[A]): Option[Simple[A]] =
    jsNullToOption(f).map(fromJs)

  type Full   [I, A, O] = FullF[DefaultEffects.Sync, I, A, O]
  type Get    [A]       = GetF[DefaultEffects.Sync, A]
  type Handle [A]       = HandleF[DefaultEffects.Sync, A]
  type Set    [A]       = SetF[DefaultEffects.Sync, A]
  type Simple [A]       = SimpleF[DefaultEffects.Sync, A]
  type SimpleF[F[_], A] = FullF[F, A, A, A]

  trait HandleF[F[_], A] {
    val raw: facade.React.RefHandle[A | Null]
    final def root: Simple[A] = fromJs(raw)
  }

  trait GetF[F[_], A] { self =>
    protected[Ref] def F: Sync[F]
    def withEffect[G[_]](implicit G: Sync[G]): GetF[G, A]

    def get: F[Option[A]]

    def map[B](f: A => B): GetF[F, B]

    def mapOption[B](f: A => Option[B]): GetF[F, B]

    def narrowOption[B <: A](implicit ct: ClassTag[B]): GetF[F, B]

    def widen[B >: A]: GetF[F, B]

    final def foreach(f: A => Unit): F[Unit] =
      foreachCB(a => F.delay(f(a)))

    final def foreachCB(f: A => F[Unit]): F[Unit] =
      F.flatMap(get) {
        case Some(a) => f(a)
        case None    => F.empty
      }

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
      F.runSync(get).getOrElse(sys error "Reference is empty")
  }

  trait SetF[F[_], A] {
    protected[Ref] def F: Sync[F]
    def withEffect[G[_]](implicit G: Sync[G]): SetF[G, A]

    def set(newValue: Option[A]): F[Unit]

    final lazy val rawSetFn: facade.React.RefFn[A] =
      (n: A | Null) => F.runSync(set(jsNullToOption(n)))

    def contramap[B](f: B => A): SetF[F, B]

    def narrow[B <: A]: SetF[F, B]
  }

  trait FullF[F[_], I, A, O] extends HandleF[F, A] with SetF[F, I] with GetF[F, O] { self =>
    override def withEffect[G[_]](implicit G: Sync[G]): FullF[G, I, A, O]
    override def contramap[X](f: X => I): FullF[F, X, A, O]
    override def narrow[X <: I]: FullF[F, X, A, O]
    override def map[X](f: O => X): FullF[F, I, A, X]
    override def widen[X >: O]: FullF[F, I, A, X]
    override def mapOption[B](f: O => Option[B]): FullF[F, I, A, B]

    final override def narrowOption[B <: O](implicit ct: ClassTag[B]): FullF[F, I, A, B] =
      mapOption(ct.unapply)
  }

  def Full[I, A, O](raw: facade.React.RefHandle[A | Null], l: I => A, r: A => Option[O]): Full[I, A, O] =
    FullF(raw, l, r)(DefaultEffects.Sync)

  def FullF[F[_], I, A, O](_raw: facade.React.RefHandle[A | Null], l: I => A, r: A => Option[O])(implicit FF: Sync[F]): FullF[F, I, A, O] =
    new FullF[F, I, A, O] {

      override protected[Ref] def F = FF

      override def withEffect[G[_]](implicit G: Sync[G]) =
        G.subst[F, ({type L[E[_]] = FullF[E, I, A, O]})#L](this)(
          FullF(raw, l, r)(G))

      override val raw = _raw

      override def set(newValue: Option[I]) =
        F.delay {
          raw.current = newValue match {
            case Some(i) => l(i)
            case None    => null
          }
        }

      override val get =
        F.delay(jsNullToOption(raw.current).flatMap(r))

      override def contramap[X](f: X => I): FullF[F, X, A, O] =
        FullF(raw, l compose f, r)

      override def narrow[X <: I]: FullF[F, X, A, O] =
        FullF(raw, l, r)

      override def map[X](f: O => X): FullF[F, I, A, X] =
        FullF(raw, l, r.andThen(_.map(f)))

      override def widen[X >: O]: FullF[F, I, A, X] =
        FullF(raw, l, r)

      override def mapOption[B](f: O => Option[B]): FullF[F, I, A, B] =
        FullF(raw, l, r.andThen(_.flatMap(f)))
    }

  // ===================================================================================================================

  type ToComponent[I, R, O, C] = ToComponentF[DefaultEffects.Sync, I, R, O, C]

  final class ToComponentF[F[_], I, R, O, C](ref: FullF[F, I, R, O], val component: C) extends FullF[F, I, R, O] {
    override protected[Ref] def F = ref.F

    override def withEffect[G[_]](implicit G: Sync[G]) =
      G.subst[F, ({type L[E[_]] = ToComponentF[E, I, R, O, C]})#L](this)(
        new ToComponentF(ref.withEffect[G], component)
      )(F)

    override val raw = ref.raw
    override val get = ref.get
    override def set(o: Option[I]) = ref.set(o)

    override def contramap[A](f: A => I): ToComponentF[F, A, R, O, C] =
      ToComponent(ref.contramap(f), component)

    override def map[A](f: O => A): ToComponentF[F, I, R, A, C] =
      ToComponent(ref.map(f), component)

    override def mapOption[B](f: O => Option[B]): FullF[F, I, R, B] =
      ToComponent(ref.mapOption(f), component)

    override def widen[A >: O]: ToComponentF[F, I, R, A, C] =
      map[A](o => o)

    override def narrow[A <: I]: ToComponentF[F, A, R, O, C] =
      contramap[A](a => a)
  }

  object ToComponent {

    def apply[F[_], I, R, O, C](ref: FullF[F, I, R, O], c: C): ToComponentF[F, I, R, O, C] =
      new ToComponentF(ref, c)

    def inject[F[_], I, R, O, CT[-p, +u] <: CtorType[p, u], P, U](c: CT[P, U], ref: FullF[F, I, R, O]): ToComponentF[F, I, R, O, CT[P, U]] =
      apply(ref, CtorType.hackBackToSelf[CT, P, U](c)(c.withRawProp("ref", ref.rawSetFn)))
  }

  // ===================================================================================================================

  // /** @since 2.0.0 */
  // trait NonEmpty[I, R, O] { self =>
  //   val raw: facade.React.RefHandle[R]
  //   def get: CallbackTo[O]
  //   def set(i: I): Callback
  //   def mod(f: O => I): Callback

  //   def contramap[A](f: A => I): NonEmpty[A, R, O] =
  //     new NonEmpty[A, R, O] {
  //       override val raw   = self.raw
  //       def get            = self.get
  //       def set(a: A)      = self.set(f(a))
  //       def mod(g: O => A) = self.mod(f compose g)
  //     }

  //   def map[A](f: O => A): NonEmpty[I, R, A] =
  //     new NonEmpty[I, R, A] {
  //       override val raw   = self.raw
  //       def get            = self.get.map(f)
  //       def set(i: I)      = self.set(i)
  //       def mod(g: A => I) = self.mod(g compose f)
  //     }
  // }

  // object NonEmpty {
  //   type Simple[A] = NonEmpty[A, A, A]

  //   def Simple[A](r: facade.React.RefHandle[A]): Simple[A] =
  //     new NonEmpty[A, A, A] {
  //       override val raw   = r
  //       def get            = CallbackTo(raw.current)
  //       def set(a: A)      = Callback { raw.current = a }
  //       def mod(f: A => A) = Callback { raw.current = f(raw.current) }
  //     }
  // }

  // ===================================================================================================================

  type ToJsComponentF[F[_], P <: js.Object, S <: js.Object, R <: JsComponent.RawMounted[P, S]] =
    FullF[F, R, R, JsComponent.MountedWithRawType[P, S, R]]

  type ToJsComponent[P <: js.Object, S <: js.Object, R <: JsComponent.RawMounted[P, S]] =
    ToJsComponentF[DefaultEffects.Sync, P, S, R]

  def toJsComponent[P <: js.Object, S <: js.Object]: ToJsComponent[P, S, JsComponent.RawMounted[P, S]] =
    apply[JsComponent.RawMounted[P, S]].map(JsComponent.mounted[P, S](_))

  def toJsComponentWithMountedFacade[P <: js.Object, S <: js.Object, F <: js.Object]: ToJsComponent[P, S, JsComponent.RawMounted[P, S] with F] =
    apply[JsComponent.RawMounted[P, S] with F].map(JsComponent.mounted[P, S](_).addFacade[F])

  type WithJsComponentF[FR[_], FJ[_], A[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object] =
    ToComponentF[FR, R, R, JsComponent.MountedWithRawType[P0, S0, R],
      CT1[P1, JsComponent.UnmountedMapped[FJ, A, P1, S1, R, P0, S0]]]

  type WithJsComponent[F[_], A[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object] =
    WithJsComponentF[DefaultEffects.Sync, F, A, P1, S1, CT1, R, P0, S0]

  def toJsComponent[F[_], A[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]]
      (a: WithJsComponentArg[F, A, P1, S1, CT1, R, P0, S0])
      : WithJsComponent[F, A, P1, S1, CT1, R, P0, S0] =
    a.wrap(toJsComponentWithMountedFacade[P0, S0, R])

  // Ridiculous that this is needed but Scala needs explicit help when F=Effect.Id
  final class WithJsComponentArg[F[_], A[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object]
      (val wrap: ToJsComponent[P0, S0, JsComponent.RawMounted[P0, S0] with R] =>
                 WithJsComponent[F, A, P1, S1, CT1, R, P0, S0]
      ) extends AnyVal

  object WithJsComponentArg {
    implicit def direct[F[_], A[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]]
        (c: JsComponent.ComponentMapped[F, A, P1, S1, CT1, R, P0, S0, CT0])
        : WithJsComponentArg[F, A, P1, S1, CT1, R, P0, S0] =
      new WithJsComponentArg[F, A, P1, S1, CT1, R, P0, S0](ToComponent.inject(c, _))

    implicit def effectId[A[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u]]
        (c: JsComponent.ComponentMapped[Id, A, P1, S1, CT1, R, P0, S0, CT0])
        : WithJsComponentArg[Id, A, P1, S1, CT1, R, P0, S0] =
      new WithJsComponentArg[Id, A, P1, S1, CT1, R, P0, S0](ToComponent.inject(c, _))
  }

  // ===================================================================================================================

  type ToScalaComponentF[F[_], P, S, B] =
    FullF[
      F,
      ScalaComponent.RawMounted[P, S, B],
      ScalaComponent.RawMounted[P, S, B],
      ScalaComponent.MountedImpure[P, S, B]]

  type ToScalaComponent[P, S, B] =
    ToScalaComponentF[DefaultEffects.Sync, P, S, B]

  def toScalaComponent[P, S, B]: ToScalaComponent[P, S, B] =
    apply[ScalaComponent.RawMounted[P, S, B]].map(_.mountedImpure)

  type WithScalaComponentF[F[_], P, S, B, CT[-p, +u] <: CtorType[p, u]] =
    ToComponentF[
      F,
      ScalaComponent.RawMounted[P, S, B],
      ScalaComponent.RawMounted[P, S, B],
      ScalaComponent.MountedImpure[P, S, B],
      CT[P, ScalaComponent.Unmounted[P, S, B]]]

  type WithScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]] =
    WithScalaComponentF[DefaultEffects.Sync, P, S, B, CT]

  def toScalaComponent[P, S, B, CT[-p, +u] <: CtorType[p, u]]
                      (c: ScalaComponent.Component[P, S, B, CT])
                      : WithScalaComponent[P, S, B, CT] =
    ToComponent.inject(c, toScalaComponent[P, S, B])

  // ===================================================================================================================

  type ToAnyVdom = Simple[TopNode]

  /** For use with the `untypedRef` vdom attribute. */
  def toAnyVdom(): ToAnyVdom =
    apply

  type ToVdom[N <: TopNode] = Full[TopNode, TopNode, N]

  /** For use with the `untypedRef` vdom attribute. */
  def toVdom[N <: TopNode : ClassTag]: ToVdom[N] =
    toAnyVdom().narrowOption[N]
}