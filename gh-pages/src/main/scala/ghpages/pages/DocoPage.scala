package ghpages.pages

import japgolly.scalajs.react._, vdom.html_<^._

object DocoPage {

  val component = ScalaComponent.static("Doco")(
    <.p(
      ^.marginTop := "1em",
      ^.fontSize := "110%",
      ^.color := "#292929",
      "There's plenty, but for now it's all on the ",
      <.a(^.href := "https://github.com/japgolly/scalajs-react", "project page"),
      "..."))

}
