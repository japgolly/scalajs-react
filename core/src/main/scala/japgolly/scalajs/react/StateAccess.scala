package japgolly.scalajs.react

import japgolly.scalajs.react.internal.{Effect, Lens}

/**
  * Base class for something that has read/write state access (under the same effect type).
  *
  * Passing this around (top-level) is fine but do not use it in a generic/library/helper method.
  * In intermediary positions, use [[StateAccessor]] instead.
  *
  * @tparam F The type of effect when accessing state.
  * @tparam S State type.
  */
trait StateAccess[F[_], S] extends StateAccess.Write[F, S] {
  final type State = S

  protected implicit def F: Effect[F]

  def state: F[State]

  type WithMappedState[S2] <: StateAccess[F, S2]
  def xmapState[S2](f: S => S2)(g: S2 => S): WithMappedState[S2]
  def zoomState[S2](get: S => S2)(set: S2 => S => S): WithMappedState[S2]

  type WithEffect[F2[_]] <: StateAccess[F2, S]
  def withEffect[F2[_]](implicit t: Effect.Trans[F, F2]): WithEffect[F2]
  final def withEffectsPure(implicit t: Effect.Trans[F, CallbackTo]): WithEffect[CallbackTo] = withEffect
  final def withEffectsImpure(implicit t: Effect.Trans[F, Effect.Id]): WithEffect[Effect.Id] = withEffect
}

object StateAccess {

  trait SetState[F[_], S] extends Any {

    final def setState(newState: S): F[Unit] =
      setState(newState, Callback.empty)

    /** @param callback Executed after state is changed. */
    def setState(newState: S, callback: Callback): F[Unit] =
      setStateOption(Some(newState))

    final def setStateOption(newState: Option[S]): F[Unit] =
      setStateOption(newState, Callback.empty)

    /** @param callback Executed regardless of whether state is changed. */
    def setStateOption(newState: Option[S], callback: Callback): F[Unit]

    @deprecated("Create a function yourself. If you're using this in Reusable.fn(…), use Reusable.fn.state(this.zoom…).set instead", "1.2.0")
    final def setStateFn[I](f: I => S, callback: Callback = Callback.empty): I => F[Unit] =
      i => setState(f(i), callback)

    @deprecated("Create a function yourself. If you're using this in Reusable.fn(…), use Reusable.fn.state(this.zoom…).setOption instead", "1.2.0")
    final def setStateOptionFn[I](f: I => Option[S], callback: Callback = Callback.empty): I => F[Unit] =
      i => setStateOption(f(i), callback)

    def toSetStateFn: SetStateFn[F, S] =
      SetStateFn(setStateOption)
  }

  trait ModState[F[_], S] extends Any {

    final def modState(mod: S => S): F[Unit] =
      modState(mod, Callback.empty)

    /** @param callback Executed after state is changed. */
    def modState(mod: S => S, callback: Callback): F[Unit] =
      modStateOption(mod.andThen(Some(_)), callback)

    final def modStateOption(mod: S => Option[S]): F[Unit] =
      modStateOption(mod, Callback.empty)

    /** @param callback Executed regardless of whether state is changed. */
    def modStateOption(mod: S => Option[S], callback: Callback): F[Unit]

    @deprecated("Create a function yourself. If you're using this in Reusable.fn(…), use Reusable.fn.state(this.zoom…).mod instead", "1.2.0")
    final def modStateFn[I](f: I => S => S, callback: Callback = Callback.empty): I => F[Unit] =
      i => modState(f(i), callback)

    @deprecated("Create a function yourself. If you're using this in Reusable.fn(…), use Reusable.fn.state(this.zoom…).modOption instead", "1.2.0")
    final def modStateOptionFn[I](f: I => S => Option[S], callback: Callback = Callback.empty): I => F[Unit] =
      i => modStateOption(f(i), callback)

    def toModStateFn: ModStateFn[F, S] =
      ModStateFn(modStateOption)
  }

  trait ModStateWithProps[F[_], P, S] extends Any {

    final def modState(mod: (S, P) => S): F[Unit] =
      modState(mod, Callback.empty)

    /** @param callback Executed after state is changed. */
    def modState(mod: (S, P) => S, callback: Callback): F[Unit] =
      modStateOption((s, p) => Some(mod(s, p)), callback)

    final def modStateOption(mod: (S, P) => Option[S]): F[Unit] =
      modStateOption(mod, Callback.empty)

    /** @param callback Executed regardless of whether state is changed. */
    def modStateOption(mod: (S, P) => Option[S], callback: Callback): F[Unit]

    def toModStateWithPropsFn: ModStateWithPropsFn[F, P, S] =
      ModStateWithPropsFn(modStateOption)
  }

  // ===================================================================================================================

  trait Write[F[_], S] extends Any with SetState[F, S] with ModState[F, S]

  trait WriteWithProps[F[_], P, S] extends Any with Write[F, S] with ModStateWithProps[F, P, S]

  // ===================================================================================================================

  /** For testing. */
  def apply[F[_], S](stateFn: => F[S])
                    (setItFn: (Option[S], Callback) => F[Unit],
                     modItFn: ((S => Option[S]), Callback) => F[Unit])
                    (implicit FF: Effect[F]): StateAccess[F, S] =
    new StateAccess[F, S] {
      override type WithEffect[F2[_]] = StateAccess[F2, S]
      override type WithMappedState[S2] = StateAccess[F, S2]

      override protected implicit def F = FF

      override def state = stateFn

      override def setStateOption(newState: Option[State], callback: Callback) =
        setItFn(newState, callback)

      override def modStateOption(mod: State => Option[State], callback: Callback) =
        modItFn(mod, callback)

      override def xmapState[S2](f: S => S2)(g: S2 => S) =
        apply(
          F.map(stateFn)(f))(
          (s, c) => setItFn(s map g, c),
          (m, c) => modItFn(s => m(f(s)) map g, c))(
          FF)

      override def zoomState[S2](get: S => S2)(set: S2 => S => S) = {
        val l = Lens(get)(set)
        apply(
          F.map(stateFn)(get))(
          (s, c) => modItFn(l setO s, c),
          (m, c) => modItFn(l modO m, c))(
          FF)
      }

      override def withEffect[F2[_]](implicit t: Effect.Trans[F, F2]) =
        apply(
          t(stateFn))(
          (s, c) => t(setItFn(s, c)),
          (f, c) => t(modItFn(f, c)))(
          t.to)
    }
}