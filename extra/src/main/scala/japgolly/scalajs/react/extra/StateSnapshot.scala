package japgolly.scalajs.react.extra

import japgolly.scalajs.react._

final class StateSnapshot[S](val value: S,
                             val setState: S ~=> Callback,
                             private[StateSnapshot] val reusability: Reusability[S]) {

  @deprecated("Use setState instead.", "1.0.0") def set = setState
  @deprecated("Use modState instead.", "1.0.0") def mod = modState _

  def modState(f: S => S): Callback =
    setState(f(value))

  def xmap[T](f: S => T)(g: T => S): StateSnapshot[T] =
    new StateSnapshot(f(value), setState contramap g, reusability contramap g)

  // Zoom is dangerously deceptive here as it appears to work but will often override the non-zoomed subset of state.
  // Use the zoom methods on Mounted directly for a reliable function.
  //
  //def zoom[T](f: S => T)(g: T => S => S): StateSnapshot[T] =
  //  xmap(f)(g(_)(value))
}

object StateSnapshot {

  private[this] val reusabilityInstance: Reusability[StateSnapshot[Any]] = {
    val f = implicitly[Reusability[Any ~=> Callback]] // Safe to reuse
    Reusability((x, y) =>
      (x eq y) ||
      (x.reusability.test(x.value, y.value) && f.test(x.setState, y.setState)))
  }

  implicit def reusability[S]: Reusability[StateSnapshot[S]] =
    reusabilityInstance.asInstanceOf[Reusability[StateSnapshot[S]]]

  // ===================================================================================================================
  // No reuse

  def apply[S](value: S) = new NoReuse(value)
  final class NoReuse[S](private val value: S) extends AnyVal {

    def apply(set: S => Callback): StateSnapshot[S] =
      new StateSnapshot(value, ReusableFn(set), Reusability.never)

    def writeVia[I](i: I)(implicit t: StateAccessor.WriteCB[I, S]): StateSnapshot[S] =
      apply(t.setState(i))
  }

  def of[I, S](i: I)(implicit t: StateAccessor.ReadIdWriteCB[I, S]): StateSnapshot[S] =
    apply(t.state(i)).writeVia(i)

  // ===================================================================================================================

  object withReuse {

    // Putting (implicit r: Reusability[S]) here would shadow WithReuse.apply
    def apply[S](value: S) = new WithReuse(value)
    final class WithReuse[S](private val value: S) extends AnyVal {

      def apply(set: S ~=> Callback)(implicit r: Reusability[S]): StateSnapshot[S] =
        new StateSnapshot(value, set, r)

      def writeVia[I](i: I)(implicit t: StateAccessor.WriteCB[I, S], r: Reusability[S]): StateSnapshot[S] =
        apply(ReusableFn(t setState i))(r)
    }

    def of[I, S](i: I)(implicit t: StateAccessor.ReadIdWriteCB[I, S], r: Reusability[S]): StateSnapshot[S] =
      apply(t.state(i)).writeVia(i)(t, r)
  }
}
