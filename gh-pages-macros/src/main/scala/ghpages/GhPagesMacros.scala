package ghpages

import java.util.regex.Pattern
import scala.reflect.macros.blackbox.Context
import japgolly.scalajs.react.internal.MacroUtils

object GhPagesMacros {
  def exampleSource: String = macro GhPagesMacroImpls.exampleSource
}

object GhPagesMacroImpls {
  val trimRight = "\\s+$".r

  def blankLine(s: String) =
    s.trim.isEmpty

  @annotation.tailrec
  def trimLeftAll(ls: List[String]): List[String] =
    if (ls.nonEmpty && ls.forall(_.headOption forall Character.isWhitespace))
      trimLeftAll(ls.map(s => if (s.isEmpty) s else s.tail))
    else
      ls

  val exampleStart = "EXAMPLE:START"
  val exampleEnd = "EXAMPLE:END"
}

class GhPagesMacroImpls(val c: Context) extends MacroUtils {
  import GhPagesMacroImpls._
  import c.universe._

  def splitOnce(marker: String)(s: String): (String, String) = {
    val r = s"""\n[ \t]*//[ \t]*${Pattern quote marker}[ \t]*""".r
    val x = r.split(s)
    if (x.length < 2)
      fail(s"Marker not found: // $marker")
    else if (x.length > 2)
      fail(s"Duplicate marker found: // $marker")
    (x(0), x(1))
  }

  def betweenMarkers(s: String, a: String, b: String): String = {
    val tmp = splitOnce(a)(s)._2
    splitOnce(b)(tmp)._1
  }

  def exampleSource: c.Expr[String] = {
    val fileContent = String valueOf c.enclosingPosition.source.content
    val egContent = betweenMarkers(fileContent, exampleStart, exampleEnd)

    val lines =
      egContent.split('\n')
        .iterator
        .map(trimRight.replaceFirstIn(_, ""))
        .dropWhile(blankLine)
        .toList
        .reverse.dropWhile(blankLine).reverse

    val output = trimLeftAll(lines) mkString "\n"

    c.Expr[String](Literal(Constant(output)))
  }
}
