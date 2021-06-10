package japgolly.scalajs.react

import japgolly.scalajs.react.internal.Lens
import japgolly.scalajs.react.util.Effect
import japgolly.scalajs.react.util.SafeEffect.Sync

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

  def state: F[State]

  type WithMappedState[S2] <: StateAccess[F, S2]
  def xmapState[S2](f: S => S2)(g: S2 => S): WithMappedState[S2]
  def zoomState[S2](get: S => S2)(set: S2 => S => S): WithMappedState[S2]

  type WithEffect[F2[_]] <: StateAccess[F2, S]
  def withEffect[F2[_]](implicit t: Effect.Trans[F, F2]): WithEffect[F2]
  // TODO: FX final def withEffectsPure(implicit t: Effect.Trans[F, CallbackTo]): WithEffect[CallbackTo] = withEffect
  final def withEffectsImpure(implicit t: Effect.Trans[F, Effect.Id]): WithEffect[Effect.Id] = withEffect
}

object StateAccess {

  trait Base[F[_]] extends Any {
    protected implicit def F: Effect[F]

    // TODO: FX final protected def async(f: Callback => F[Unit]): AsyncCallback[Unit] =
    //   AsyncCallback.viaCallback(cb => F.toCallback(f(cb)))
  }

  trait SetState[F[_], S] extends Any with Base[F] {

    final def setState(newState: S): F[Unit] =
      setState(newState, Sync.empty)

    /** @param callback Executed after state is changed. */
    def setState[G[_], A](newState: S, callback: => G[A])(implicit G: Sync[G]): F[Unit] =
      setStateOption(Some(newState), callback)

    final def setStateOption(newState: Option[S]): F[Unit] =
      setStateOption(newState, Sync.empty)

    /** @param callback Executed regardless of whether state is changed. */
    def setStateOption[G[_], A](newState: Option[S], callback: => G[A])(implicit G: Sync[G]): F[Unit]

    def toSetStateFn: SetStateFn[F, S] =
      SetStateFn(setStateOption(_, _))

    final def setStateAsync(newState: S): AsyncCallback[Unit] =
      async(setState(newState, _))

    final def setStateOptionAsync(newState: Option[S]): AsyncCallback[Unit] =
      async(setStateOption(newState, _))
  }

  trait ModState[F[_], S] extends Any with Base[F] {

    final def modState(mod: S => S): F[Unit] =
      modState(mod, Sync.empty)

    /** @param callback Executed after state is changed. */
    def modState[G[_], A](mod: S => S, callback: => G[A])(implicit G: Sync[G]): F[Unit] =
      modStateOption(mod.andThen(Some(_)), callback)

    final def modStateOption(mod: S => Option[S]): F[Unit] =
      modStateOption(mod, Sync.empty)

    /** @param callback Executed regardless of whether state is changed. */
    def modStateOption[G[_], A](mod: S => Option[S], callback: => G[A])(implicit G: Sync[G]): F[Unit]

    def toModStateFn: ModStateFn[F, S] =
      ModStateFn(modStateOption(_, _))

    final def modStateAsync(mod: S => S): AsyncCallback[Unit] =
      async(modState(mod, _))

    final def modStateOptionAsync(mod: S => Option[S]): AsyncCallback[Unit] =
      async(modStateOption(mod, _))
  }

  trait ModStateWithProps[F[_], P, S] extends Any with Base[F] {

    final def modState(mod: (S, P) => S): F[Unit] =
      modState(mod, Sync.empty)

    /** @param callback Executed after state is changed. */
    def modState[G[_], A](mod: (S, P) => S, callback: => G[A])(implicit G: Sync[G]): F[Unit] =
      modStateOption((s, p) => Some(mod(s, p)), callback)

    final def modStateOption(mod: (S, P) => Option[S]): F[Unit] =
      modStateOption(mod, Sync.empty)

    /** @param callback Executed regardless of whether state is changed. */
    def modStateOption[G[_], A](mod: (S, P) => Option[S], callback: => G[A])(implicit G: Sync[G]): F[Unit]

    def toModStateWithPropsFn: ModStateWithPropsFn[F, P, S] =
      ModStateWithPropsFn(modStateOption(_, _))

    final def modStateAsync(mod: (S, P) => S): AsyncCallback[Unit] =
      async(modState(mod, _))

    final def modStateOptionAsync(mod: (S, P) => Option[S]): AsyncCallback[Unit] =
      async(modStateOption(mod, _))
  }

  // ===================================================================================================================

  trait Write[F[_], S] extends Any with SetState[F, S] with ModState[F, S]

  trait WriteWithProps[F[_], P, S] extends Any with Write[F, S] with ModStateWithProps[F, P, S]

  // ===================================================================================================================

  /** For testing. */
  def apply[F[_], S](stateFn: => F[S])
                    (setItFn: (Option[S], Sync.RawCallback) => F[Unit],
                     modItFn: ((S => Option[S]), Sync.RawCallback) => F[Unit])
                    (implicit FF: Effect[F]): StateAccess[F, S] =
    new StateAccess[F, S] {
      override type WithEffect[F2[_]] = StateAccess[F2, S]
      override type WithMappedState[S2] = StateAccess[F, S2]

      override protected implicit def F = FF

      override def state = stateFn

      override def setStateOption[G[_], A](newState: Option[State], callback: => G[A])(implicit G: Sync[G]) =
        setItFn(newState, G.syncJsFn0(callback))

      override def modStateOption[G[_], A](mod: State => Option[State], callback: => G[A])(implicit G: Sync[G]) =
        modItFn(mod, G.syncJsFn0(callback))

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

  def const[F[_], S](stateFn: => F[S])(implicit FF: Effect[F]): StateAccess[F, S] =
    new StateAccess[F, S] {
      override type WithEffect[F2[_]] = StateAccess[F2, S]
      override type WithMappedState[S2] = StateAccess[F, S2]

      override protected implicit def F = FF

      override def state = stateFn

      override def setStateOption[G[_], A](newState: Option[State], callback: => G[A])(implicit G: Sync[G]) =
        F.point(G.syncRun(callback))

      override def modStateOption[G[_], A](mod: State => Option[State], callback: => G[A])(implicit G: Sync[G]) =
        F.point(G.syncRun(callback))

      override def xmapState[S2](f: S => S2)(g: S2 => S) =
        const(F.map(stateFn)(f))(FF)

      override def zoomState[S2](get: S => S2)(set: S2 => S => S) =
        const(F.map(stateFn)(get))(FF)

      override def withEffect[F2[_]](implicit t: Effect.Trans[F, F2]) =
        const(t(stateFn))(t.to)
    }
}