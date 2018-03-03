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

    def setState(newState: State, callback: Callback = Callback.empty): F[Unit]

    def modState(mod: State => State, callback: Callback = Callback.empty): F[Unit]

    /** @param callback Executed regardless of whether state changes. */
    def setStateOption(newState: Option[State], callback: Callback = Callback.empty): F[Unit]

    /** @param callback Executed regardless of whether state changes. */
    def modStateOption(mod: State => Option[State], callback: Callback = Callback.empty): F[Unit]

    final def setStateFn[I](f: I => State, callback: Callback = Callback.empty): I => F[Unit] =
      i => setState(f(i), callback)

    final def modStateFn[I](f: I => State => State, callback: Callback = Callback.empty): I => F[Unit] =
      i => modState(f(i), callback)

    /** @param callback Executed regardless of whether state changes. */
    final def setStateOptionFn[I](f: I => Option[State], callback: Callback = Callback.empty): I => F[Unit] =
      i => setStateOption(f(i), callback)

    /** @param callback Executed regardless of whether state changes. */
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

      override def setState(newState: State, callback: Callback = Callback.empty) =
        setStateOption(Some(newState), callback)

      override def modState(mod: State => State, callback: Callback = Callback.empty) =
        modStateOption(mod.andThen(Some(_)), callback)

      override def setStateOption(newState: Option[State], callback: Callback = Callback.empty) =
        setItFn(newState, callback)

      override def modStateOption(mod: State => Option[State], callback: Callback = Callback.empty) =
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