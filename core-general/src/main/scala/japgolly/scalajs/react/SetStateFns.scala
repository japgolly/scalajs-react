package japgolly.scalajs.react

import japgolly.scalajs.react.util.Effect
import japgolly.scalajs.react.util.SafeEffect.Sync
import japgolly.scalajs.react.util.Util.identityFn

final class SetStateFn[F[_], S](underlyingFn: (Option[S], Sync.RawCallback) => F[Unit])
                               (implicit FF: Effect[F]) extends StateAccess.SetState[F, S] {

  override protected implicit def F = FF

  override def apply[G[_], A](newState: Option[S], callback: => G[A])(implicit G: Sync[G]): F[Unit] =
    underlyingFn(newState, G.syncJsFn0(callback))

  /** @param callback Executed regardless of whether state is changed. */
  override def setStateOption[G[_], A](newState: Option[S], callback: => G[A])(implicit G: Sync[G]): F[Unit] =
    underlyingFn(newState, G.syncJsFn0(callback))

  override def toSetStateFn: SetStateFn[F, S] =
    this

  def contramap[A](f: A => S): SetStateFn[F, A] =
    SetStateFn[F, A]((a, cb) => underlyingFn(a map f, cb))(FF)

  def narrow[A <: S]: SetStateFn[F, A] =
    contramap(identityFn)
}

object SetStateFn {
  def apply[F[_]: Effect, S](f: (Option[S], Sync.RawCallback) => F[Unit]): SetStateFn[F, S] =
    new SetStateFn(f)
}

// =====================================================================================================================

final class ModStateFn[F[_], S](underlyingFn: (S => Option[S], Sync.RawCallback) => F[Unit])
                               (implicit FF: Effect[F]) extends StateAccess.ModState[F, S] {

  override protected implicit def F = FF

  override def apply[G[_], A](f: S => Option[S], callback: => G[A])(implicit G: Sync[G]): F[Unit] =
    underlyingFn(f, G.syncJsFn0(callback))

  /** @param callback Executed regardless of whether state is changed. */
  override def modStateOption[G[_], A](f: S => Option[S], callback: => G[A])(implicit G: Sync[G]): F[Unit] =
    underlyingFn(f, G.syncJsFn0(callback))

  override def toModStateFn: ModStateFn[F, S] =
    this

  def toModStateWithPropsFn[P](p: P): ModStateWithPropsFn[F, P, S] =
    ModStateWithPropsFn[F, P, S]((f, cb) => underlyingFn(f(_, p), cb))(FF)

  def toSetStateFn: SetStateFn[F, S] =
    SetStateFn[F, S]((s, cb) => underlyingFn(_ => s, cb))(FF)

  def xmapState[A](f: S => A)(g: A => S): ModStateFn[F, A] =
    ModStateFn[F, A]((m, cb) => underlyingFn(s => m(f(s)) map g, cb))(FF)
}

object ModStateFn {
  def apply[F[_]: Effect, S](f: (S => Option[S], Sync.RawCallback) => F[Unit]): ModStateFn[F, S] =
    new ModStateFn(f)
}

// =====================================================================================================================

final class ModStateWithPropsFn[F[_], P, S](underlyingFn: ((S, P) => Option[S], Sync.RawCallback) => F[Unit])
                                           (implicit FF: Effect[F]) extends StateAccess.ModStateWithProps[F, P, S] {

  override protected implicit def F = FF

  override def apply[G[_], A](f: (S, P) => Option[S], callback: => G[A])(implicit G: Sync[G]): F[Unit] =
    underlyingFn(f, G.syncJsFn0(callback))

  /** @param callback Executed regardless of whether state is changed. */
  override def modStateOption[G[_], A](f: (S, P) => Option[S], callback: => G[A])(implicit G: Sync[G]): F[Unit] =
    underlyingFn(f, G.syncJsFn0(callback))

  override def toModStateWithPropsFn: ModStateWithPropsFn[F, P, S] =
    this

  def toModStateFn: ModStateFn[F, S] =
    ModStateFn[F, S]((f, cb) => underlyingFn((s, _) => f(s), cb))(FF)

  def toSetStateFn: SetStateFn[F, S] =
    SetStateFn[F, S]((s, cb) => underlyingFn((_, _) => s, cb))(FF)

  def mapProps[A](f: P => A): ModStateWithPropsFn[F, A, S] =
    ModStateWithPropsFn[F, A, S]((m, cb) => underlyingFn((s, p) => m(s, f(p)), cb))(FF)

  def widenProps[A >: P]: ModStateWithPropsFn[F, A, S] =
    mapProps(identityFn)

  def xmapState[A](f: S => A)(g: A => S): ModStateWithPropsFn[F, P, A] =
    ModStateWithPropsFn[F, P, A]((m, cb) => underlyingFn((s, p) => m(f(s), p) map g, cb))(FF)
}

object ModStateWithPropsFn {
  def apply[F[_]: Effect, P, S](f: ((S, P) => Option[S], Sync.RawCallback) => F[Unit]): ModStateWithPropsFn[F, P, S] =
    new ModStateWithPropsFn(f)
}
