package japgolly.scalajs.react.core

import japgolly.scalajs.react.React
import scala.scalajs.js
import utest._

object ContextTest extends TestSuite {

  case class X()

  def ObjectIs(a: js.Any, b: js.Any): Boolean =
    js.Dynamic.global.Object.is(a, b).asInstanceOf[Boolean]

  override def tests = Tests {

    'refEq {
      def test[A](a: React.Context.Provided[A])(b: React.Context.Provided[A], expect: Boolean = true): Unit = {
        val actual = ObjectIs(a.rawValue, b.rawValue)
        assert(actual == expect)
      }

      'int - {
        val ctx = React.createContext(123)
        test(ctx.provide(3))(ctx.provide(3))
        test(ctx.provide(3))(ctx.provide(4), false)
      }

      'str - {
        val ctx = React.createContext("argh")
        test(ctx.provide("3a"))(ctx.provide("3a"))
        test(ctx.provide("3a"))(ctx.provide("4a"), false)
      }

      'caseClass - {
        val ctx = React.createContext(X())
        val p = ctx.provide(X())
        test(p)(p)
        test(p)(ctx.provide(X()), false)
      }

    }

  }
}
