package japgolly.scalajs.react.extra

import japgolly.scalajs.react.util.Effect._
import japgolly.scalajs.react.util.{DefaultEffects, NotAllowed}
import japgolly.scalajs.react.{Reusability, Reusable, StateAccess}
import scala.reflect.ClassTag

final class StateSnapshotF[F[_], A[_], S](val value: S,
                                          val underlyingSetFn: Reusable[StateSnapshotF.SetFn[F, S]],
                                          private[extra] val reusability: Reusability[S])
                                         (implicit FF: UnsafeSync[F], AA: Async[A]) extends StateAccess.Write[F, A, S] {

  override type WithEffect     [G[_]] = StateSnapshotF[G, A, S]
  override type WithAsyncEffect[G[_]] = StateSnapshotF[F, G, S]

  override def withEffect[G[_]](implicit G: UnsafeSync[G]): WithEffect[G] =
    G.subst[F, ({ type L[E[_]] = StateSnapshotF[E, A, S] })#L](this) {
      val setFn2: Reusable[StateSnapshotF.SetFn[G, S]] =
        underlyingSetFn.map(f => (a, c) => G.transSync(f(a, F.transSync(c)))(F))
      new StateSnapshotF(value, setFn2, reusability)(G, A)
    }(F)

  override def withAsyncEffect[G[_]](implicit G: Async[G]): WithAsyncEffect[G] =
    new StateSnapshotF(value, underlyingSetFn, reusability)(F, G)

  override protected implicit def F: UnsafeSync[F] = FF
  override protected implicit def A: Async[A] = AA

  override def toString = s"StateSnapshot($value)"

  /** @param callback Executed regardless of whether state is changed. */
  override def setStateOption[G[_]](newState: Option[S], callback: => G[Unit])(implicit G: Dispatch[G]): F[Unit] =
    underlyingSetFn(newState, F.transDispatch(callback))

  /** @param callback Executed regardless of whether state is changed. */
  override def modStateOption[G[_]](mod: S => Option[S], callback: => G[Unit])(implicit G: Dispatch[G]): F[Unit] =
    setStateOption(mod(value), callback)

  private def copyWithoutReuse[T](value: T)(set: StateSnapshotF.SetFn[F, T]): StateSnapshotF[F, A, T] =
    new StateSnapshotF[F, A, T](value, Reusable.byRef(set), Reusability.never)(F, A)

  /** THIS WILL VOID REUSABILITY.
    *
    * The resulting `StateSnapshot[T]` will not be reusable.
    */
  def xmapState[T](f: S => T)(g: T => S): StateSnapshotF[F, A, T] =
    copyWithoutReuse(f(value))((ot, cb) => underlyingSetFn.value(ot map g, cb))

  /** THIS WILL VOID REUSABILITY.
    *
    * The resulting `StateSnapshot[T]` will not be reusable.
    */
  def zoomState[T](f: S => T)(g: T => S => S): StateSnapshotF[F, A, T] =
    xmapState(f)(g(_)(value))

  /** THIS WILL VOID REUSABILITY.
    *
    * The resulting `StateSnapshot[T]` will not be reusable.
    */
  def zoomStateOption[T](f: S => Option[T])(g: T => S => S): Option[StateSnapshotF[F, A, T]] =
    f(value).map(t => copyWithoutReuse(t)((ot, cb) => underlyingSetFn.value(ot.map(g(_)(value)), cb)))

  def withReuse: StateSnapshotF.InstanceMethodsWithReuse[F, A, S] =
    new StateSnapshotF.InstanceMethodsWithReuse(this)

  /** @return `None` if `value: S` isn't `value: T` as well. */
  def narrowOption[T <: S: ClassTag]: Option[StateSnapshotF[F, A, T]] =
    value match {
      case t: T => Some(copyWithoutReuse(t)(setStateOption(_, _)(F)))
      case _    => None
    }

  /** Unsafe because writes may be dropped.
    *
    * Eg. if you widen a `StateSnapshot[Banana]` into `StateSnapshot[Food]`, calling `setState(banana2)` will work
    * but `setState(pasta)` will be silently ignored.
    */
  def unsafeWiden[T >: S](implicit ct: ClassTag[S]): StateSnapshotF[F, A, T] =
    copyWithoutReuse(value: T) { (ob, cb) =>
      val oa: Option[S] =
        ob match {
          case Some(s: S) => Some(s)
          case _          => None
        }
      setStateOption(oa, cb)(F)
    }

  /** THIS WILL VOID REUSABILITY.
    *
    * The resulting `StateSnapshotF[F, A, S]` will not be reusable.
    */
  def withValue(s: S): StateSnapshotF[F, A, S] =
    copyWithoutReuse(s)(underlyingSetFn)
}

// █████████████████████████████████████████████████████████████████████████████████████████████████████████████████████

object StateSnapshotF {
  type SetFn      [F[_], -S] = (Option[S]     , F[Unit])   => F[Unit]
  type ModFn      [F[_],  S] = (S => Option[S], F[Unit])   => F[Unit]
  type TupledSetFn[F[_], -S] = ((Option[S]     , F[Unit])) => F[Unit]
  type TupledModFn[F[_],  S] = ((S => Option[S], F[Unit])) => F[Unit]

  type StateSnapshot[S] = StateSnapshotF[DefaultEffects.Sync, DefaultEffects.Async, S]

  private[this] val reusabilityInstance: Reusability[StateSnapshotF[Nothing, Nothing, Any]] = {
    val f = implicitly[Reusability[Reusable[SetFn[Nothing, Any]]]] // Safe to reuse
    Reusability((x, y) =>
      (x eq y) ||
        (f.test(x.underlyingSetFn, y.underlyingSetFn)
          && x.reusability.test(x.value, y.value)
          && y.reusability.test(x.value, y.value)))
  }

  implicit def reusability[F[_], A[_], S]: Reusability[StateSnapshotF[F, A, S]] =
    reusabilityInstance.asInstanceOf[Reusability[StateSnapshotF[F, A, S]]]

  final class InstanceMethodsWithReuse[F[_], A[_], S](self: StateSnapshotF[F, A, S]) { // not AnyVal, nominal for Monocle ext

    def withValue(s: S)(implicit r: Reusability[S]): StateSnapshotF[F, A, S] =
      new StateSnapshotF(s, self.underlyingSetFn, r)(self.F, self.A)

    @deprecated("This ability doesn't work. See https://github.com/japgolly/scalajs-react/issues/721 for an explanation, and https://japgolly.github.io/scalajs-react/#examples/state-snapshot-2 for the alternative.", "1.7.1")
    def xmapState(no: NotAllowed) = no.result

    @deprecated("This ability doesn't work. See https://github.com/japgolly/scalajs-react/issues/721 for an explanation, and https://japgolly.github.io/scalajs-react/#examples/state-snapshot-2 for the alternative.", "1.7.1")
    def zoomState(no: NotAllowed) = no.result

    @deprecated("This ability doesn't work. See https://github.com/japgolly/scalajs-react/issues/721 for an explanation, and https://japgolly.github.io/scalajs-react/#examples/state-snapshot-2 for the alternative.", "1.7.1")
    def zoomStateOption(no: NotAllowed) = no.result
  }

}
