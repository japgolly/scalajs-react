package japgolly.scalajs.react.extra

import japgolly.scalajs.react.util.Effect._
import japgolly.scalajs.react.util.{DefaultEffects, NotAllowed}
import japgolly.scalajs.react.{Reusability, Reusable, StateAccess}
import scala.reflect.ClassTag

final class StateSnapshotF[F[_], S](val value: S,
                                    val underlyingSetFn: Reusable[StateSnapshotF.SetFn[F, S]],
                                    private[extra] val reusability: Reusability[S])
                                   (implicit FF: Sync[F]) extends StateAccess.Write[F, DefaultEffects.Async, S] {

  override protected implicit def A: Async[DefaultEffects.Async] = DefaultEffects.Async
  override protected implicit def F: Sync[F] = FF

  override def toString = s"StateSnapshot($value)"

  /** @param callback Executed regardless of whether state is changed. */
  override def setStateOption[G[_]](newState: Option[S], callback: => G[Unit])(implicit G: Dispatch[G]): F[Unit] =
    underlyingSetFn(newState, F.fromJsFn0(G.dispatchFn(callback)))

  /** @param callback Executed regardless of whether state is changed. */
  override def modStateOption[G[_]](mod: S => Option[S], callback: => G[Unit])(implicit G: Dispatch[G]): F[Unit] =
    setStateOption(mod(value), callback)

  private def copyWithoutReuse[T](value: T)(set: StateSnapshotF.SetFn[F, T]): StateSnapshotF[F, T] =
    new StateSnapshotF[F, T](value, Reusable.byRef(set), Reusability.never)(F)

  /** THIS WILL VOID REUSABILITY.
    *
    * The resulting `StateSnapshot[T]` will not be reusable.
    */
  def xmapState[T](f: S => T)(g: T => S): StateSnapshotF[F, T] =
    copyWithoutReuse(f(value))((ot, cb) => underlyingSetFn.value(ot map g, cb))

  /** THIS WILL VOID REUSABILITY.
    *
    * The resulting `StateSnapshot[T]` will not be reusable.
    */
  def zoomState[T](f: S => T)(g: T => S => S): StateSnapshotF[F, T] =
    xmapState(f)(g(_)(value))

  /** THIS WILL VOID REUSABILITY.
    *
    * The resulting `StateSnapshot[T]` will not be reusable.
    */
  def zoomStateOption[T](f: S => Option[T])(g: T => S => S): Option[StateSnapshotF[F, T]] =
    f(value).map(t => copyWithoutReuse(t)((ot, cb) => underlyingSetFn.value(ot.map(g(_)(value)), cb)))

  def withReuse: StateSnapshotF.InstanceMethodsWithReuse[F, S] =
    new StateSnapshotF.InstanceMethodsWithReuse(this)

  /** @return `None` if `value: S` isn't `value: T` as well. */
  def narrowOption[T <: S: ClassTag]: Option[StateSnapshotF[F, T]] =
    value match {
      case t: T => Some(copyWithoutReuse(t)(setStateOption(_, _)(F)))
      case _    => None
    }

  /** Unsafe because writes may be dropped.
    *
    * Eg. if you widen a `StateSnapshot[Banana]` into `StateSnapshot[Food]`, calling `setState(banana2)` will work
    * but `setState(pasta)` will be silently ignored.
    */
  def unsafeWiden[T >: S](implicit ct: ClassTag[S]): StateSnapshotF[F, T] =
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
    * The resulting `StateSnapshotF[F, S]` will not be reusable.
    */
  def withValue(s: S): StateSnapshotF[F, S] =
    copyWithoutReuse(s)(underlyingSetFn)
}

// █████████████████████████████████████████████████████████████████████████████████████████████████████████████████████

object StateSnapshotF {
  type SetFn      [F[_], -S] = (Option[S]     , F[Unit])   => F[Unit]
  type ModFn      [F[_],  S] = (S => Option[S], F[Unit])   => F[Unit]
  type TupledSetFn[F[_], -S] = ((Option[S]     , F[Unit])) => F[Unit]
  type TupledModFn[F[_],  S] = ((S => Option[S], F[Unit])) => F[Unit]

  type StateSnapshot[S] = StateSnapshotF[DefaultEffects.Sync, S]

  private[this] val reusabilityInstance: Reusability[StateSnapshotF[Nothing, Any]] = {
    val f = implicitly[Reusability[Reusable[SetFn[Nothing, Any]]]] // Safe to reuse
    Reusability((x, y) =>
      (x eq y) ||
        (f.test(x.underlyingSetFn, y.underlyingSetFn)
          && x.reusability.test(x.value, y.value)
          && y.reusability.test(x.value, y.value)))
  }

  implicit def reusability[F[_], A[_], S]: Reusability[StateSnapshotF[F, S]] =
    reusabilityInstance.asInstanceOf[Reusability[StateSnapshotF[F, S]]]

  final class InstanceMethodsWithReuse[F[_], S](self: StateSnapshotF[F, S]) { // not AnyVal, nominal for Monocle ext

    def withValue(s: S)(implicit r: Reusability[S]): StateSnapshotF[F, S] =
      new StateSnapshotF(s, self.underlyingSetFn, r)(self.F)

    @deprecated("This ability doesn't work. See https://github.com/japgolly/scalajs-react/issues/721 for an explanation, and https://japgolly.github.io/scalajs-react/#examples/state-snapshot-2 for the alternative.", "1.7.1")
    def xmapState(no: NotAllowed) = no.result

    @deprecated("This ability doesn't work. See https://github.com/japgolly/scalajs-react/issues/721 for an explanation, and https://japgolly.github.io/scalajs-react/#examples/state-snapshot-2 for the alternative.", "1.7.1")
    def zoomState(no: NotAllowed) = no.result

    @deprecated("This ability doesn't work. See https://github.com/japgolly/scalajs-react/issues/721 for an explanation, and https://japgolly.github.io/scalajs-react/#examples/state-snapshot-2 for the alternative.", "1.7.1")
    def zoomStateOption(no: NotAllowed) = no.result
  }

}
