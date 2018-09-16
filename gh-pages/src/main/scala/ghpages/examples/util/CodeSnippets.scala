package ghpages.examples.util

import org.scalajs.dom.Element
import org.scalajs.dom.ext.PimpedNodeList
import scala.scalajs.js.Dynamic.global
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object CodeSnippets {

  def apply(lang: String)(src: String): VdomTag =
    <.pre(
      ^.padding := "0",
      <.code(
        ^.cls := s"lang-$lang",
        src.trim))

  val js: String => VdomTag =
    apply("javascript")

  val scala: String => VdomTag =
    apply("scala")

  def installSyntaxHighlighting[P, C <: Children, S, B, U <: UpdateSnapshot]: ScalaComponent.Config[P, C, S, B, U, UpdateSnapshot.Some[U#Value]] = _
    .componentDidMount (_.getDOMNode.toElement.fold(Callback.empty)(applySyntaxHighlighting))
    .componentDidUpdate(_.getDOMNode.toElement.fold(Callback.empty)(applySyntaxHighlighting))

  private def applySyntaxHighlighting(root: Element) = Callback {
    val nodeList = root.querySelectorAll("pre code").toArray
    nodeList.foreach(global.hljs highlightBlock _)
  }
}
