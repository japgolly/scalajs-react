package ghpages.examples.util

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object SideBySide {

  case class Content(jsSource: String, scalaSource: String, el: VdomElement) {
    def apply() = sideBySideComponent(this)
  }

  val sideBySideComponent = ScalaFnComponent[Content] { p =>
    for {
      ref <- CodeSnippets.useSyntaxHighlighting
    } yield
      <.div.withRef(ref)(
        <.section(^.cls := "demo",
          <.div(^.cls := "demo", p.el)),
        <.hr,
        <.div(^.cls := "row",
          <.div(^.cls := "col-md-6",
            <.h3("JS source"),
            CodeSnippets.js(p.jsSource)),
          <.div(^.cls := "col-md-6",
            <.h3("Scala source"),
            CodeSnippets.scala(p.scalaSource))))
  }
}
