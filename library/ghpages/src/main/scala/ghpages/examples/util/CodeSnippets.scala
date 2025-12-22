package ghpages.examples.util

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.Element
import scala.scalajs.js.Dynamic.global

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

  val useSyntaxHighlighting: HookResult[Ref.ToVdom[Element]] =
    for {
      ref <- useRefToVdom[Element]
      _ <- useEffect(applySyntaxHighlightingToRef(ref))
    } yield ref

  private def applySyntaxHighlightingToRef(ref: Ref.ToVdom[Element]): Callback =
    for {
      el <- ref.get.asCBO
      _ <- applySyntaxHighlighting(el)
    } yield ()

  private def applySyntaxHighlighting(root: Element): Callback =
    Callback {
      val nodeList = root.querySelectorAll("pre code").toArray
      nodeList.foreach(global.hljs highlightBlock _)
    }
}
