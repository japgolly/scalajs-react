package japgolly.scalajs.react.core.vdom

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scala.scalajs.js
import utest._

object ReactAttrTest extends TestSuite {

  private val anything = VdomAttr[Any]("")
  private val intOnly = ^.colSpan
  private val stringOnly = ^.href
  private val style = ^.style
  private val mouse = ^.onMouseDown
  private val focus = ^.onFocus

  private def jsObj: js.Object = new js.Object()
  private def jsDict: js.Dictionary[String] = js.Dictionary.empty
  private val unit = ()
  private def callback = Callback.empty
  private def anyEH: ReactEvent => Callback = _ => callback
  private def mouseEH: ReactMouseEvent => Callback = _ => callback
  private def focusEH: ReactFocusEvent => Callback = _ => callback
  private def mouseInputEH: ReactMouseEventFromInput => Callback = _ => callback

  override def tests = Tests {

    "valueTypes" - {

      "any" - {
        anything := ""
        anything := 5
        anything := jsObj
        anything := jsDict
        anything := unit
      }

      "string" - {
        stringOnly := ""
        compileError("stringOnly := 5") // Explicit .toString required
        compileError("stringOnly := jsObj")
        compileError("stringOnly := jsDict")
        compileError("stringOnly := unit").msg
      }

      "int" - {
        intOnly := 5
        compileError("intOnly := \"\"")
        compileError("intOnly := jsObj")
        compileError("intOnly := jsDict")
        compileError("intOnly := unit").msg
      }

      "jsObject" - {
        style := jsObj
        style := jsDict
        compileError("style := 5")
        compileError("style := \"\"")
        compileError("style := unit").msg
      }

      "mouseEvent" - {
        mouse --> callback
        mouse ==> anyEH
        mouse ==> mouseEH
        mouse ==> mouseInputEH
        compileError("mouse ==> focusEH")
        compileError("mouse := \"\"")
        compileError("mouse := 5")
        compileError("mouse := jsObj")
        compileError("mouse := jsDict")
        compileError("mouse := unit").msg
      }

      "focusEvent" - {
        focus --> callback
        focus ==> anyEH
        focus ==> focusEH
        compileError("focus ==> mouseEH")
        compileError("focus ==> mouseInputEH")
        compileError("focus := \"\"")
        compileError("focus := 5")
        compileError("focus := jsObj")
        compileError("focus := jsDict")
        compileError("focus := unit").msg
      }

    }

  }
}
