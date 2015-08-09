package japgolly.scalajs.react

import scala.scalajs.js.{Function => JFn, undefined}

/**
 * Generic read & write access to a component's state, (whatever the type of state might be).
 */
abstract class CompStateAccess[-C, S] {
  def state(c: C): S
  def setState(c: C, s: S, cb: OpCallback): Unit
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
    override def state(c: C)                          = c.get()
    override def setState(c: C, s: S, cb: OpCallback) = c.set(s, cb)
  }

  object SS extends HK[ComponentScope_SS] {
    override def state(c: C)                          = c._state.v
    override def setState(c: C, s: S, cb: OpCallback) = c._setState(WrapObj(s), cb.map[JFn](f => f))
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

    @inline def setState(s: S, cb: OpCallback = undefined)(implicit C: CC): Unit =
      C.setState(_c, s, cb)

    @inline def modState(f: S => S, cb: OpCallback = undefined)(implicit C: CC): Unit =
      setState(f(state), cb)

    def lift(implicit C: CC) = new CompStateFocus[S](
      () => _c.state,
      (a: S, cb: OpCallback) => _c.setState(a, cb))

    /** Zoom-in on a subset of the state. */
    def zoom[T](f: S => T)(g: (S, T) => S)(implicit C: CC) = new CompStateFocus[T](
      () => f(_c.state),
      (b: T, cb: OpCallback) => _c.setState(g(_c.state, b), cb))

    @deprecated("focusStateId has been renamed to lift. focusStateId will be removed in 0.10.0", "0.9.2")
    def focusStateId(implicit C: CC) = lift

    @deprecated("focusState has been renamed to zoom for consistency. focusState will be removed in 0.10.0", "0.9.2")
    def focusState[T](f: S => T)(g: (S, T) => S)(implicit C: CC) = zoom(f)(g)
  }
}

/**
 * Read & write access to a specific subset of a specific component's state.
 *
 * @tparam S The type of state.
 */
final class CompStateFocus[S] private[react](val get: () => S,
                                             val set: (S, OpCallback) => Unit)

object CompStateFocus {
  @inline def apply[S](get: () => S)(set: (S, OpCallback) => Unit): CompStateFocus[S] =
    new CompStateFocus(get, set)
}
