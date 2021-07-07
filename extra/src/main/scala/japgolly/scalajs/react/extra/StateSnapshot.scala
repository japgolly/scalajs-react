package japgolly.scalajs.react.extra.internal

import japgolly.scalajs.react.component.{Generic => GenericComponent}
import japgolly.scalajs.react.extra.StateSnapshotF
import japgolly.scalajs.react.extra.StateSnapshotF.StateSnapshot
import japgolly.scalajs.react.hooks.{Api => HooksApi, CustomHook}
import japgolly.scalajs.react.internal.{Iso, Lens}
import japgolly.scalajs.react.util.DefaultEffects.{Async => DA, Sync => DS}
import japgolly.scalajs.react.util.Effect.Sync
import japgolly.scalajs.react.{Reusability, Reusable, StateAccess, StateAccessor}

object StateSnapshot {
  type SetFn      [-S] = StateSnapshotF.SetFn      [DS, S]
  type ModFn      [ S] = StateSnapshotF.ModFn      [DS, S]
  type TupledSetFn[-S] = StateSnapshotF.TupledSetFn[DS, S]
  type TupledModFn[ S] = StateSnapshotF.TupledModFn[DS, S]

  private def effectFn[F[_], A](f: (A, F[Unit]) => F[Unit])(implicit F: Sync[F]): (A, DS[Unit]) => DS[Unit] =
    DS.subst[F, ({ type L[E[_]] = (A, E[Unit]) => E[Unit]})#L](f)(
      (a, c) => DS.transSync(f(a, F.transSync(c))))

  private def effectFnT[F[_], A](f: ((A, F[Unit])) => F[Unit])(implicit F: Sync[F]): ((A, DS[Unit])) => DS[Unit] =
    effectFn[F, A](untuple(f)).tupled

  def SetFn      [F[_], S](f: StateSnapshotF.SetFn      [F, S])(implicit F: Sync[F]): SetFn      [S] = effectFn(f)
  def ModFn      [F[_], S](f: StateSnapshotF.ModFn      [F, S])(implicit F: Sync[F]): ModFn      [S] = effectFn(f)
  def TupledSetFn[F[_], S](f: StateSnapshotF.TupledSetFn[F, S])(implicit F: Sync[F]): TupledSetFn[S] = effectFnT(f)
  def TupledModFn[F[_], S](f: StateSnapshotF.TupledModFn[F, S])(implicit F: Sync[F]): TupledModFn[S] = effectFnT(f)

  // ===================================================================================================================

  private def reusableSetFn[S](f: SetFn[S]): Reusable[SetFn[S]] =
    Reusable.byRef(f)

  private def untuple[A,B,C](f: ((A, B)) => C): (A, B) => C =
    (a, b) => f((a, b))

  private lazy val setFnReadOnly: Reusable[SetFn[Any]] =
    reusableSetFn[Any]((_, cb) => cb)

  // ███████████████████████████████████████████████████████████████████████████████████████████████████████████████████
  // Construction DSL

  object withReuse {

    // Putting (implicit r: Reusability[S]) here would shadow WithReuse.apply
    def apply[S](value: S): FromValue[S] =
      new FromValue(value)

    /** @since 2.0.0 */
    def hook[S](initialValue: => S)(implicit rs: Reusability[S]): CustomHook[Unit, StateSnapshot[S]] =
      CustomHook[Unit]
        .useState(initialValue)
        .useRef(List.empty[DS[Unit]])
        .useEffectBy { (_, _, delayedCallbacks) =>
          val cbs = delayedCallbacks.value
          if (cbs.isEmpty)
            DS.empty
          else
            DS.chain(DS.runAll(cbs: _*), delayedCallbacks.set(Nil))
        }
        .buildReturning { (_, state, delayedCallbacks) =>
          val setFn: SetFn[S] = (os, cb) =>
            os match {
              case Some(s) =>
                val updateState = state.setState(s)
                if (DS.isEmpty(cb))
                  updateState
                else
                  DS.chain(delayedCallbacks.mod(cb :: _), updateState)
              case None =>
                cb
            }
          new StateSnapshot[S](state.value, state.originalSetState.withValue(setFn), rs)
        }

    /** This is meant to be called once and reused so that the setState callback stays the same. */
    def prepare[S](f: SetFn[S]): FromSetStateFn[S] =
      new FromSetStateFn(reusableSetFn(f))

    /** This is meant to be called once and reused so that the setState callback stays the same. */
    def prepareF[F[_]: Sync, S](f: StateSnapshotF.SetFn[F, S]): FromSetStateFn[S] =
      prepare(effectFn(f))

    /** This is meant to be called once and reused so that the setState callback stays the same. */
    def prepareTupled[S](f: TupledSetFn[S]): FromSetStateFn[S] =
      prepare(untuple(f))

    /** This is meant to be called once and reused so that the setState callback stays the same. */
    def prepareTupledF[F[_]: Sync, S](f: StateSnapshotF.TupledSetFn[F, S]): FromSetStateFn[S] =
      prepareF(untuple(f))

    /** This is meant to be called once and reused so that the setState callback stays the same. */
    def prepareVia[I, F[_], A[_], S](i: I)(implicit t: StateAccessor.Write[I, F, A, S]): FromSetStateFn[S] = {
      val w = t(i).withEffect[DS]
      prepare(w.setStateOption(_, _))
    }

    def prepareViaProps[F[_], A1[_], P, S1, I, G[_], A2[_], S]($: GenericComponent.Mounted[F, A1, P, S1])
                                                              (f: P => I)
                                                              (implicit t: I => StateAccess.SetState[G, A2, S], F: Sync[F], G: Sync[G]): FromSetStateFn[S] =
      prepareViaCallback(F.map($.props)(f))

    def prepareViaCallback[F[_], I, G[_], A[_], S](cb: F[I])(implicit t: I => StateAccess.SetState[G, A, S], F: Sync[F], G: Sync[G]): FromSetStateFn[S] =
      prepareF[F, S]((os, k) => F.flatMap(cb)(i => F.transSync(t(i).setStateOption(os, k))))

    def xmap[S, T](get: S => T)(set: T => S): FromLens[S, T] =
      new FromLens(Iso(get)(set).toLens)

    def zoom[S, T](get: S => T)(set: T => S => S): FromLens[S, T] =
      new FromLens(Lens(get)(set))

    final class FromLens[S, T](private val l: Lens[S, T]) extends AnyVal {
      // There's no point having (value: S)(mod: (S => S) ~=> Sync[Unit]) because the callback will be composed with the
      // lens which avoids reusability.
      // def apply(value: S) = new FromLensValue(l, l get value)

      /** This is meant to be called once and reused so that the setState callback stays the same. */
      def prepare(modify: ModFn[S]): FromLensSetStateFn[S, T] =
        new FromLensSetStateFn[S, T](l, reusableSetFn((ot, cb) => modify(l setO ot, cb)))

      /** This is meant to be called once and reused so that the setState callback stays the same. */
      def prepareF[F[_]: Sync](modify: StateSnapshotF.ModFn[F, S]): FromLensSetStateFn[S, T] =
        prepare(effectFn(modify))

      /** This is meant to be called once and reused so that the setState callback stays the same. */
      def prepareTupled(modify: TupledModFn[S]): FromLensSetStateFn[S, T] =
        prepare(untuple(modify))

      /** This is meant to be called once and reused so that the setState callback stays the same. */
      def prepareTupledF[F[_]: Sync](modify: StateSnapshotF.TupledModFn[F, S]): FromLensSetStateFn[S, T] =
        prepareF(untuple(modify))

      /** This is meant to be called once and reused so that the setState callback stays the same. */
      def prepareVia[F[_], A[_], I](i: I)(implicit t: StateAccessor.Write[I, F, A, S]): FromLensSetStateFn[S, T] = {
        val w = t(i).withEffect[DS]
        prepare(w.modStateOption(_, _))
      }

    // def prepareViaProps[P, I]($: GenericComponent.MountedPure[P, _])(f: P => I)(implicit t: I => StateAccess.ModState[Sync, Async, S]): FromLensSetStateFn[S, T] =
    def prepareViaProps[F[_], A1[_], P, S1, I, G[_], A2[_]]($: GenericComponent.Mounted[F, A1, P, S1])
                                                           (f: P => I)
                                                           (implicit t: I => StateAccess.ModState[G, A2, S], F: Sync[F], G: Sync[G]): FromLensSetStateFn[S, T] =
        prepareViaCallback(F.map($.props)(f))

      def prepareViaCallback[F[_], I, G[_], A[_]](cb: F[I])(implicit t: I => StateAccess.ModState[G, A, S], F: Sync[F], G: Sync[G]): FromLensSetStateFn[S, T] =
        prepareF[F]((f, k) => F.flatMap(cb)(i => F.transSync(t(i).modStateOption(f, k))))

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

  /** @since 2.0.0 */
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
        apply(t(i).setStateOption(_, _))

      def readOnly: StateSnapshot[S] =
        apply(setFnReadOnly)
    }

    final class FromLensValue[S, T](l: Lens[S, T], value: T) {
      def apply(modify: ModFn[S]): StateSnapshot[T] =
        StateSnapshot(value)((ot, cb) => modify(l setO ot, cb))

      def tupled(modify: TupledModFn[S]): StateSnapshot[T] =
        apply(untuple(modify))

      def setStateVia[I](i: I)(implicit t: StateAccessor.WritePure[I, S]): StateSnapshot[T] =
        apply(t(i).modStateOption(_, _))
    }
  }

  // ███████████████████████████████████████████████████████████████████████████████████████████████████████████████████

  object HooksApiExt {
    sealed class Primary[Ctx, Step <: HooksApi.AbstractStep](api: HooksApi.Primary[Ctx, Step]) {
      final def useStateSnapshot[S](initialState: => S)(implicit step: Step): step.Next[StateSnapshot[S]] =
        useStateSnapshotBy(_ => initialState)

      final def useStateSnapshotBy[S](initialState: Ctx => S)(implicit step: Step): step.Next[StateSnapshot[S]] =
        api.customBy(ctx => StateSnapshot.hook(initialState(ctx)))

      final def useStateSnapshotWithReuse[S](initialState: => S)(implicit r: Reusability[S], step: Step): step.Next[StateSnapshot[S]] =
        useStateSnapshotWithReuseBy(_ => initialState)

      final def useStateSnapshotWithReuseBy[S](initialState: Ctx => S)(implicit r: Reusability[S], step: Step): step.Next[StateSnapshot[S]] =
        api.customBy(ctx => StateSnapshot.withReuse.hook(initialState(ctx)))
    }

    final class Secondary[Ctx, CtxFn[_], Step <: HooksApi.SubsequentStep[Ctx, CtxFn]](api: HooksApi.Secondary[Ctx, CtxFn, Step]) extends Primary[Ctx, Step](api) {
      def useStateSnapshotBy[S](initialState: CtxFn[S])(implicit step: Step): step.Next[StateSnapshot[S]] =
        useStateSnapshotBy(step.squash(initialState)(_))

      def useStateSnapshotWithReuseBy[S](initialState: CtxFn[S])(implicit r: Reusability[S], step: Step): step.Next[StateSnapshot[S]] =
        useStateSnapshotWithReuseBy(step.squash(initialState)(_))
    }
  }

  trait HooksApiExt {
    import HooksApiExt._

    implicit def hooksExtUseStateSnapshot1[Ctx, Step <: HooksApi.AbstractStep](api: HooksApi.Primary[Ctx, Step]): Primary[Ctx, Step] =
      new Primary(api)

    implicit def hooksExtUseStateSnapshot2[Ctx, CtxFn[_], Step <: HooksApi.SubsequentStep[Ctx, CtxFn]](api: HooksApi.Secondary[Ctx, CtxFn, Step]): Secondary[Ctx, CtxFn, Step] =
      new Secondary(api)
  }
}
