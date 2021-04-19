package japgolly.scalajs.react

import scala.quoted.*

// TODO: Move into test-utils

object CompileTimeTestUtil {

  inline def assertEqualExprs(inline actual: Any, inline expect: Any): Unit =
    ${ _assertEqualExprs('actual, 'expect, false) }

  inline def _assertEqualExprs(inline actual: Any, inline expect: Any): Unit =
    ${ _assertEqualExprs('actual, 'expect, true) }

  private def _assertEqualExprs(actual: Expr[Any], expect: Expr[Any], debug: Boolean)(using Quotes): Expr[Unit] = {
    if (actual matches expect) {
      if debug then println(actual.show)
      '{ () }
    } else {
      val a = actual.show
      val e = expect.show
      // TODO: use assertMultiline
      quotes.reflect.report.throwError(s"Actual: $a\nExpect: $e")
    }
  }

  inline def showCode[A](inline a: A): A =
    ${ showCodeImpl('a) }

  private def showCodeImpl[A](a: Expr[A])(using Quotes): Expr[A] = {
    import quotes.reflect.*
    val sep = "="*120
    println(sep)
    try
      println(a.asTerm.show)
    catch {
      case t: Throwable =>
        println(t.getMessage)
    }
    println(sep)
    a
  }

  inline def showTasty[A](inline a: A): A =
    ${ showTastyImpl('a) }

  private def showTastyImpl[A](a: Expr[A])(using Quotes): Expr[A] = {
    import quotes.reflect.*
    val sep = "="*120
    println(sep)
    println(a.asTerm)
    println(sep)
    a
  }

}