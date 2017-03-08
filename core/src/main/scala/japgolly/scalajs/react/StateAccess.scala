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
trait StateAccess[F[_], S] {
  final type State = S

  protected implicit def F: Effect[F]

  type WithMappedState[S2] <: StateAccess[F, S2]
  def xmapState[S2](f: S => S2)(g: S2 => S): WithMappedState[S2]
  def zoomState[S2](get: S => S2)(set: S2 => S => S): WithMappedState[S2]

  def state: F[State]
  def setState(newState: State, callback: Callback = Callback.empty): F[Unit]
  def modState(mod: State => State, callback: Callback = Callback.empty): F[Unit]

  @deprecated("Renamed to setStateFn", "1.0.0")
  final def _setState[I](f: I => State, callback: Callback = Callback.empty): I => F[Unit] =
    setStateFn(f, callback)

  @deprecated("Renamed to modStateFn", "1.0.0")
  final def _modState[I](f: I => State => State, callback: Callback = Callback.empty): I => F[Unit] =
    modStateFn(f, callback)

  final def setStateFn[I](f: I => State, callback: Callback = Callback.empty): I => F[Unit] =
    i => setState(f(i), callback)

  final def modStateFn[I](f: I => State => State, callback: Callback = Callback.empty): I => F[Unit] =
    i => modState(f(i), callback)

  type WithEffect[F2[_]] <: StateAccess[F2, S]
  def withEffect[F2[_]](implicit t: Effect.Trans[F, F2]): WithEffect[F2]
  final def withEffectsPure(implicit t: Effect.Trans[F, CallbackTo]): WithEffect[CallbackTo] = withEffect
  final def withEffectsImpure(implicit t: Effect.Trans[F, Effect.Id]): WithEffect[Effect.Id] = withEffect
}

object StateAccess {

  /** For testing. */
  def apply[F[_], S](stateFn: => F[S])
                    (setItFn: (S, Callback) => F[Unit],
                     modItFn: ((S => S), Callback) => F[Unit])
                    (implicit FF: Effect[F]): StateAccess[F, S] =
    new StateAccess[F, S] {
      override type WithEffect[F2[_]] = StateAccess[F2, S]
      override type WithMappedState[S2] = StateAccess[F, S2]

      override protected implicit def F = FF

      override def state = stateFn

      override def setState(newState: State, callback: Callback = Callback.empty) =
        setItFn(newState, callback)

      override def modState(mod: State => State, callback: Callback = Callback.empty) =
        modItFn(mod, callback)

      override def xmapState[S2](f: S => S2)(g: S2 => S) =
        apply(
          F.map(stateFn)(f))(
          (s, c) => setItFn(g(s), c),
          (m, c) => modItFn(g compose m compose f, c))(
          FF)

      override def zoomState[S2](get: S => S2)(set: S2 => S => S) = {
        val l = Lens(get)(set)
        apply(
          F.map(stateFn)(get))(
          (s, c) => modItFn(l set s, c),
          (m, c) => modItFn(l mod m, c))(
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