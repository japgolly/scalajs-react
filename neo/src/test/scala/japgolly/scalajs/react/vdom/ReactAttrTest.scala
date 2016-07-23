package japgolly.scalajs.react.vdom

import utest._
import scala.scalajs.js
import PackageBase._

object ReactAttrTest extends TestSuite {

  val stringOnly = "".reactAttr[String]
  val intOnly = "".reactAttr[Int]
  val style = "".reactAttr[js.Object]
  val anything = "".reactAttr[Any]

  def jsObj: js.Object = new js.Object()
  def jsDict: js.Dictionary[String] = js.Dictionary.empty
  def unit = ()

  override def tests = TestSuite {

    'valueTypes {

      'any {
        anything := ""
        anything := 5
        anything := jsObj
        anything := jsDict
        anything := unit
      }

      'string {
        stringOnly := ""
        compileError("stringOnly := 5")
        compileError("stringOnly := jsObj")
        compileError("stringOnly := jsDict")
        compileError("stringOnly := unit").msg
      }

      'int {
        intOnly := 5
        compileError("intOnly := \"\"")
        compileError("intOnly := jsObj")
        compileError("intOnly := jsDict")
        compileError("intOnly := unit").msg
      }

      'jsObject {
        style := jsObj
        style := jsDict
        compileError("style := 5")
        compileError("style := \"\"")
        compileError("style := unit").msg
      }

    }

  }
}
