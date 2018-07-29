package japgolly.scalajs.react.component

import org.scalajs.dom.console
import scala.annotation.elidable
import scala.scalajs.js
import japgolly.scalajs.react.internal.JsUtil

object InspectRaw {

  @elidable(elidable.ASSERTION)
  def assertValidJsComponent(input: js.Any, where: sourcecode.FullName, line: sourcecode.Line): Unit =
    assertValid(input, "JsComponent", where, line) {
      case _: js.Function => true
      case o: js.Object   => List("$$typeof", "type").exists(js.Object.hasProperty(o, _))
      case _              => false
    }

  @elidable(elidable.ASSERTION)
  def assertValidJsFn(input: js.Any, where: sourcecode.FullName, line: sourcecode.Line): Unit =
    assertValid(input, "JsFnComponent", where, line) {
      case _: js.Function => true
      case _              => false
    }

  private def assertValid(input: js.Any, name: String, where: sourcecode.FullName, line: sourcecode.Line)
                         (isValid: js.Any => Boolean): Unit =
    if (!isValid(input)) {

      def invalidComponentDesc(a: js.Any): String =
        (a: Any) match {
          case s: String    => '"' + s.replace("\n", "\\n") + '"' // doesn't need to be perfect
          case o: js.Object => JsUtil.inspectObject(o)
          case ()           => a.toString
          case _            => s"${a.toString}: ${js.typeOf(a)}"
        }

      val solution: String = (input: Any) match {
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
           |Invalid $name! You've called $name(${invalidComponentDesc(input)})
           |Source: ${where.value} (line #${line.value})
           |
           |${solution.trim}
           |
           |If you believe this error message is wrong, please raise a scalajs-react issue
           |and use $name.force as a workaround.
           |!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
           |
         """.stripMargin

      try console.error(errMsg) catch {case _: Throwable => }
      throw new AssertionError(errMsg)
    }

}
