package japgolly.scalajs.react.test

import scalajs.js
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._

/**
 * Houses a variable and provides React-like access to it.
 *
 * It can be used to mock an `ExternalVar[A]`, a `ReusableVar[A]` and a `CompState.Access[A]`.
 *
 * @tparam A The variable type.
 */
class ReactTestVar[A](val initialValue: A) {
  import CompScope._

  override def toString =
    s"ReactTestVar(initialValue = $initialValue, value = ${value()})"

  private val obj: ObjectWithStateVar[A] = {
    type JSCB = js.UndefOr[js.Function0[js.Any]]
    type ModFn = js.Function1[WrapObj[A], WrapObj[A]]

    val setStateFn: js.Function2[js.Any, JSCB, Unit] =
      (arg: js.Any, cb: JSCB) => {

        val newValue: A =
          if (arg.toString.startsWith("function"))
            arg.asInstanceOf[ModFn].apply(valueW()).v
          else
            arg.asInstanceOf[WrapObj[A]].v

        setValue(newValue)
        cb.foreach(_.apply())
      }

    js.Dynamic.literal("state" -> WrapObj(initialValue), "setState" -> setStateFn)
      .asInstanceOf[ObjectWithStateVar[A]]
  }

  private def valueW(): WrapObj[A] =
    obj.state

  def value(): A =
    valueW().v

  def setValue(a: A): Unit =
    obj.state = WrapObj(a)

  def reset(): Unit =
    setValue(initialValue)

  def compStateAccess(): CompState.Access[A] =
    obj.asInstanceOf[CanSetState[A] with ReadCallback with WriteCallback]

  val reusableSet: A ~=> Callback =
    ReusableFn(a => Callback(setValue(a)))

  def reusableVar()(implicit r: Reusability[A]): ReusableVar[A] =
    ReusableVar(value())(reusableSet)(r)

  def externalVar(): ExternalVar[A] =
    ExternalVar(value())(reusableSet)
}

object ReactTestVar {
  def apply[A](a: A): ReactTestVar[A] =
    new ReactTestVar(a)
}

/**
 * A JS object with a state variable.
 */
@js.native
trait ObjectWithStateVar[A] extends js.Object {
  var state: WrapObj[A] = js.native
}
