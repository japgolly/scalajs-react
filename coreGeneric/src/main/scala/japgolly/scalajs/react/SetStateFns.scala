package japgolly.scalajs.react

import japgolly.scalajs.react.util.Effect.Sync.Untyped
import japgolly.scalajs.react.util.Effect._
import japgolly.scalajs.react.util.Util.identityFn

final class SetStateFn[F[_], A[_], S](underlyingFn: (Option[S], Untyped[Unit]) => F[Unit])
                                     (implicit FF: UnsafeSync[F], AA: Async[A]) extends StateAccess.SetState[F, A, S] {

  override type WithEffect     [G[_]] = SetStateFn[G, A, S]
  override type WithAsyncEffect[G[_]] = SetStateFn[F, G, S]

  override protected implicit def F = FF
  override protected implicit def A = AA

  override def withEffect[G[_]](implicit G: UnsafeSync[G]) =
    G.subst[F, WithEffect](this)(new SetStateFn(G.transSyncFn2(underlyingFn)(F))(G, A))(F)

  override def withAsyncEffect[G[_]](implicit G: Async[G]) =
    G.subst[A, WithAsyncEffect](this)(new SetStateFn(underlyingFn)(F, G))(A)

  /** @param callback Executed regardless of whether state is changed. */
  override def setStateOption[G[_]](newState: Option[S], callback: => G[Unit])(implicit G: Dispatch[G]): F[Unit] =
    underlyingFn(newState, G.dispatchFn(callback))

  override def toSetStateFn: SetStateFn[F, A, S] =
    this

  def contramap[B](f: B => S): SetStateFn[F, A, B] =
    SetStateFn[F, A, B]((a, cb) => underlyingFn(a map f, cb))(F, A)

  def narrow[B <: S]: SetStateFn[F, A, B] =
    contramap(identityFn)
}

object SetStateFn {
  def apply[F[_]: UnsafeSync, A[_]: Async, S](f: (Option[S], Untyped[Unit]) => F[Unit]): SetStateFn[F, A, S] =
    new SetStateFn(f)
}

// =====================================================================================================================

final class ModStateFn[F[_], A[_], S](underlyingFn: (S => Option[S], Untyped[Unit]) => F[Unit])
                                     (implicit FF: UnsafeSync[F], AA: Async[A]) extends StateAccess.ModState[F, A, S] {

  override type WithEffect     [G[_]] = ModStateFn[G, A, S]
  override type WithAsyncEffect[G[_]] = ModStateFn[F, G, S]

  override protected implicit def F = FF
  override protected implicit def A = AA

  override def withEffect[G[_]](implicit G: UnsafeSync[G]) =
    G.subst[F, WithEffect](this)(new ModStateFn(G.transSyncFn2(underlyingFn)(F))(G, A))(F)

  override def withAsyncEffect[G[_]](implicit G: Async[G]) =
    G.subst[A, WithAsyncEffect](this)(new ModStateFn(underlyingFn)(F, G))(A)

  /** @param callback Executed regardless of whether state is changed. */
  override def modStateOption[G[_]](f: S => Option[S], callback: => G[Unit])(implicit G: Dispatch[G]): F[Unit] =
    underlyingFn(f, G.dispatchFn(callback))

  override def toModStateFn: ModStateFn[F, A, S] =
    this

  def toModStateWithPropsFn[P](p: P): ModStateWithPropsFn[F, A, P, S] =
    ModStateWithPropsFn[F, A, P, S]((f, cb) => underlyingFn(f(_, p), cb))(F, A)

  def toSetStateFn: SetStateFn[F, A, S] =
    SetStateFn[F, A, S]((s, cb) => underlyingFn(_ => s, cb))(F, A)

  def xmapState[B](f: S => B)(g: B => S): ModStateFn[F, A, B] =
    ModStateFn[F, A, B]((m, cb) => underlyingFn(s => m(f(s)) map g, cb))(F, A)
}

object ModStateFn {
  def apply[F[_]: UnsafeSync, A[_]: Async, S](f: (S => Option[S], Untyped[Unit]) => F[Unit]): ModStateFn[F, A, S] =
    new ModStateFn(f)
}

// =====================================================================================================================

final class ModStateWithPropsFn[F[_], A[_], P, S](underlyingFn: ((S, P) => Option[S], Untyped[Unit]) => F[Unit])
                                                 (implicit FF: UnsafeSync[F], AA: Async[A]) extends StateAccess.ModStateWithProps[F, A, P, S] {

  override type WithEffect     [G[_]] = ModStateWithPropsFn[G, A, P, S]
  override type WithAsyncEffect[G[_]] = ModStateWithPropsFn[F, G, P, S]

  override protected implicit def F = FF
  override protected implicit def A = AA

  override def withEffect[G[_]](implicit G: UnsafeSync[G]) =
    G.subst[F, WithEffect](this)(new ModStateWithPropsFn(G.transSyncFn2(underlyingFn)(F))(G, A))(F)

  override def withAsyncEffect[G[_]](implicit G: Async[G]) =
    G.subst[A, WithAsyncEffect](this)(new ModStateWithPropsFn(underlyingFn)(F, G))(A)

  /** @param callback Executed regardless of whether state is changed. */
  override def modStateOption[G[_]](f: (S, P) => Option[S], callback: => G[Unit])(implicit G: Dispatch[G]): F[Unit] =
    underlyingFn(f, G.dispatchFn(callback))

  override def toModStateWithPropsFn: ModStateWithPropsFn[F, A, P, S] =
    this

  def toModStateFn: ModStateFn[F, A, S] =
    ModStateFn[F, A, S]((f, cb) => underlyingFn((s, _) => f(s), cb))(F, A)

  def toSetStateFn: SetStateFn[F, A, S] =
    SetStateFn[F, A, S]((s, cb) => underlyingFn((_, _) => s, cb))(F, A)

  def mapProps[B](f: P => B): ModStateWithPropsFn[F, A, B, S] =
    ModStateWithPropsFn[F, A, B, S]((m, cb) => underlyingFn((s, p) => m(s, f(p)), cb))(F, A)

  def widenProps[B >: P]: ModStateWithPropsFn[F, A, B, S] =
    mapProps(identityFn)

  def xmapState[B](f: S => B)(g: B => S): ModStateWithPropsFn[F, A, P, B] =
    ModStateWithPropsFn[F, A, P, B]((m, cb) => underlyingFn((s, p) => m(f(s), p) map g, cb))(F, A)
}

object ModStateWithPropsFn {
  def apply[F[_]: UnsafeSync, A[_]: Async, P, S](f: ((S, P) => Option[S], Untyped[Unit]) => F[Unit]): ModStateWithPropsFn[F, A, P, S] =
    new ModStateWithPropsFn(f)
}
