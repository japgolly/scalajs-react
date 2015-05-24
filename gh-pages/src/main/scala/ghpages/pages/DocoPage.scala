package ghpages.pages

import japgolly.scalajs.react._, vdom.prefix_<^._

object DocoPage {

  val component = ReactComponentB.static("Doco",
    <.p("Please see the ",
      <.a(^.href := "https://github.com/japgolly/scalajs-react", "project page"),
      ".")
  ).buildU

}
