package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._, vdom.prefix_<^._, MonocleReact._
import japgolly.scalajs.react.extra.ExternalVar
import monocle.macros._

object ExternalVarExample {

  def content = SingleSide.Content(source, Main())

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START

  @Lenses
  case class Name(firstName: String, surname: String)

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

  val NameChanger = ReactComponentB[ExternalVar[String]]("Name changer")
    .render { evar =>
      def updateName = (event: ReactEventI) => evar.set(event.target.value)
      <.input(
        ^.`type`    := "text",
        ^.value     := evar.value,
        ^.onChange ==> updateName)
    }
    .build

  // EXAMPLE:END
}
