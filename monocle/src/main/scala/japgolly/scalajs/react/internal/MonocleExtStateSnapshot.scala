package japgolly.scalajs.react.internal

import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.Reusable
import japgolly.scalajs.react.extra.StateSnapshot

trait MonocleExtStateSnapshot {
  import MonocleExtStateSnapshot._

  final implicit def MonocleReactExt_StateSnapshotNR(x: StateSnapshot.type) = new ObjectWithoutReuse(StateSnapshot)
  final implicit def MonocleReactExt_StateSnapshotWR(x: StateSnapshot.withReuse.type) = new ObjectWithReuse(StateSnapshot.withReuse)
  final implicit def MonocleReactExt_StateSnapshot[A](x: StateSnapshot[A]) = new Instance(x)
  final implicit def MonocleReactExt_StateSnapshotWR[A](x: StateSnapshot.InstanceMethodsWithReuse[A]) = new InstanceWithReuse(x)
}

object MonocleExtStateSnapshot {

  final class ObjectWithoutReuse(private val ε: StateSnapshot.type) extends AnyVal {
    def zoomL[S, T](lens: monocle.Lens[S, T]) =
      StateSnapshot.zoom(lens.get)(lens.set)
  }

  final class ObjectWithReuse(private val ε: StateSnapshot.withReuse.type) extends AnyVal {
    def zoomL[S, T](lens: monocle.Lens[S, T]) =
      StateSnapshot.withReuse.zoom(lens.get)(lens.set)
  }

  final class Instance[A](private val self: StateSnapshot[A]) extends AnyVal {
    def setStateL[B](l: monocle.Lens[A, B]): B => Callback =
      b => self.setState(l.set(b)(self.value))

    def modStateL[B](l: monocle.Lens[A, B])(f: B => B): Callback =
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
  }

  final class InstanceWithReuse[A](private val self: StateSnapshot.InstanceMethodsWithReuse[A]) extends AnyVal {
    def xmapStateL[B](iso: Reusable[monocle.Iso[A, B]]): StateSnapshot[B] =
      self.xmapState(iso.map(i => (i.get, i.reverseGet)))

    def zoomStateL[B](lens: Reusable[monocle.Lens[A, B]]): StateSnapshot[B] =
      self.zoomState(lens.map(l => (l.get, l.set)))
  }
}
