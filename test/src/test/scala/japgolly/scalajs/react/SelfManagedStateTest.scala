package japgolly.scalajs.react

import monocle.macros.Lenses
import utest._
import japgolly.scalajs.react._, vdom.prefix_<^._, ScalazReact._, MonocleReact._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.test._
import TestUtil2._

object SelfManagedStateTest extends TestSuite {

  // Heavily trimmed-down version of something more useful

  type State = StateFor[Any]

  case class StateFor[+A](value: A, renderFn: () => ReactElement) {
    @inline def render: ReactElement = renderFn()
  }

  type SetState        = State => Callback
  type SetStateFor[-A] = StateFor[A] => Callback

  def selfManaged[S, A](initial   : A,
                        convInput : S => A,
                        setSelf   : SetStateFor[A],
                        renderEdit: (A, S => Callback) => ReactElement): StateFor[A] = {

    def state(a: A): StateFor[A] =
      StateFor(a, () => renderEdit(a, recvEdit))

    def recvEdit: S => Callback =
      s => setSelf(editState(convInput(s)))

    def editState(a: A): StateFor[A] =
      state(a)

    editState(initial)
  }

  // -------------------------------------------------------------------------------------------------------------------

  // Copied from ExternalVarExample
  val NameChanger = ReactComponentB[ExternalVar[String]]("Name changer")
    .render_P { evar =>
    def updateName = (event: ReactEventI) => evar.set(event.target.value)
    <.input(
      ^.`type`    := "text",
      ^.value     := evar.value,
      ^.onChange ==> updateName)
  }
    .build

  def selfManagedTextEditor(initial: String, setSelf: SetStateFor[String]) =
    selfManaged[String, String](initial, identity, setSelf, (v, update) =>
      NameChanger(ExternalVar(v)(update)))

  // -------------------------------------------------------------------------------------------------------------------

  @Lenses
  case class TopLevelState(firstName: StateFor[String], lastName: StateFor[String])

  def initTopLevelState($: CompStateFocus[TopLevelState], firstName: String, lastName: String): TopLevelState =
    TopLevelState(
      selfManagedTextEditor(firstName, $ _setStateL TopLevelState.firstName),
      selfManagedTextEditor(lastName,  $ _setStateL TopLevelState.lastName))

  val TopLevel = ReactComponentB[Unit]("TopLevel")
    .initialStateC[TopLevelState](initTopLevelState(_, "John", "Wick"))
    .render_S { s =>
      <.div(
        <.label("First name:", s.firstName.render),
        <.label("Surname:",    s.lastName.render),
        <.p(s"My name is ${s.firstName.value} ${s.lastName.value}."))
    }
    .buildU

  override def tests = TestSuite {
    val c  = ReactTestUtils.renderIntoDocument(TopLevel())
    def p  = ReactTestUtils.findRenderedDOMComponentWithTag(c, "p").getDOMNode().innerHTML
    def is = ReactTestUtils.scryRenderedDOMComponentsWithTag(c, "input")
    def i1 = is(0)

    assertEq("My name is John Wick.", p)
    ChangeEventData("Fat") simulate i1
    assertEq("My name is Fat Wick.", p)
  }
}
