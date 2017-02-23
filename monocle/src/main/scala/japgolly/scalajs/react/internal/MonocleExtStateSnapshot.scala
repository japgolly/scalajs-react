package japgolly.scalajs.react.internal

import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.extra._

trait MonocleExtStateSnapshot {
  import MonocleExtStateSnapshot._

  final implicit def MonocleReactExt_StateSnapshot[A](x: StateSnapshot[A]) = new Instance(x)
  final implicit def MonocleReactExt_StateSnapshotNR(x: StateSnapshot.type) = new WithoutReuse(StateSnapshot)
  final implicit def MonocleReactExt_StateSnapshotWR(x: StateSnapshot.withReuse.type) = new WithReuse(StateSnapshot.withReuse)
}

object MonocleExtStateSnapshot {
  // Keep this import here so that Lens etc take priority over .internal
  import monocle._

  final class Instance[A](private val self: StateSnapshot[A]) extends AnyVal {
    def setStateL[B](l: Lens[A, B]): B => Callback =
      b => self.setState(l.set(b)(self.value))

    def modStateL[B](l: Lens[A, B])(f: B => B): Callback =
      self.setState(l.modify(f)(self.value))
  }

  final class WithoutReuse(private val ε: StateSnapshot.type) extends AnyVal {
    def zoomL[S, T](lens: Lens[S, T]) = StateSnapshot.zoom(lens.get)(lens.set)
  }

  final class WithReuse(private val ε: StateSnapshot.withReuse.type) extends AnyVal {
    def zoomL[S, T](lens: Lens[S, T]) = StateSnapshot.withReuse.zoom(lens.get)(lens.set)
  }

}
