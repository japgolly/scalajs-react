package ghpages.examples.util

import japgolly.scalajs.react._, vdom.all._

object SingleSide {

  case class Content(scalaSource: String, el: VdomElement) {
    def apply() = singleSideComponent(this)
  }

  val singleSideComponent = ScalaComponent.build[Content]("singleSideComponent")
    .render_P(p =>
      div(
        section(cls := "demo",
          div(cls := "demo", p.el)),
        hr,
        h3("Source"),
        div(`class` := "row",
          div(`class` := "col-md-10",
            pre(code(p.scalaSource.trim))))
      )
    )
    .configure(SideBySide.installSyntaxHighlighting)
    .build
}
