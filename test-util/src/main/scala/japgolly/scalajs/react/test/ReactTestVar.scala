package japgolly.scalajs.react.test

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._

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
class ReactTestVar[A](val initialValue: A) {

  override def toString =
    s"ReactTestVar(initialValue = $initialValue, value = ${value()})"

  private var _value: A = _
  private var _history: Vector[A] = _
  private var _onUpdate: Vector[Callback] = _
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
      cb.attempt.runNow().left.toOption.foreach(_.printStackTrace())
  }

  def modValue(f: A => A): Unit =
    setValue(f(value()))

  def modValueOption(f: A => Option[A]): Unit =
    f(value()).foreach(setValue)

  def value(): A =
    _value

  def onUpdate(callback: => Unit): Unit =
    _onUpdate :+= Callback(callback)

  /**
   * Log of state values since initialised or last reset.
   *
   * Changes are ordered from oldest to newest.
   *
   * The initial value is also included and is always the first element.
   */
  def history(): Vector[A] =
    _history

  val setStateOptionCBFn: Reusable[(Option[A], Callback) => Callback] =
    Reusable.byRef((oa, cb) => cb <<? oa.map(a => Callback(setValue(a))))

  val setStateFn: A ~=> Callback =
    setStateOptionCBFn.map(f => (a: A) => f(Some(a), Callback.empty))

  def stateSnapshot(): StateSnapshot[A] =
    StateSnapshot(value())(setStateOptionCBFn)

  def stateSnapshotWithReuse()(implicit r: Reusability[A]): StateSnapshot[A] =
    StateSnapshot.withReuse(value())(setStateOptionCBFn)

  lazy val stateAccess: StateAccessPure[A] =
    StateAccess(CallbackTo(value()))(
      setStateOptionCBFn,
      (fo, cb) => Callback(modValueOption(fo)) >> cb)
}

object ReactTestVar {
  def apply[A](a: A): ReactTestVar[A] =
    new ReactTestVar(a)
}
