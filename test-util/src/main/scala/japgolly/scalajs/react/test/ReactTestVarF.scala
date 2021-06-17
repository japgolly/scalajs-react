package japgolly.scalajs.react.test

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.util.DefaultEffects.Async
import japgolly.scalajs.react.util.Effect.Sync

/**
  * Houses a variable and provides React-like access to it.
  *
  * Provides mock-like instances of the following:
  * - [[Reusable]] functions
  * - [[StateAccess]]
  * - [[StateSnapshot]]
  * - [[StateSnapshot]] with [[Reusability]]
  *
  * It also keeps a log of changes, accessible via `.history()`.
  *
  * @tparam A The variable type.
  * @since 0.11.0
  */
class ReactTestVarF[F[_], A](val initialValue: A)(implicit val sync: Sync[F]) {

  override def toString =
    s"ReactTestVar(initialValue = $initialValue, value = ${value()})"

  private var _value: A = _
  private var _history: Vector[A] = _
  private var _onUpdate: Vector[F[Unit]] = _
  reset()

  def reset(): Unit = {
    resetListeners()
    resetData()
  }

  def resetListeners(): Unit = {
    _onUpdate = Vector.empty
  }

  def resetData(): Unit = {
    _history = Vector.empty
    setValue(initialValue)
  }

  def setValue(a: A): Unit = {
    _value = a
    _history :+= a
    for (cb <- _onUpdate)
      sync.runSync(sync.reset(cb))
  }

  def modValue(f: A => A): Unit =
    setValue(f(value()))

  def modValueOption(f: A => Option[A]): Unit =
    f(value()).foreach(setValue)

  def value(): A =
    _value

  def onUpdate(callback: => Unit): Unit =
    _onUpdate :+= sync.delay(callback)

  /**
   * Log of state values since initialised or last reset.
   *
   * Changes are ordered from oldest to newest.
   *
   * The initial value is also included and is always the first element.
   */
  def history(): Vector[A] =
    _history

  val setStateOptionCBFn: Reusable[(Option[A], F[Unit]) => F[Unit]] =
    Reusable.byRef((oa, cb) =>
      oa match {
        case Some(a) => sync.chain(sync.delay(setValue(a)), cb)
        case None    => cb
      }
    )

  val setStateFn: Reusable[A => F[Unit]] =
    setStateOptionCBFn.map(f => (a: A) => f(Some(a), sync.empty))

  def stateSnapshot(): StateSnapshot[A] =
    StateSnapshot(value())(StateSnapshot.SetFn(setStateOptionCBFn))

  def stateSnapshotWithReuse()(implicit r: Reusability[A]): StateSnapshot[A] =
    StateSnapshot.withReuse(value())(setStateOptionCBFn.map(StateSnapshot.SetFn(_)))

  lazy val stateAccess: StateAccess[F, Async, A] =
    StateAccess[F, Async, A](
      stateFn = sync.delay(value()))(
      setItFn = setStateOptionCBFn,
      modItFn = (fo, cb) => sync.chain(sync.delay(modValueOption(fo)), cb))
}

object ReactTestVarF {
  def apply[F[_]: Sync, A](a: A): ReactTestVarF[F, A] =
    new ReactTestVarF(a)
}
