package japgolly.scalajs.react.component

import japgolly.scalajs.react.internal.JsUtil
import japgolly.scalajs.react.internal.Util._
import scala.scalajs.LinkingInfo.developmentMode
import scala.scalajs.js

object InspectRaw {

  @inline def assertValidJsComponent(input: => Any, where: => sourcecode.File, line: => sourcecode.Line): Unit =
    if (developmentMode) {
      val i = input
      devAssert(
        i match {
          case _: js.Function => true
          case o: js.Object   => List("$$typeof", "type").exists(js.Object.hasProperty(o, _))
          case _              => false
        },
        errMsg(i, "JsComponent", where, line))
    }

  @inline def assertValidJsFn(input: => Any, where: => sourcecode.File, line: => sourcecode.Line): Unit =
    if (developmentMode) {
      val i = input
      devAssert(
        i match {
          case _: js.Function => true
          case _              => false
        },
        errMsg(i, "JsFnComponent", where, line))
    }

  @inline def assertValidJsForwardRefComponent(input: => Any, where: => sourcecode.File, line: => sourcecode.Line): Unit =
    if (developmentMode) {
      val i = input
      devAssert(
        typeSymbolIs(i, "react.forward_ref"),
        errMsg(i, "JsForwardRefComponent", where, line))
    }

  // ===================================================================================================================

  private def typeSymbolIs(input: Any, expect: String): Boolean =
    input match {
      case o: js.Object =>
        val t = o.asInstanceOf[js.Dynamic].selectDynamic("$$typeof").asInstanceOf[Any]
        js.Symbol.forKey(expect) == t
      case _ =>
        false
    }

  private def errMsg(input: Any, name: String, where: sourcecode.File, line: sourcecode.Line): String =
    errMsg(input, name, name, s"$name.force", where, line)

  private def errMsg(input: Any, name: String, thisMethod: String, forceMethod: String,
                     where: sourcecode.File, line: sourcecode.Line): String = {
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

    s"""
       |================================================================================
       |Invalid $name at
       |${where.value}:${line.value}
       |
       |You called $thisMethod() with an invalid argument:
       |${JsUtil.inspectValue(input).indent(2)}
       |
       |${solution.trim}
       |
       |If you believe this error message is wrong, please raise a scalajs-react issue
       |and use $forceMethod as a workaround.
       |================================================================================
     """.stripMargin

    /*
    ================================================================================
    Invalid JsFnComponent at
    /home/golly/projects/public/scalajs-react/test/src/test/scala/japgolly/scalajs/react/core/JsLikeComponentTest.scala:41

    You called JsFnComponent() with an invalid argument:
      undefined

    Make sure that
      * your @JSImport / @JSGlobal annotations have the correct values
      * the JS that you're referencing has been loaded into the JS environment

    If you believe this error message is wrong, please raise a scalajs-react issue
    and use JsFnComponent.force as a workaround.
    ================================================================================
    */
  }
}
