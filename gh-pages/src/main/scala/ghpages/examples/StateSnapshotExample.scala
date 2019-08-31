package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._, vdom.html_<^._
import japgolly.scalajs.react.MonocleReact._
import japgolly.scalajs.react.extra.StateSnapshot


object StateSnapshotExample {

  def content = SingleSide.Content(source, Main())

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START
  import monocle.macros._

  @Lenses
  case class Name(firstName: String, surname: String)

  val NameChanger = ScalaComponent.builder[StateSnapshot[String]]("Name changer")
    .render_P { stateSnapshot =>
      <.input.text(
        ^.value     := stateSnapshot.value,
        ^.onChange ==> ((e: ReactEventFromInput) => stateSnapshot.setState(e.target.value)))
    }
    .build

  val Main = ScalaComponent.builder[Unit]("StateSnapshot example")
    .initialState(Name("John", "Wick"))
    .render { $ =>
      val name       = $.state
      val firstNameV = StateSnapshot.zoomL(Name.firstName).of($)
      val surnameV   = StateSnapshot.zoomL(Name.surname).of($)
      <.div(
        <.label("First name:", NameChanger(firstNameV)),
        <.label("Surname:",    NameChanger(surnameV  )),
        <.p(s"My name is ${name.surname}, ${name.firstName} ${name.surname}."))
    }
    .build

  // EXAMPLE:END
}
