package japgolly.scalajs.react.component

import org.scalajs.dom.console
import scala.annotation.elidable
import scala.scalajs.js
import scala.util.Try
import japgolly.scalajs.react.internal.JsUtil

object InspectRaw {

  def isComponent(a: js.Any): Boolean =
    a match {
      case _: js.Function => true
      case _              => false
    }

  @elidable(elidable.ASSERTION)
  @inline
  def assertIsComponent(aa: => js.Any, name: => String, where: sourcecode.FullName, line: sourcecode.Line): Unit = {
    val a = aa
    if (!isComponent(a)) {

      def invalidComponentDesc(a: js.Any): String =
        (a: Any) match {
          case s: String    => '"' + s.replace("\n", "\\n") + '"' // doesn't need to be perfect
          case o: js.Object => JsUtil.inspectObject(o)
          case ()           => a.toString
          case _            => s"${a.toString}: ${js.typeOf(a)}"
        }

      val solution: String = (a: Any) match {
        case _: String =>
          """
            |String arguments are no longer supported. Either:
            |  * create a JS facade using @JSImport / @JSGlobal
            |  * grab the JS value using js.Dynamic
            |
            |See https://github.com/japgolly/scalajs-react/blob/master/doc/INTEROP.md"
          """.stripMargin
        case _ =>
          """
            |Make sure that
            |  * your @JSImport / @JSGlobal annotations have the correct values
            |  * the JS that you're referencing has been loaded into the JS environment
          """.stripMargin
      }

      val errMsg =
        s"""
           |
           |!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
           |Invalid $name! You've called $name(${invalidComponentDesc(a)})
           |Source: ${where.value} (line #${line.value})
           |
           |${solution.trim}
           |!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
           |
         """.stripMargin
      Try(console.error(errMsg))
      throw new AssertionError(errMsg)
    }
  }

}
