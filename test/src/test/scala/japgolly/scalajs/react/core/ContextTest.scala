package japgolly.scalajs.react.core

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.test.TestUtil._
import scala.scalajs.js
import utest._

object ContextTest extends TestSuite {

  case class X()

  def ObjectIs(a: js.Any, b: js.Any): Boolean =
    js.Dynamic.global.Object.is(a, b).asInstanceOf[Boolean]

  override def tests = Tests {

    'usage {
      case class X(i: Int)
      val ctx = React.createContext(X(5))
      val consumer = ScalaComponent.builder[Unit]("").render_P(_ => <.h4(ctx.consume("x = " + _))).build
      val provider = ScalaComponent.builder[X]("").render_P(i => <.pre(ctx.provide(i)(consumer()))).build

      'default  - assertRender(consumer(), "<h4>x = X(5)</h4>")
      'provided - assertRender(provider(X(5000)), "<pre><h4>x = X(5000)</h4></pre>")
    }

    'usageFn {
      val ctx = React.createContext(123)
      val consumer = ScalaFnComponent[Unit](_ => <.h4(ctx.consume("i = " + _)))
      val provider = ScalaFnComponent[Int](i => <.pre(ctx.provide(i)(consumer(())))) // TODO (()) should be ()

      'default  - assertRender(consumer(()), "<h4>i = 123</h4>")
      'provided - assertRender(provider(88), "<pre><h4>i = 88</h4></pre>")
    }

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
