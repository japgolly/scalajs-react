package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.internal.{Iso, Lens}

final class StateSnapshot[S](val value: S,
                             val underlyingSetFn: Reusable[StateSnapshot.SetFn[S]],
                             private[StateSnapshot] val reusability: Reusability[S]) extends StateAccess.Write[CallbackTo, S] {

  override def toString = s"StateSnapshot($value)"

  /** @param callback Executed regardless of whether state is changed. */
  override def setStateOption(newState: Option[S], callback: Callback): Callback =
    underlyingSetFn(newState, callback)

  /** @param callback Executed regardless of whether state is changed. */
  override def modStateOption(mod: S => Option[S], callback: Callback): Callback =
    setStateOption(mod(value), callback)

  /** THIS WILL VOID REUSABILITY.
    *
    * The resulting `StateSnapshot[T]` will not be reusable.
    */
  def xmapState[T](f: S => T)(g: T => S): StateSnapshot[T] =
    StateSnapshot(f(value))((ot, cb) => underlyingSetFn.value(ot map g, cb))

  /** THIS WILL VOID REUSABILITY.
    *
    * The resulting `StateSnapshot[T]` will not be reusable.
    */
  def zoomState[T](f: S => T)(g: T => S => S): StateSnapshot[T] =
    xmapState(f)(g(_)(value))

  def withReuse: StateSnapshot.InstanceMethodsWithReuse[S] =
    new StateSnapshot.InstanceMethodsWithReuse(this)
}

object StateSnapshot {

  type SetFn[-S] = (Option[S], Callback) => Callback
  type ModFn[S] = (S => Option[S], Callback) => Callback

  type TupledSetFn[-S] = ((Option[S], Callback)) => Callback
  type TupledModFn[S] = ((S => Option[S], Callback)) => Callback

  private def reusableSetFn[S](f: SetFn[S]): Reusable[SetFn[S]] =
    Reusable.byRef(f)

  private def untuple[A,B,C](f: ((A, B)) => C): (A, B) => C =
    (a, b) => f((a, b))

  final class InstanceMethodsWithReuse[S](self: StateSnapshot[S]) { // not AnyVal, nominal for Monocle ext
    import self.{value, underlyingSetFn}

    def xmapState[T](iso: Reusable[(S => T, T => S)]): StateSnapshot[T] =
      new StateSnapshot[T](
        iso._1(value),
        Reusable.ap(underlyingSetFn, iso)((f, g) => (ot, cb) => f(ot map g._2, cb)),
        self.reusability.contramap(iso._2))

    def zoomState[T](lens: Reusable[(S => T, T => S => S)]): StateSnapshot[T] =
      new StateSnapshot[T](
        lens._1(value),
        Reusable.ap(underlyingSetFn, lens)((f, g) => (ot, cb) => f(ot.map(g._2(_)(value)), cb)),
        self.reusability.contramap(lens._2(_)(value)))
  }

  // ███████████████████████████████████████████████████████████████████████████████████████████████████████████████████
  // Construction DSL

  object withReuse {

    // Putting (implicit r: Reusability[S]) here would shadow WithReuse.apply
    def apply[S](value: S): FromValue[S] =
      new FromValue(value)

    /** This is meant to be called once and reused so that the setState callback stays the same. */
    def prepare[S](f: SetFn[S]): FromSetStateFn[S] =
      new FromSetStateFn(reusableSetFn(f))

    /** This is meant to be called once and reused so that the setState callback stays the same. */
    def prepareTupled[S](f: TupledSetFn[S]): FromSetStateFn[S] =
      prepare(untuple(f))

    /** This is meant to be called once and reused so that the setState callback stays the same. */
    def prepareVia[I, S](i: I)(implicit t: StateAccessor.WritePure[I, S]): FromSetStateFn[S] =
      prepare(t(i).setStateOption)

    def xmap[S, T](get: S => T)(set: T => S): FromLens[S, T] =
      new FromLens(Iso(get)(set).toLens)

    def zoom[S, T](get: S => T)(set: T => S => S): FromLens[S, T] =
      new FromLens(Lens(get)(set))

    final class FromLens[S, T](private val l: Lens[S, T]) extends AnyVal {
      // There's no point having (value: S)(mod: (S => S) ~=> Callback) because the callback will be composed with the
      // lens which avoids reusability.
      // def apply(value: S) = new FromLensValue(l, l get value)

      /** This is meant to be called once and reused so that the setState callback stays the same. */
      def prepare(modify: ModFn[S]): FromLensSetStateFn[S, T] =
        new FromLensSetStateFn[S, T](l, reusableSetFn((ot, cb) => modify(l setO ot, cb)))

      /** This is meant to be called once and reused so that the setState callback stays the same. */
      def prepareTupled(modify: TupledModFn[S]): FromLensSetStateFn[S, T] =
        prepare(untuple(modify))

      /** This is meant to be called once and reused so that the setState callback stays the same. */
      def prepareVia[I](i: I)(implicit t: StateAccessor.WritePure[I, S]): FromLensSetStateFn[S, T] =
        prepare(t(i).modStateOption)

      def xmap[U](get: T => U)(set: U => T): FromLens[S, U] =
        new FromLens(l --> Iso(get)(set))

      def zoom[U](get: T => U)(set: U => T => T): FromLens[S, U] =
        new FromLens(l --> Lens(get)(set))
    }

    final class FromValue[S](private val value: S) extends AnyVal {
      def apply(set: Reusable[SetFn[S]])(implicit r: Reusability[S]): StateSnapshot[S] =
        new StateSnapshot(value, set, r)

      def tupled(set: Reusable[TupledSetFn[S]])(implicit r: Reusability[S]): StateSnapshot[S] =
        apply(set.map(untuple))
    }

    final class FromSetStateFn[S](private val set: Reusable[SetFn[S]]) extends AnyVal {
      def apply(value: S)(implicit r: Reusability[S]): StateSnapshot[S] =
        withReuse(value)(set)(r)
    }

    final class FromLensSetStateFn[S, T](l: Lens[S, T], set: Reusable[SetFn[T]]) {
      def apply(value: S)(implicit r: Reusability[T]): StateSnapshot[T] =
        withReuse(l get value)(set)(r)
    }
  }

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // withoutReuse is the default and needn't be specified explicitly

  import withoutReuse._

  def apply[S](value: S): FromValue[S] =
    new FromValue(value)

  def of[I, S](i: I)(implicit t: StateAccessor.ReadImpureWritePure[I, S]): StateSnapshot[S] =
    apply(t.state(i)).setStateVia(i)

  def xmap[S, T](get: S => T)(set: T => S): FromLens[S, T] =
    new FromLens(Iso(get)(set).toLens)

  def zoom[S, T](get: S => T)(set: T => S => S): FromLens[S, T] =
    new FromLens(Lens(get)(set))

  object withoutReuse {
    final class FromLens[S, T](private val l: Lens[S, T]) extends AnyVal {
      def apply(value: S) = new FromLensValue(l, l get value)

      def of[I](i: I)(implicit t: StateAccessor.ReadImpureWritePure[I, S]): StateSnapshot[T] =
        apply(t.state(i)).setStateVia(i)

      def xmap[U](get: T => U)(set: U => T): FromLens[S, U] =
        new FromLens(l --> Iso(get)(set))

      def zoom[U](get: T => U)(set: U => T => T): FromLens[S, U] =
        new FromLens(l --> Lens(get)(set))
    }

    final class FromValue[S](private val value: S) extends AnyVal {
      def apply(set: SetFn[S]): StateSnapshot[S] =
        new StateSnapshot(value, reusableSetFn(set), Reusability.never)

      def tupled(set: TupledSetFn[S]): StateSnapshot[S] =
        apply(untuple(set))

      def setStateVia[I](i: I)(implicit t: StateAccessor.WritePure[I, S]): StateSnapshot[S] =
        apply(t(i).setStateOption)
    }

    final class FromLensValue[S, T](l: Lens[S, T], value: T) {
      def apply(modify: ModFn[S]): StateSnapshot[T] =
        StateSnapshot(value)((ot, cb) => modify(l setO ot, cb))

      def tupled(modify: TupledModFn[S]): StateSnapshot[T] =
        apply(untuple(modify))

      def setStateVia[I](i: I)(implicit t: StateAccessor.WritePure[I, S]): StateSnapshot[T] =
        apply(t(i).modStateOption)
    }
  }

  // ███████████████████████████████████████████████████████████████████████████████████████████████████████████████████

  private[this] val reusabilityInstance: Reusability[StateSnapshot[Any]] = {
    val f = implicitly[Reusability[Reusable[SetFn[Any]]]] // Safe to reuse
    Reusability((x, y) =>
      (x eq y) ||
        (f.test(x.underlyingSetFn, y.underlyingSetFn)
          && x.reusability.test(x.value, y.value)
          && y.reusability.test(x.value, y.value)))
  }

  implicit def reusability[S]: Reusability[StateSnapshot[S]] =
    reusabilityInstance.asInstanceOf[Reusability[StateSnapshot[S]]]
}
