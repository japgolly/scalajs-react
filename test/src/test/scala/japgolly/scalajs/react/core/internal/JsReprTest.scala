package japgolly.scalajs.react.core.internal

import japgolly.scalajs.react.internal.{Box, JsRepr}
import scala.scalajs.js
import utest._

object JsReprTest extends TestSuite {

  case class X(i: Int)

  val jsObj = new js.Object()

  override def tests = Tests {

    'implicits {
      def test[A](a: A, expect: js.Any)(implicit j: JsRepr[A]): Unit = {

        val fromExpected = j.unsafeFromJs(expect)
        assert(a == fromExpected)

        val actual = j.toJs(a)
        val roundTrip = j.unsafeFromJs(actual)
        assert(a == roundTrip)
      }

      'unit      - test((), ())
      'boolean   - test(true, true)
      'byte      - test(3.toByte, 3.toByte)
      'short     - test(7.toShort, 7.toShort)
      'int       - test(11, 11)
      'longMin   - test(Long.MinValue, Long.MinValue)
      'longMax   - test(Long.MaxValue, Long.MaxValue)
      'float     - test(14.3f, 14.3f)
      'double    - test(3.6, 3.6)
      'string    - test("heh", "heh")
      'jsAny     - test(jsObj.asInstanceOf[js.Any], jsObj.asInstanceOf[js.Any])
      'jsObject  - test(jsObj, jsObj)
      'caseClass - test(X(3), Box(X(3)))
    }

  }
}
