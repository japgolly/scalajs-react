package japgolly.scalajs.react.internal.monocle

import japgolly.scalajs.react.extra.{StateSnapshot, StateSnapshotF}
import japgolly.scalajs.react.util.DefaultEffects.Sync
import japgolly.scalajs.react.util.NotAllowed
import scala.annotation.nowarn

@nowarn("cat=unused")
trait MonocleExtStateSnapshot {
  import MonocleExtStateSnapshot._

  @inline final implicit def MonocleReactExt_StateSnapshotNR(x: StateSnapshot.type): ObjectWithoutReuse.type =
    ObjectWithoutReuse

  @inline final implicit def MonocleReactExt_StateSnapshotWR(x: StateSnapshot.withReuse.type): ObjectWithReuse.type =
    ObjectWithReuse

  @inline final implicit def MonocleReactExt_StateSnapshot[A](x: StateSnapshot[A]): Instance[A] =
    new Instance(x)

  @inline final implicit def MonocleReactExt_StateSnapshotWR[F[_], A](x: StateSnapshotF.InstanceMethodsWithReuse[F, A]): InstanceWithReuse[F, A] =
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

  final class Instance[A](private val self: StateSnapshot[A]) extends AnyVal {
    def setStateL[B](l: monocle.Lens[A, B]): B => Sync[Unit] =
      b => self.setState(l.set(b)(self.value))

    def modStateL[B](l: monocle.Lens[A, B])(f: B => B): Sync[Unit] =
      self.setState(l.modify(f)(self.value))

    /** THIS WILL VOID REUSABILITY.
      *
      * The resulting `StateSnapshot[T]` will not be reusable.
      */
    def xmapStateL[B](iso: monocle.Iso[A, B]): StateSnapshot[B] =
      self.xmapState(iso.get)(iso.reverseGet)

    /** THIS WILL VOID REUSABILITY.
      *
      * The resulting `StateSnapshot[T]` will not be reusable.
      */
    def zoomStateL[B](lens: monocle.Lens[A, B]): StateSnapshot[B] =
      self.zoomState(lens.get)(lens.set)

    /** THIS WILL VOID REUSABILITY.
      *
      * The resulting `StateSnapshot[T]` will not be reusable.
      */
    def zoomStateO[B](o: monocle.Optional[A, B]): Option[StateSnapshot[B]] =
      self.zoomStateOption(o.getOption)(o.set)
  }

  final class InstanceWithReuse[F[_], A](private val self: StateSnapshotF.InstanceMethodsWithReuse[F, A]) extends AnyVal {

    @deprecated("This ability doesn't work. See https://github.com/japgolly/scalajs-react/issues/721 for an explanation, and https://japgolly.github.io/scalajs-react/#examples/state-snapshot-2 for the alternative.", "1.7.1")
    def xmapStateL(no: NotAllowed) = no.result

    @deprecated("This ability doesn't work. See https://github.com/japgolly/scalajs-react/issues/721 for an explanation, and https://japgolly.github.io/scalajs-react/#examples/state-snapshot-2 for the alternative.", "1.7.1")
    def zoomStateL(no: NotAllowed) = no.result

    @deprecated("This ability doesn't work. See https://github.com/japgolly/scalajs-react/issues/721 for an explanation, and https://japgolly.github.io/scalajs-react/#examples/state-snapshot-2 for the alternative.", "1.7.1")
    def zoomStateO(no: NotAllowed) = no.result
  }
}
