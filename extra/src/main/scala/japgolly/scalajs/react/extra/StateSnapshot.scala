package japgolly.scalajs.react.extra

import japgolly.scalajs.react._

final class StateSnapshot[S](val value: S,
                             val setState: S ~=> Callback,
                             private[StateSnapshot] val reusability: Reusability[S]) {

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

  def apply[S](value: S)(set: S => Callback): StateSnapshot[S] =
    new StateSnapshot(value, ReusableFn(set), Reusability.never)

  object reuse {
    def apply[S](value: S)(set: S ~=> Callback)(implicit r: Reusability[S]): StateSnapshot[S] =
      new StateSnapshot(value, set, r)
  }
}