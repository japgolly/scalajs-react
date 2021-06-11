package japgolly.scalajs.react

import japgolly.scalajs.react.internal.Lens
import japgolly.scalajs.react.util.UnsafeEffect
import japgolly.scalajs.react.util.DefaultEffects
import japgolly.scalajs.react.util.UnsafeEffect.Id
import japgolly.scalajs.react.util.SafeEffect

/**
  * Base class for something that has read/write state access (under the same effect type).
  *
  * Passing this around (top-level) is fine but do not use it in a generic/library/helper method.
  * In intermediary positions, use [[StateAccessor]] instead.
  *
  * @tparam F The type of effect when accessing state.
  * @tparam S State type.
  */
trait StateAccess[F[_], A[_], S] extends StateAccess.Write[F, A, S] {
  final type State = S

  def state: F[State]

  type WithMappedState[S2] <: StateAccess[F, A, S2]
  def xmapState[S2](f: S => S2)(g: S2 => S): WithMappedState[S2]
  def zoomState[S2](get: S => S2)(set: S2 => S => S): WithMappedState[S2]

  type WithEffect[F2[_]] <: StateAccess[F2, A, S]
  def withEffect[F2[_]](implicit t: UnsafeEffect.Sync.Trans[F, F2]): WithEffect[F2]
  final def withEffectsPure(implicit t: UnsafeEffect.Sync.Trans[F, DefaultEffects.Sync]): WithEffect[DefaultEffects.Sync] = withEffect
  final def withEffectsImpure(implicit t: UnsafeEffect.Sync.Trans[F, Id]): WithEffect[Id] = withEffect

  type WithAsyncEffect[A2[_]] <: StateAccess[F, A2, S]
  def withAsyncEffect[A2[_]](implicit t: SafeEffect.Async.Trans[A, A2]): WithAsyncEffect[A2]
}

object StateAccess {

  trait Base[F[_], A[_]] extends Any {
    protected implicit def F: UnsafeEffect.Sync[F]
    protected implicit def A: SafeEffect.Async[A]

    final protected def async(f: SafeEffect.Sync.Untyped[Any] => F[Unit]): A[Unit] =
      A.async_(r => F.toJsFn0(f(r)))
  }

  trait SetState[F[_], A[_], S] extends Any with Base[F, A] {

    final def setState(newState: S): F[Unit] =
      setState(newState, SafeEffect.Sync.empty)

    /** @param callback Executed after state is changed. */
    def setState[G[_], B](newState: S, callback: => G[B])(implicit G: SafeEffect.Sync[G]): F[Unit] =
      setStateOption(Some(newState), callback)

    final def setStateOption(newState: Option[S]): F[Unit] =
      setStateOption(newState, SafeEffect.Sync.empty)

    /** @param callback Executed regardless of whether state is changed. */
    def setStateOption[G[_], B](newState: Option[S], callback: => G[B])(implicit G: SafeEffect.Sync[G]): F[Unit]

    def toSetStateFn: SetStateFn[F, A, S] =
      SetStateFn(setStateOption(_, _))

    final def setStateAsync(newState: S): A[Unit] =
      async(setState(newState, _))

    final def setStateOptionAsync(newState: Option[S]): A[Unit] =
      async(setStateOption(newState, _))
  }

  trait ModState[F[_], A[_], S] extends Any with Base[F, A] {

    final def modState(mod: S => S): F[Unit] =
      modState(mod, SafeEffect.Sync.empty)

    /** @param callback Executed after state is changed. */
    def modState[G[_], B](mod: S => S, callback: => G[B])(implicit G: SafeEffect.Sync[G]): F[Unit] =
      modStateOption(mod.andThen(Some(_)), callback)

    final def modStateOption(mod: S => Option[S]): F[Unit] =
      modStateOption(mod, SafeEffect.Sync.empty)

    /** @param callback Executed regardless of whether state is changed. */
    def modStateOption[G[_], B](mod: S => Option[S], callback: => G[B])(implicit G: SafeEffect.Sync[G]): F[Unit]

    def toModStateFn: ModStateFn[F, A, S] =
      ModStateFn(modStateOption(_, _))

    final def modStateAsync(mod: S => S): A[Unit] =
      async(modState(mod, _))

    final def modStateOptionAsync(mod: S => Option[S]): A[Unit] =
      async(modStateOption(mod, _))
  }

  trait ModStateWithProps[F[_], A[_], P, S] extends Any with Base[F, A] {

    final def modState(mod: (S, P) => S): F[Unit] =
      modState(mod, SafeEffect.Sync.empty)

    /** @param callback Executed after state is changed. */
    def modState[G[_], B](mod: (S, P) => S, callback: => G[B])(implicit G: SafeEffect.Sync[G]): F[Unit] =
      modStateOption((s, p) => Some(mod(s, p)), callback)

    final def modStateOption(mod: (S, P) => Option[S]): F[Unit] =
      modStateOption(mod, SafeEffect.Sync.empty)

    /** @param callback Executed regardless of whether state is changed. */
    def modStateOption[G[_], B](mod: (S, P) => Option[S], callback: => G[B])(implicit G: SafeEffect.Sync[G]): F[Unit]

    def toModStateWithPropsFn: ModStateWithPropsFn[F, A, P, S] =
      ModStateWithPropsFn(modStateOption(_, _))

    final def modStateAsync(mod: (S, P) => S): A[Unit] =
      async(modState(mod, _))

    final def modStateOptionAsync(mod: (S, P) => Option[S]): A[Unit] =
      async(modStateOption(mod, _))
  }

  // ===================================================================================================================

  trait Write[F[_], A[_], S] extends Any with SetState[F, A, S] with ModState[F, A, S]

  trait WriteWithProps[F[_], A[_], P, S] extends Any with Write[F, A, S] with ModStateWithProps[F, A, P, S]

  // ===================================================================================================================

  /** For testing. */
  def apply[F[_], A[_], S](stateFn: => F[S])
                          (setItFn: (Option[S], SafeEffect.Sync.Untyped[Any]) => F[Unit],
                           modItFn: ((S => Option[S]), SafeEffect.Sync.Untyped[Any]) => F[Unit])
                          (implicit FF: UnsafeEffect.Sync[F], AA: SafeEffect.Async[A]): StateAccess[F, A, S] =
    new StateAccess[F, A, S] {
      override type WithEffect[F2[_]] = StateAccess[F2, A, S]
      override type WithAsyncEffect[A2[_]] = StateAccess[F, A2, S]
      override type WithMappedState[S2] = StateAccess[F, A, S2]

      override protected implicit def F = FF
      override protected implicit def A = AA

      override def state = stateFn

      override def setStateOption[G[_], B](newState: Option[State], callback: => G[B])(implicit G: SafeEffect.Sync[G]) =
        setItFn(newState, G.toJsFn0(callback))

      override def modStateOption[G[_], B](mod: State => Option[State], callback: => G[B])(implicit G: SafeEffect.Sync[G]) =
        modItFn(mod, G.toJsFn0(callback))

      override def xmapState[S2](f: S => S2)(g: S2 => S) =
        apply(
          F.map(stateFn)(f))(
          (s, c) => setItFn(s map g, c),
          (m, c) => modItFn(s => m(f(s)) map g, c))(
          FF, AA)

      override def zoomState[S2](get: S => S2)(set: S2 => S => S) = {
        val l = Lens(get)(set)
        apply(
          F.map(stateFn)(get))(
          (s, c) => modItFn(l setO s, c),
          (m, c) => modItFn(l modO m, c))(
          FF, AA)
      }

      override def withEffect[F2[_]](implicit t: UnsafeEffect.Sync.Trans[F, F2]) =
        apply(
          t(stateFn))(
          (s, c) => t(setItFn(s, c)),
          (f, c) => t(modItFn(f, c)))(
          t.to, A)

      override def withAsyncEffect[A2[_]](implicit t: SafeEffect.Async.Trans[A, A2]) =
        apply(stateFn)(setItFn, modItFn)(F, t.to)
    }

  def const[F[_], A[_], S](stateFn: => F[S])(implicit FF: UnsafeEffect.Sync[F], AA: SafeEffect.Async[A]): StateAccess[F, A, S] =
    new StateAccess[F, A, S] {
      override type WithEffect[F2[_]] = StateAccess[F2, A, S]
      override type WithAsyncEffect[A2[_]] = StateAccess[F, A2, S]
      override type WithMappedState[S2] = StateAccess[F, A, S2]

      override protected implicit def F = FF
      override protected implicit def A = AA

      override def state = stateFn

      override def setStateOption[G[_], B](newState: Option[State], callback: => G[B])(implicit G: SafeEffect.Sync[G]) =
        F.delay(G.runSync(callback))

      override def modStateOption[G[_], B](mod: State => Option[State], callback: => G[B])(implicit G: SafeEffect.Sync[G]) =
        F.delay(G.runSync(callback))

      override def xmapState[S2](f: S => S2)(g: S2 => S) =
        const(F.map(stateFn)(f))(FF, AA)

      override def zoomState[S2](get: S => S2)(set: S2 => S => S) =
        const(F.map(stateFn)(get))(FF, AA)

      override def withEffect[F2[_]](implicit t: UnsafeEffect.Sync.Trans[F, F2]) =
        const(t(stateFn))(t.to, A)

      override def withAsyncEffect[A2[_]](implicit t: SafeEffect.Async.Trans[A, A2]) =
        const(stateFn)(F, t.to)
   }
}