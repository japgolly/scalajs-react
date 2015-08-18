package japgolly.scalajs.react

/**
 * Generic read & write access to a component's state, (whatever the type of state might be).
 */
abstract class CompStateAccess[-C, S] {
  def state(c: C): S
  def setState(c: C, s: S, cb: Callback): Callback
}

object CompStateAccess {

  /**
   * This is a hack to avoid creating new instances for each type of state.
   */
  abstract class HK[K[_]] extends CompStateAccess[K[Any], Any] {
    final type S = Any
    final type C = K[S]
    @inline final def force[S]: CompStateAccess[K[S], S] =
      this.asInstanceOf[CompStateAccess[K[S], S]]
  }

  object Focus extends HK[CompStateFocus] {
    override def state(c: C)                        = c.get()
    override def setState(c: C, s: S, cb: Callback) = c.set(s, cb)
  }

  object SS extends HK[ComponentScope_SS] {
    override def state(c: C)                        = c._state.v
    override def setState(c: C, s: S, cb: Callback) = Callback(c._setState(WrapObj(s), cb.toJsCallback))
  }

  @inline implicit def focus[S]: CompStateAccess[CompStateFocus[S], S] =
    Focus.force

  @inline implicit def cm[P, S, B, N <: TopNode]: CompStateAccess[ComponentScopeM[P, S, B, N], S] =
    CompStateAccess.SS.force[S]

  @inline implicit def cu[P, S, B]: CompStateAccess[ComponentScopeU[P, S, B], S] =
    CompStateAccess.SS.force[S]

  @inline implicit def bs[P, S]: CompStateAccess[BackendScope[P, S], S] =
    CompStateAccess.SS.force[S]

  final class Ops[C, S](private val _c: C) extends AnyVal {
    // This should really be a class param but then we lose the AnyVal
    type CC = CompStateAccess[C, S]

    @inline def state(implicit C: CC): S =
      C.state(_c)

    /**
     * Creates a callback that always returns the latest state when run.
     */
    def stateCB(implicit C: CC): CallbackTo[S] =
      CallbackTo(state)

    def setState(s: S, cb: Callback = Callback.empty)(implicit C: CC): Callback =
      C.setState(_c, s, cb)

    def setStateCB(s: CallbackTo[S], cb: Callback = Callback.empty)(implicit C: CC): Callback =
      s >>= (setState(_, cb))

    def _setState[I](f: I => S, cb: Callback = Callback.empty)(implicit C: CC): I => Callback =
      i => C.setState(_c, f(i), cb)

    def modState(f: S => S, cb: Callback = Callback.empty)(implicit C: CC): Callback =
      stateCB >>= (s => setState(f(s), cb))
      //Callback lazily setState(f(state), cb)

    def modStateCB(f: S => CallbackTo[S], cb: Callback = Callback.empty)(implicit C: CC): Callback =
      stateCB >>= (s => setStateCB(f(s), cb))

    def _modState[I](f: I => S => S, cb: Callback = Callback.empty)(implicit C: CC): I => Callback =
      i => modState(f(i), cb)

    def lift(implicit C: CC) = new CompStateFocus[S](
      () => _c.state,
      (a: S, cb: Callback) => _c.setState(a, cb))

    /** Zoom-in on a subset of the state. */
    def zoom[T](f: S => T)(g: (S, T) => S)(implicit C: CC) = new CompStateFocus[T](
      () => f(_c.state),
      (b: T, cb: Callback) => _c.setState(g(_c.state, b), cb))
  }
}

/**
 * Read & write access to a specific subset of a specific component's state.
 *
 * @tparam S The type of state.
 */
final class CompStateFocus[S] private[react](val get: () => S,
                                             val set: (S, Callback) => Callback)

object CompStateFocus {
  @inline def apply[S](get: () => S)(set: (S, Callback) => Callback): CompStateFocus[S] =
    new CompStateFocus(get, set)
}
