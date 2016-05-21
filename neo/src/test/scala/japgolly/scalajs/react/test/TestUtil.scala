package japgolly.scalajs.react.test

import org.scalajs.dom.{document, html}

import scalaz.Equal
import scalaz.syntax.equal._
import scala.io.AnsiColor._

object TestUtil extends TestUtil

trait TestUtil
  extends scalaz.std.StringInstances
     with scalaz.std.StreamInstances
     with scalaz.std.VectorInstances
     with scalaz.std.SetInstances
     with scalaz.std.TupleInstances
     with scalaz.std.OptionInstances
     with scalaz.std.AnyValInstances
     with scalaz.std.ListInstances {

  def assertEq[A: Equal](actual: A, expect: A): Unit =
    assertEq(null, actual, expect)

  def assertEq[A: Equal](name: => String, actual: A, expect: A): Unit =
    if (actual ≠ expect) {
      println()
      Option(name).foreach(n => println(s">>>>>>> $n"))

      val toString: Any => String = {
        case s: Stream[_] => s.force.toString() // SI-9266
        case a            => a.toString
      }

      var as = toString(actual)
      var es = toString(expect)
      var pre = "["
      var post = "]"
      if ((as + es) contains "\n") {
        pre = "↙[\n"
      }
      println(s"expect: $pre$BOLD$BLUE$es$RESET$post")
      println(s"actual: $pre$BOLD$RED$as$RESET$post")
      println()
      assert(false)
    }

  def fail(msg: String, clearStackTrace: Boolean = true): Nothing = {
    val t = new AssertionError(msg)
    if (clearStackTrace)
      t.setStackTrace(Array.empty)
    throw t
  }

  final type TopNode = org.scalajs.dom.Element

  def assertOuterHTML(node: TopNode, expect: String): Unit =
    assertOuterHTML(null, node, expect)

  def assertOuterHTML(name: => String, node: TopNode, expect: String): Unit =
    assertEq(name, scrubReactHtml(node.outerHTML), expect)

  private val reactRubbish = """\s+data-react\S*?\s*?=\s*?".*?"|<!--(?:.|[\r\n])*?-->""".r

  def scrubReactHtml(html: String): String =
    reactRubbish.replaceAllIn(html, "")

  def newBodyContainer(): html.Element = {
    val cont = document.createElement("div")
    document.body.appendChild(cont)
    cont.asInstanceOf[html.Element]
  }

//  def withRenderedIntoBody[A, B](render: html.Element => A)(n: A => TopNode, use: A => B): B = {
//    val parent = newBodyContainer()
//    try {
//      val a = render(parent)
//      try
//        use(a)
//      finally
//        ReactDOM unmountComponentAtNode n(a).parentNode
//    } finally
//      document.body.removeChild(parent)
//  }

}

