package japgolly.scalajs.react.component

import japgolly.scalajs.react.internal.JsUtil
import org.scalajs.dom.console
import scala.annotation.elidable
import scala.scalajs.js

object InspectRaw {

  @elidable(elidable.ASSERTION)
  def assertValidJsComponent(input: Any, where: sourcecode.FullName, line: sourcecode.Line): Unit =
    assertValid(input, "JsComponent", where, line) {
      case _: js.Function => true
      case o: js.Object   => List("$$typeof", "type").exists(js.Object.hasProperty(o, _))
      case _              => false
    }

  @elidable(elidable.ASSERTION)
  def assertValidJsFn(input: Any, where: sourcecode.FullName, line: sourcecode.Line): Unit =
    assertValid(input, "JsFnComponent", where, line) {
      case _: js.Function => true
      case _              => false
    }

  @elidable(elidable.ASSERTION)
  def assertValidJsForwardRefComponent(input: Any, where: sourcecode.FullName, line: sourcecode.Line): Unit =
    assertValid(input, "JsForwardRefComponent", where, line)(typeSymbolIs("react.forward_ref"))

  private def typeSymbolIs(expect: String): Any => Boolean = {
    case o: js.Object =>
      val t = o.asInstanceOf[js.Dynamic].selectDynamic("$$typeof").asInstanceOf[Any]
      js.Symbol.forKey(expect) == t
    case _ => false
  }

  private def assertValid(input: Any, name: String, where: sourcecode.FullName, line: sourcecode.Line)
                         (isValid: Any => Boolean): Unit =
    assertValid(input, name, name, s"$name.force", where, line)(isValid)

  private def assertValid(input: Any, name: String, thisMethod: String, forceMethod: String,
                          where: sourcecode.FullName, line: sourcecode.Line)
                         (isValid: Any => Boolean): Unit =
    if (!isValid(input)) {

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
           |Invalid $name! You've called $thisMethod(${JsUtil.inspectValue(input)})
           |Source: ${where.value} (line #${line.value})
           |
           |${solution.trim}
           |
           |If you believe this error message is wrong, please raise a scalajs-react issue
           |and use $forceMethod as a workaround.
           |!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
           |
         """.stripMargin

      try console.error(errMsg) catch {case _: Throwable => }
      throw new AssertionError(errMsg)
    }

}
