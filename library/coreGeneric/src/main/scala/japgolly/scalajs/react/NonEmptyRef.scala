package japgolly.scalajs.react

import japgolly.scalajs.react.util.DefaultEffects
import japgolly.scalajs.react.util.Effect._
import japgolly.scalajs.react.util.Util.identityFn

/** Types:
  *
  *   - `Handle` - `raw: facade.React.RefHandle[A]`
  *   - `Get`    - `get: F[A]`
  *   - `Set`    - `set: A => F[Unit]`
  *   - `Full`   - `Handle & Set & Get`
  *   - `Simple` - Monomorphic version of `Full`
  */
object NonEmptyRef {

  def apply[A](initialValue: A): Simple[A] = {
    val r = facade.React.createRef[A]()
    r.current = initialValue
    fromJs(r)
  }

  def fromJs[A](r: facade.React.RefHandle[A]): Simple[A] =
    Full(r, identityFn, identityFn)

  type Full   [I, A, O] = FullF  [DefaultEffects.Sync, I, A, O]
  type Get    [A]       = GetF   [DefaultEffects.Sync, A]
  type Handle [A]       = HandleF[DefaultEffects.Sync, A]
  type Set    [A]       = SetF   [DefaultEffects.Sync, A]
  type Simple [A]       = SimpleF[DefaultEffects.Sync, A]
  type SimpleF[F[_], A] = FullF  [F, A, A, A]

  trait HandleF[F[_], A] { self =>
    val raw: facade.React.RefHandle[A]
    final def root: Simple[A] = fromJs(raw)
  }

  trait GetF[F[_], A] { self =>
    protected def F: Sync[F]
    def withEffect[G[_]](implicit G: Sync[G]): GetF[G, A]

    def get: F[A]

    def map[B](f: A => B): GetF[F, B]

    def widen[B >: A]: GetF[F, B]

    final def foreach(f: A => Unit): F[Unit] =
      foreachCB(a => F.delay(f(a)))

    final def foreachCB(f: A => F[Unit]): F[Unit] =
      F.flatMap(get)(f)

    /** Get the reference immediately.
      *
      * ONLY USE THIS IN UNIT TESTS. DO NOT USE THIS IN PRODUCTION CODE.
      *
      * Unsafe in the FP sense because it reads an underlying variable which is impure.
      */
    final def unsafeGet(): A =
      F.runSync(get)
  }

  trait SetF[F[_], A] {
    protected def F: Sync[F]
    def withEffect[G[_]](implicit G: Sync[G]): SetF[G, A]

    /** NOTE: This doesn't force an update-to/redraw-of your component. */
    def set(newValue: A): F[Unit]

    def contramap[B](f: B => A): SetF[F, B]

    def narrow[B <: A]: SetF[F, B]
  }

  trait FullF[F[_], I, A, O] extends HandleF[F, A] with SetF[F, I] with GetF[F, O] { self =>
    override def withEffect[G[_]](implicit G: Sync[G]): FullF[G, I, A, O]
    override def contramap[X](f: X => I): FullF[F, X, A, O]
    override def narrow[X <: I]: FullF[F, X, A, O]
    override def map[X](f: O => X): FullF[F, I, A, X]
    override def widen[X >: O]: FullF[F, I, A, X]

    /** NOTE: This doesn't force an update-to/redraw-of your component. */
    final def mod(f: O => I): F[Unit] =
      F.flatMap(get)(o => set(f(o)))
  }

  def Full[I, A, O](raw: facade.React.RefHandle[A], l: I => A, r: A => O): Full[I, A, O] =
    FullF(raw, l, r)(DefaultEffects.Sync)

  def FullF[F[_], I, A, O](_raw: facade.React.RefHandle[A], l: I => A, r: A => O)(implicit FF: Sync[F]): FullF[F, I, A, O] =
    new FullF[F, I, A, O] {

      override protected def F = FF

      override def withEffect[G[_]](implicit G: Sync[G]) =
        G.subst[F, ({type L[E[_]] = FullF[E, I, A, O]})#L](this)(
          FullF(raw, l, r)(G))

      override val raw = _raw

      override def set(newValue: I) =
        F.delay { raw.current = l(newValue) }

      override val get =
        F.delay(r(raw.current))

      override def contramap[X](f: X => I): FullF[F, X, A, O] =
        FullF(raw, l compose f, r)

      override def narrow[X <: I]: FullF[F, X, A, O] =
        FullF(raw, l, r)

      override def map[X](f: O => X): FullF[F, I, A, X] =
        FullF(raw, l, f compose r)

      override def widen[X >: O]: FullF[F, I, A, X] =
        FullF(raw, l, r)
    }
}
