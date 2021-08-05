package japgolly.scalajs.react

import japgolly.scalajs.react.internal.Lens
import japgolly.scalajs.react.util.DefaultEffects.{Sync => DS}
import japgolly.scalajs.react.util.Effect._

/** Base class for something that has read/write state access (under the same effect type).
  *
  * Passing this around (top-level) is fine but do not use it in a generic/library/helper method.
  * In intermediary positions, use [[StateAccessor]] instead.
  *
  * @tparam F The type of effect when accessing state.
  * @tparam S State type.
  */
trait StateAccess[F[_], A[_], S] extends StateAccess.Write[F, A, S] {
  override type WithEffect     [G[_]] <: StateAccess[G, A, S]
  override type WithAsyncEffect[G[_]] <: StateAccess[F, G, S]

  final type State = S

  def state: F[State]

  type WithMappedState[S2] <: StateAccess[F, A, S2]
  def xmapState[S2](f: S => S2)(g: S2 => S): WithMappedState[S2]
  def zoomState[S2](get: S => S2)(set: S2 => S => S): WithMappedState[S2]
}

object StateAccess {

  // ===================================================================================================================
  trait Base[F[_], A[_]] extends Any {
    protected implicit def F: UnsafeSync[F]
    protected implicit def A: Async[A]

    final protected def async(f: DS[Unit] => F[Unit]): A[Unit] =
      A.async_(r => F.toJsFn(f(DS.fromJsFn0(r))))

    type WithEffect     [G[_]] <: Base[G, A]
    type WithAsyncEffect[G[_]] <: Base[F, G]

    def withEffect[G[_]](implicit G: UnsafeSync[G]): WithEffect[G]
    def withAsyncEffect[G[_]](implicit G: Async[G]): WithAsyncEffect[G]

    final def withEffectsPure: WithEffect[DS] = withEffect
    final def withEffectsImpure: WithEffect[Id] = withEffect
  }

  // ===================================================================================================================
  trait SetState[F[_], A[_], S] extends Any with Base[F, A] {
    override type WithEffect     [G[_]] <: SetState[G, A, S]
    override type WithAsyncEffect[G[_]] <: SetState[F, G, S]

    final def setState(newState: S): F[Unit] =
      setState(newState, DS.empty)

    /** @param callback Executed after state is changed. */
    def setState[G[_]](newState: S, callback: => G[Unit])(implicit G: Dispatch[G]): F[Unit] =
      setStateOption(Some(newState), callback)

    final def setStateOption(newState: Option[S]): F[Unit] =
      setStateOption(newState, DS.empty)

    /** @param callback Executed regardless of whether state is changed. */
    def setStateOption[G[_]](newState: Option[S], callback: => G[Unit])(implicit G: Dispatch[G]): F[Unit]

    def toSetStateFn: SetStateFn[F, A, S] =
      SetStateFn((o, f) => setStateOption(o, f))

    final def setStateAsync(newState: S): A[Unit] =
      async(setState(newState, _))

    final def setStateOptionAsync(newState: Option[S]): A[Unit] =
      async(setStateOption(newState, _))
  }

  // ===================================================================================================================
  trait ModState[F[_], A[_], S] extends Any with Base[F, A] {
    override type WithEffect     [G[_]] <: ModState[G, A, S]
    override type WithAsyncEffect[G[_]] <: ModState[F, G, S]

    final def modState(mod: S => S): F[Unit] =
      modState(mod, DS.empty)

    /** @param callback Executed after state is changed. */
    def modState[G[_]](mod: S => S, callback: => G[Unit])(implicit G: Dispatch[G]): F[Unit] =
      modStateOption(mod.andThen(Some(_)), callback)

    final def modStateOption(mod: S => Option[S]): F[Unit] =
      modStateOption(mod, DS.empty)

    /** @param callback Executed regardless of whether state is changed. */
    def modStateOption[G[_]](mod: S => Option[S], callback: => G[Unit])(implicit G: Dispatch[G]): F[Unit]

    def toModStateFn: ModStateFn[F, A, S] =
      ModStateFn((o, f) => modStateOption(o, f))

    final def modStateAsync(mod: S => S): A[Unit] =
      async(modState(mod, _))

    final def modStateOptionAsync(mod: S => Option[S]): A[Unit] =
      async(modStateOption(mod, _))
  }

  // ===================================================================================================================
  trait ModStateWithProps[F[_], A[_], P, S] extends Any with Base[F, A] {
    override type WithEffect     [G[_]] <: ModStateWithProps[G, A, P, S]
    override type WithAsyncEffect[G[_]] <: ModStateWithProps[F, G, P, S]

    final def modState(mod: (S, P) => S): F[Unit] =
      modState(mod, DS.empty)

    /** @param callback Executed after state is changed. */
    def modState[G[_]](mod: (S, P) => S, callback: => G[Unit])(implicit G: Dispatch[G]): F[Unit] =
      modStateOption((s, p) => Some(mod(s, p)), callback)

    final def modStateOption(mod: (S, P) => Option[S]): F[Unit] =
      modStateOption(mod, DS.empty)

    /** @param callback Executed regardless of whether state is changed. */
    def modStateOption[G[_]](mod: (S, P) => Option[S], callback: => G[Unit])(implicit G: Dispatch[G]): F[Unit]

    def toModStateWithPropsFn: ModStateWithPropsFn[F, A, P, S] =
      ModStateWithPropsFn((o, f) => modStateOption(o, f))

    final def modStateAsync(mod: (S, P) => S): A[Unit] =
      async(modState(mod, _))

    final def modStateOptionAsync(mod: (S, P) => Option[S]): A[Unit] =
      async(modStateOption(mod, _))
  }

  // ===================================================================================================================

  trait Write[F[_], A[_], S] extends Any with SetState[F, A, S] with ModState[F, A, S] {
    override type WithEffect     [G[_]] <: Write[G, A, S]
    override type WithAsyncEffect[G[_]] <: Write[F, G, S]
  }

  trait WriteWithProps[F[_], A[_], P, S] extends Any with Write[F, A, S] with ModStateWithProps[F, A, P, S] {
    override type WithEffect     [G[_]] <: WriteWithProps[G, A, P, S]
    override type WithAsyncEffect[G[_]] <: WriteWithProps[F, G, P, S]
  }

  // ===================================================================================================================

  /** For testing. */
  def apply[F[_], A[_], S](stateFn: => F[S])
                          (setItFn: (Option[S], => F[Unit]) => F[Unit],
                           modItFn: ((S => Option[S]), => F[Unit]) => F[Unit])
                          (implicit FF: UnsafeSync[F], AA: Async[A]): StateAccess[F, A, S] =
    new StateAccess[F, A, S] {
      override type WithEffect     [G[_]] = StateAccess[G, A, S]
      override type WithAsyncEffect[G[_]] = StateAccess[F, G, S]
      override type WithMappedState[T]    = StateAccess[F, A, T]

      override protected implicit def F = FF
      override protected implicit def A = AA

      override def state = stateFn

      override def setStateOption[G[_]](newState: Option[State], callback: => G[Unit])(implicit G: Dispatch[G]) =
        setItFn(newState, F.transDispatch(callback))

      override def modStateOption[G[_]](mod: State => Option[State], callback: => G[Unit])(implicit G: Dispatch[G]) =
        modItFn(mod, F.transDispatch(callback))

      override def xmapState[S2](f: S => S2)(g: S2 => S) =
        apply(
          F.map(stateFn)(f))(
          (s, c) => setItFn(s map g, c),
          (m, c) => modItFn(s => m(f(s)) map g, c))(
          F, A)

      override def zoomState[S2](get: S => S2)(set: S2 => S => S) = {
        val l = Lens(get)(set)
        apply(
          F.map(stateFn)(get))(
          (s, c) => modItFn(l setO s, c),
          (m, c) => modItFn(l modO m, c))(
          F, A)
      }

      override def withEffect[F2[_]](implicit t: UnsafeSync[F2]) =
        apply(
          t.transSync(stateFn)(F))(
          (s, c) => t.transSync(setItFn(s, F.transSync(c)))(F),
          (f, c) => t.transSync(modItFn(f, F.transSync(c)))(F))(
          t, A)

      override def withAsyncEffect[A2[_]](implicit t: Async[A2]) =
        apply(stateFn)(setItFn, modItFn)(F, t)
    }

  def const[F[_], A[_], S](stateFn: => F[S])(implicit FF: UnsafeSync[F], AA: Async[A]): StateAccess[F, A, S] =
    new StateAccess[F, A, S] {
      override type WithEffect     [G[_]] = StateAccess[G, A, S]
      override type WithAsyncEffect[G[_]] = StateAccess[F, G, S]
      override type WithMappedState[T]    = StateAccess[F, A, T]

      override protected implicit def F = FF
      override protected implicit def A = AA

      override def state = stateFn

      override def setStateOption[G[_]](newState: Option[State], callback: => G[Unit])(implicit G: Dispatch[G]) =
        F.transDispatch(callback)

      override def modStateOption[G[_]](mod: State => Option[State], callback: => G[Unit])(implicit G: Dispatch[G]) =
        F.transDispatch(callback)

      override def xmapState[S2](f: S => S2)(g: S2 => S) =
        const(F.map(stateFn)(f))(F, AA)

      override def zoomState[S2](get: S => S2)(set: S2 => S => S) =
        const(F.map(stateFn)(get))(F, AA)

      override def withEffect[F2[_]](implicit t: UnsafeSync[F2]) =
        const(t.transSync(stateFn)(F))(t, A)

      override def withAsyncEffect[A2[_]](implicit t: Async[A2]) =
        const(stateFn)(F, t)
   }
}
