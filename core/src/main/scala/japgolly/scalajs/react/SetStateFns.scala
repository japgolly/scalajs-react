package japgolly.scalajs.react

import scala.runtime.AbstractFunction2
import japgolly.scalajs.react.internal.identityFn

final class SetStateFn[F[_], S](underlyingFn: (Option[S], Callback) => F[Unit]) extends AbstractFunction2[Option[S], Callback, F[Unit]] with StateAccess.SetState[F, S] {

  override def apply(newState: Option[S], callback: Callback): F[Unit] =
    underlyingFn(newState, callback)

  /** @param callback Executed regardless of whether state is changed. */
  override def setStateOption(newState: Option[S], callback: Callback): F[Unit] =
    underlyingFn(newState, callback)

  override def toSetStateFn: SetStateFn[F, S] =
    this

  def contramap[A](f: A => S): SetStateFn[F, A] =
    SetStateFn((a, cb) => underlyingFn(a map f, cb))

  def narrow[A <: S]: SetStateFn[F, A] =
    contramap(identityFn)
}

object SetStateFn {
  def apply[F[_], S](f: (Option[S], Callback) => F[Unit]): SetStateFn[F, S] =
    new SetStateFn(f)
}

// =====================================================================================================================

final class ModStateFn[F[_], S](underlyingFn: (S => Option[S], Callback) => F[Unit]) extends AbstractFunction2[S => Option[S], Callback, F[Unit]] with StateAccess.ModState[F, S] {

  override def apply(f: S => Option[S], callback: Callback): F[Unit] =
    underlyingFn(f, callback)

  /** @param callback Executed regardless of whether state is changed. */
  override def modStateOption(f: S => Option[S], callback: Callback): F[Unit] =
    underlyingFn(f, callback)

  override def toModStateFn: ModStateFn[F, S] =
    this

  def toSetStateFn: SetStateFn[F, S] =
    SetStateFn((s, cb) => underlyingFn(_ => s, cb))

  def xmap[A](f: S => A)(g: A => S): ModStateFn[F, A] =
    ModStateFn((m, cb) => underlyingFn(s => m(f(s)) map g, cb))
}

object ModStateFn {
  def apply[F[_], S](f: (S => Option[S], Callback) => F[Unit]): ModStateFn[F, S] =
    new ModStateFn(f)
}
