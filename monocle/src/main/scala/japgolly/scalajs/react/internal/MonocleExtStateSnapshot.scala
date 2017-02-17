package japgolly.scalajs.react.internal

import monocle._
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.extra._

trait MonocleExtStateSnapshot {
  import MonocleExtStateSnapshot._

  final implicit def MonocleReactExt_StateSnapshot[A](x: StateSnapshot[A]) = new Instance(x)
  final implicit def MonocleReactExt_StateSnapshotObj(x: StateSnapshot.type) = new Root(StateSnapshot)
}

object MonocleExtStateSnapshot {

  final class Instance[A](private val self: StateSnapshot[A]) extends AnyVal {
    def setStateL[B](l: Lens[A, B]): B => Callback =
      b => self.setState(l.set(b)(self.value))

    def modStateL[B](l: Lens[A, B])(f: B => B): Callback =
      self.setState(l.mod(f)(self.value))
  }

  final class Root(private val ε: StateSnapshot.type) extends AnyVal {
    def at[S, A](lens: Lens[S, A]) = new WithLens(lens)
  }

  // ===================================================================================================================

  final class WithLens[S, A](private val lens: Lens[S, A]) extends AnyVal {
    def apply(value: S) = new WithLensValue(lens, value)

    def of[I](i: I)(implicit t: StateAccess.ReadIdWriteCB[I, S]): StateSnapshot[A] =
      apply(t.state(i)).writeVia(i)

    def withReuse = new WithLensReuse(lens)
  }

  final class WithLensReuse[S, A](private val lens: Lens[S, A]) extends AnyVal {
    def apply(value: S) = new WithLensReuseValue(lens, value)

    def of[I](i: I)(implicit t: StateAccess.ReadIdWriteCB[I, S], r: Reusability[A]): StateSnapshot[A] =
      apply(t.state(i)).writeVia(i)
  }

  // ===================================================================================================================

  final class WithLensValue[S, A](private val lens: Lens[S, A], value: S) {
    def apply(mod: (S => S) => Callback): StateSnapshot[A] =
      StateSnapshot(lens get value)(a => mod(lens set a))

    def writeVia[I](i: I)(implicit t: StateAccess.WriteCB[I, S]): StateSnapshot[A] =
      apply(t.modState(i))
  }

  final class WithLensReuseValue[S, A](private val lens: Lens[S, A], value: S) {
    def apply(mod: (S => S) => Callback)(implicit r: Reusability[A]): StateSnapshot[A] =
      StateSnapshot.withReuse(lens get value)(ReusableFn(a => mod(lens set a)))(r)

    def writeVia[I](i: I)(implicit t: StateAccess.WriteCB[I, S], r: Reusability[A]): StateSnapshot[A] =
      apply(t.modState(i))
  }
}
