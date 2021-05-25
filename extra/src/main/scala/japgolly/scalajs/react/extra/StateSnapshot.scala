package japgolly.scalajs.react.extra

import japgolly.scalajs.react._
import japgolly.scalajs.react.internal.{Effect, Iso, Lens, NotAllowed}
import scala.reflect.ClassTag

final class StateSnapshot[S](val value: S,
                             val underlyingSetFn: Reusable[StateSnapshot.SetFn[S]],
                             private[StateSnapshot] val reusability: Reusability[S]) extends StateAccess.Write[CallbackTo, S] {

  override protected implicit def F: Effect[CallbackTo] =
    Effect.callbackInstance

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

  /** THIS WILL VOID REUSABILITY.
    *
    * The resulting `StateSnapshot[T]` will not be reusable.
    */
  def zoomStateOption[T](f: S => Option[T])(g: T => S => S): Option[StateSnapshot[T]] =
    f(value).map(t => StateSnapshot(t)((ot, cb) => underlyingSetFn.value(ot.map(g(_)(value)), cb)))

  def withReuse: StateSnapshot.InstanceMethodsWithReuse[S] =
    new StateSnapshot.InstanceMethodsWithReuse(this)

  /** @return `None` if `value: S` isn't `value: T` as well. */
  def narrowOption[T <: S: ClassTag]: Option[StateSnapshot[T]] =
    value match {
      case b: T => Some(StateSnapshot(b)(setStateOption(_, _)))
      case _    => None
    }

  /** Unsafe because writes may be dropped.
    *
    * Eg. if you widen a `StateSnapshot[Banana]` into `StateSnapshot[Food]`, calling `setState(banana2)` will work
    * but `setState(pasta)` will be silently ignored.
    */
  def unsafeWiden[T >: S](implicit ct: ClassTag[S]): StateSnapshot[T] =
    StateSnapshot[T](value) { (ob, cb) =>
      val oa: Option[S] =
        ob match {
          case Some(s: S) => Some(s)
          case _          => None
        }
      setStateOption(oa, cb)
    }

  /** THIS WILL VOID REUSABILITY.
    *
    * The resulting `StateSnapshot[S]` will not be reusable.
    */
  def withValue(s: S): StateSnapshot[S] =
    StateSnapshot(s)(underlyingSetFn)
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

  private lazy val setFnReadOnly: Reusable[SetFn[Any]] =
    reusableSetFn[Any]((_, cb) => cb)

  final class InstanceMethodsWithReuse[S](self: StateSnapshot[S]) { // not AnyVal, nominal for Monocle ext

    def withValue(s: S)(implicit r: Reusability[S]): StateSnapshot[S] =
      StateSnapshot.withReuse(s)(self.underlyingSetFn)

    @deprecated("This ability doesn't work. See https://github.com/japgolly/scalajs-react/issues/721 for an explanation, and https://japgolly.github.io/scalajs-react/#examples/state-snapshot-2 for the alternative.", "1.7.1")
    def xmapState(no: NotAllowed) = no.result

    @deprecated("This ability doesn't work. See https://github.com/japgolly/scalajs-react/issues/721 for an explanation, and https://japgolly.github.io/scalajs-react/#examples/state-snapshot-2 for the alternative.", "1.7.1")
    def zoomState(no: NotAllowed) = no.result

    @deprecated("This ability doesn't work. See https://github.com/japgolly/scalajs-react/issues/721 for an explanation, and https://japgolly.github.io/scalajs-react/#examples/state-snapshot-2 for the alternative.", "1.7.1")
    def zoomStateOption(no: NotAllowed) = no.result
  }

  // ███████████████████████████████████████████████████████████████████████████████████████████████████████████████████
  // Construction DSL

  object withReuse {

    // Putting (implicit r: Reusability[S]) here would shadow WithReuse.apply
    def apply[S](value: S): FromValue[S] =
      new FromValue(value)

    /** @since 1.8.0 */
    def hook[S](initialValue: => S)(implicit rs: Reusability[S]): CustomHook[Unit, StateSnapshot[S]] =
      CustomHook[Unit]
        .useState(initialValue)
        .useRef(List.empty[Callback])
        .useEffectBy { (_, _, delayedCallbacks) => _(
          delayedCallbacks.get.flatMap(cbs =>
            Callback.when(cbs.nonEmpty)(
              Callback.runAll(cbs: _*) >> delayedCallbacks.set(Nil)
            )
          )
        )}
        .buildReturning { (_, state, delayedCallbacks) =>
          val setFn: SetFn[S] = (os, cb) =>
            os match {
              case Some(s) => delayedCallbacks.mod(cb :: _) >> state.setState(s)
              case None    => cb
            }
          new StateSnapshot[S](state.value, state.originalSetState.withValue(setFn), rs)
        }

    /** This is meant to be called once and reused so that the setState callback stays the same. */
    def prepare[S](f: SetFn[S]): FromSetStateFn[S] =
      new FromSetStateFn(reusableSetFn(f))

    /** This is meant to be called once and reused so that the setState callback stays the same. */
    def prepareTupled[S](f: TupledSetFn[S]): FromSetStateFn[S] =
      prepare(untuple(f))

    /** This is meant to be called once and reused so that the setState callback stays the same. */
    def prepareVia[I, S](i: I)(implicit t: StateAccessor.WritePure[I, S]): FromSetStateFn[S] =
      prepare(t(i).setStateOption)

    def prepareViaProps[P, I, S]($: GenericComponent.MountedPure[P, _])(f: P => I)(implicit t: I => StateAccess.SetState[CallbackTo, S]): FromSetStateFn[S] =
      prepareViaCallback($.props.map(f))

    def prepareViaCallback[I, S](cb: CallbackTo[I])(implicit t: I => StateAccess.SetState[CallbackTo, S]): FromSetStateFn[S] =
      prepare((os, k) => cb.flatMap(t(_).setStateOption(os, k)))

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

      def prepareViaProps[P, I]($: GenericComponent.MountedPure[P, _])(f: P => I)(implicit t: I => StateAccess.ModState[CallbackTo, S]): FromLensSetStateFn[S, T] =
        prepareViaCallback($.props.map(f))

      def prepareViaCallback[I](cb: CallbackTo[I])(implicit t: I => StateAccess.ModState[CallbackTo, S]): FromLensSetStateFn[S, T] =
        prepare((f, k) => cb.flatMap(t(_).modStateOption(f, k)))

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

      def readOnly(implicit r: Reusability[S]): StateSnapshot[S] =
        apply(setFnReadOnly)
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

  /** @since 1.8.0 */
  def hook[S](initialValue: => S): CustomHook[Unit, StateSnapshot[S]] =
    withReuse.hook(initialValue)(Reusability.never)

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

      def readOnly: StateSnapshot[S] =
        apply(setFnReadOnly)
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
