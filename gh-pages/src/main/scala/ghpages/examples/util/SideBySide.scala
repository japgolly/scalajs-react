package ghpages.examples.util

import japgolly.scalajs.react._, vdom.ReactVDom._, all._, tags2._
import org.scalajs.dom.document
import org.scalajs.dom.extensions.PimpedNodeList

object SideBySide {

  case class Content(jsSource: String, scalaSource: String, el: ReactElement) {
    def apply() = sideBySideComponent(this)
  }

  val sideBySideComponent = ReactComponentB[Content]("sideBySideExample")
    .render(p =>
      div(
        div(`class` := "row",
          div(`class` := "col-md-6",
            h3("JS source"),
            pre(code(p.jsSource))),
          div(`class` := "col-md-6",
            h3("Scala source"),
            pre(code(p.scalaSource)))),
        hr,
        section(cls := "demo",
          h3("Demo"),
          div(cls := "demo", p.el)))
    )
    .configure(installSyntaxHighlighting)
    .build

  def installSyntaxHighlighting[P, S, B] =
    (_: ReactComponentB[P, S, B])
      .componentDidMount(_ => applySyntaxHighlight())
      .componentDidUpdate((_,_,_)  => applySyntaxHighlight())

  def applySyntaxHighlight() = {
    import scala.scalajs.js.Dynamic.{global => g}
    val nodeList = document.querySelectorAll("pre code").toArray
    nodeList.foreach( n => g.hljs.highlightBlock(n))
  }
}