package japgolly.scalajs.react.internal.monocle

import japgolly.scalajs.react.extra.{StateSnapshot, StateSnapshotF}

trait MonocleExtStateSnapshot {
  import MonocleExtStateSnapshot._

  @inline final implicit def MonocleReactExt_StateSnapshotNR(x: StateSnapshot.type): ObjectWithoutReuse.type =
    ObjectWithoutReuse

  @inline final implicit def MonocleReactExt_StateSnapshotWR(x: StateSnapshot.withReuse.type): ObjectWithReuse.type =
    ObjectWithReuse

  @inline final implicit def MonocleReactExt_StateSnapshot[F[_], A[_], S](x: StateSnapshotF[F, A, S]): Instance[F, A, S] =
    new Instance(x)

  @inline final implicit def MonocleReactExt_StateSnapshotWR[F[_], A[_], S](x: StateSnapshotF.InstanceMethodsWithReuse[F, A, S]): InstanceWithReuse[F, A, S] =
    new InstanceWithReuse(x)
}

object MonocleExtStateSnapshot {

  object ObjectWithoutReuse {
    def zoomL[S, T](lens: monocle.Lens[S, T]) =
      StateSnapshot.zoom(lens.get)(lens.set)
  }

  object ObjectWithReuse {
    def zoomL[S, T](lens: monocle.Lens[S, T]) =
      StateSnapshot.withReuse.zoom(lens.get)(lens.set)
  }

  final class Instance[F[_], A[_], S](private val self: StateSnapshotF[F, A, S]) extends AnyVal {
    def setStateL[T](l: monocle.Lens[S, T]): T => F[Unit] =
      b => self.setState(l.set(b)(self.value))

    def modStateL[T](l: monocle.Lens[S, T])(f: T => T): F[Unit] =
      self.setState(l.modify(f)(self.value))

    /** THIS WILL VOID REUSABILITY.
      *
      * The resulting `StateSnapshot[T]` will not be reusable.
      */
    def xmapStateL[T](iso: monocle.Iso[S, T]): StateSnapshotF[F, A, T] =
      self.xmapState(iso.get)(iso.reverseGet)

    /** THIS WILL VOID REUSABILITY.
      *
      * The resulting `StateSnapshot[T]` will not be reusable.
      */
    def zoomStateL[T](lens: monocle.Lens[S, T]): StateSnapshotF[F, A, T] =
      self.zoomState(lens.get)(lens.set)

    /** THIS WILL VOID REUSABILITY.
      *
      * The resulting `StateSnapshot[T]` will not be reusable.
      */
    def zoomStateO[T](o: monocle.Optional[S, T]): Option[StateSnapshotF[F, A, T]] =
      self.zoomStateOption(o.getOption)(o.set)
  }

  final class InstanceWithReuse[F[_], A[_], S](private val self: StateSnapshotF.InstanceMethodsWithReuse[F, A, S]) extends AnyVal {
  }
}
