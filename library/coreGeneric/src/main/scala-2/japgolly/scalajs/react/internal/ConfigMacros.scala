package japgolly.scalajs.react.internal

import japgolly.microlibs.compiletime._
import scala.reflect.macros.blackbox.Context

abstract class ConfigMacros(val c: Context) extends MacroUtils {
  import c.universe._

  protected def readConfig(key: String): Option[String] =
    (Option(System.getProperty(key, null)) orElse Option(System.getenv(key)))
      .map(_.trim.toLowerCase)
      .filter(_.nonEmpty)

  protected def modStr(expr: c.Expr[String])(f: String => c.Expr[String]): c.Expr[String] =
    expr match {
      case Expr(Literal(Constant(s: String))) => f(s)
      case _                                  => expr
    }

  protected implicit def lit(s: String): c.Expr[String] =
    c.Expr(Literal(Constant(s)))
}
