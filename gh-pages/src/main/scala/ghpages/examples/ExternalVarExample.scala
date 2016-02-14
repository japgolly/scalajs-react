package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._, vdom.prefix_<^._, MonocleReact._
import japgolly.scalajs.react.extra.ExternalVar

object ExternalVarExample {

  def content = SingleSide.Content(source, Main())

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START
  import monocle.macros._

  @Lenses
  case class Name(firstName: String, surname: String)

  val NameChanger = ReactComponentB[ExternalVar[String]]("Name changer")
    .render_P { evar =>
      def updateName = (event: ReactEventI) => evar.set(event.target.value)
      <.input.text(
        ^.value     := evar.value,
        ^.onChange ==> updateName)
    }
    .build

  val Main = ReactComponentB[Unit]("ExternalVar example")
    .initialState(Name("John", "Wick"))
    .render { $ =>
      val name        = $.state
      val firstNameEV = ExternalVar.state($.zoomL(Name.firstName))
      val surnameEV   = ExternalVar.state($.zoomL(Name.surname))
      <.div(
        <.label("First name:", NameChanger(firstNameEV)),
        <.label("Surname:",    NameChanger(surnameEV  )),
        <.p(s"My name is ${name.surname}, ${name.firstName} ${name.surname}."))
    }
    .buildU

  // EXAMPLE:END
}
