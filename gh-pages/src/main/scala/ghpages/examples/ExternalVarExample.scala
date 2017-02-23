package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._, vdom.html_<^._, MonocleReact._
import japgolly.scalajs.react.extra.StateSnapshot

object StateSnapshotExample {

  def content = SingleSide.Content(source, Main())

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START
  import monocle.macros._

  @Lenses
  case class Name(firstName: String, surname: String)

  val NameChanger = ScalaComponent.build[StateSnapshot[String]]("Name changer")
    .render_P { evar =>
      def updateName = (event: ReactEventFromInput) => evar.set(event.target.value)
      <.input.text(
        ^.value     := evar.value,
        ^.onChange ==> updateName)
    }
    .build

  val Main = ScalaComponent.build[Unit]("StateSnapshot example")
    .initialState(Name("John", "Wick"))
    .render { $ =>
      val name        = $.state
      val firstNameEV = StateSnapshot.zoomL(Name.firstName).of($)
      val surnameEV   = StateSnapshot.zoomL(Name.surname).of($)
      <.div(
        <.label("First name:", NameChanger(firstNameEV)),
        <.label("Surname:",    NameChanger(surnameEV  )),
        <.p(s"My name is ${name.surname}, ${name.firstName} ${name.surname}."))
    }
    .build

  // EXAMPLE:END
}
