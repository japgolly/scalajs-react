package ghpages.examples.util

import japgolly.scalajs.react._, vdom.ReactVDom._, all._

object SingleSide {

  case class Content(scalaSource: String, el: ReactElement) {
    def apply() = singleSideComponent(this)
  }

  val singleSideComponent = ReactComponentB[Content]("singleSideComponent")
    .render(p =>
        div(`class` := "row",
          div(`class` := "col-md-6",
            pre(code(p.scalaSource.trim))),
          div(`class` := "col-md-6",
            p.el))
    )
    .configure(SideBySide.installSyntaxHighlighting)
    .build
}
