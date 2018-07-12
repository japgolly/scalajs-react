package japgolly.scalajs.react.core.internal

import japgolly.scalajs.react.internal.{Box, JsRepr}
import scala.scalajs.js
import utest._

object JsReprTest extends TestSuite {

  case class X(i: Int)

  override def tests = Tests {

    'implicits {
      def test[A](a: A, expect: js.Any)(implicit j: JsRepr[A]): Unit = {

        val fromExpected = j.unsafeFromJs(expect)
        assert(a == fromExpected)

        val actual = j.toJs(a)
        val roundTrip = j.fromJs(actual)
        assert(a == roundTrip)
      }

      'bool - test(true, true)
      'int - test(123, 123)
      'caseClass - test(X(3), Box(X(3)))
    }

  }
}
