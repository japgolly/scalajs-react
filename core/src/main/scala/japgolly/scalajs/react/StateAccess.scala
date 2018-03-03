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

  /** Various methods to modify a component's state. */
  trait Write[F[_], State] extends Any {

    // Abstract

    /** @param callback Executed after state is changed. */
    def setState(newState: State, callback: Callback): F[Unit]

    /** @param callback Executed after state is changed. */
    def modState(mod: State => State, callback: Callback): F[Unit]

    /** @param callback Executed regardless of whether state is changed. */
    def setStateOption(newState: Option[State], callback: Callback): F[Unit]

    /** @param callback Executed regardless of whether state is changed. */
    def modStateOption(mod: State => Option[State], callback: Callback): F[Unit]

    // Concrete

    final def setState(newState: State): F[Unit] =
      setState(newState, Callback.empty)

    final def modState(mod: State => State): F[Unit] =
      modState(mod, Callback.empty)

    final def setStateOption(newState: Option[State]): F[Unit] =
      setStateOption(newState, Callback.empty)

    final def modStateOption(mod: State => Option[State]): F[Unit] =
      modStateOption(mod, Callback.empty)

    /** @param callback Executed after state is changed. */
    final def setStateFn[I](f: I => State, callback: Callback = Callback.empty): I => F[Unit] =
      i => setState(f(i), callback)

    /** @param callback Executed after state is changed. */
    final def modStateFn[I](f: I => State => State, callback: Callback = Callback.empty): I => F[Unit] =
      i => modState(f(i), callback)

    /** @param callback Executed regardless of whether state is changed. */
    final def setStateOptionFn[I](f: I => Option[State], callback: Callback = Callback.empty): I => F[Unit] =
      i => setStateOption(f(i), callback)

    /** @param callback Executed regardless of whether state is changed. */
    final def modStateOptionFn[I](f: I => State => Option[State], callback: Callback = Callback.empty): I => F[Unit] =
      i => modStateOption(f(i), callback)
  }

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

      override def setState(newState: State, callback: Callback) =
        setStateOption(Some(newState), callback)

      override def modState(mod: State => State, callback: Callback) =
        modStateOption(mod.andThen(Some(_)), callback)

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