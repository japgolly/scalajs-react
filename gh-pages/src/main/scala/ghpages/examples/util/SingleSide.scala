package ghpages.examples.util

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object SingleSide {

  case class Content(scalaSource: String, el: VdomElement) {
    def apply() = singleSideComponent(this)
  }

  val singleSideComponent = ScalaComponent.builder[Content]("singleSideComponent")
    .render_P(p =>
      <.div(
        <.section(^.cls := "demo",
          <.div(^.cls := "demo", p.el)),
        <.hr,
        <.h3("Source"),
        <.div(^.cls := "row",
          <.div(^.cls := "col-md-10",
            CodeSnippets.scala(p.scalaSource)))))
    .configure(CodeSnippets.installSyntaxHighlighting)
    .build
}
