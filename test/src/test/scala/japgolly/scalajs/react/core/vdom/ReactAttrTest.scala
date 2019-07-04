package japgolly.scalajs.react.core.vdom

import scala.scalajs.js
import utest._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object ReactAttrTest extends TestSuite {

  val anything = VdomAttr[Any]("")
  val intOnly = ^.colSpan
  val stringOnly = ^.href
  val style = ^.style
  val mouse = ^.onMouseDown
  val focus = ^.onFocus

  def jsObj: js.Object = new js.Object()
  def jsDict: js.Dictionary[String] = js.Dictionary.empty
  def unit = ()
  def callback = Callback.empty
  def anyEH: ReactEvent => Callback = _ => callback
  def mouseEH: ReactMouseEvent => Callback = _ => callback
  def focusEH: ReactFocusEvent => Callback = _ => callback
  def mouseInputEH: ReactMouseEventFromInput => Callback = _ => callback

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
