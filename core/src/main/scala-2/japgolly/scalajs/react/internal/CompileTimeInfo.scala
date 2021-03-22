package japgolly.scalajs.react.internal

import japgolly.microlibs.macro_utils.MacroUtils
import scala.reflect.macros.blackbox.Context

object CompileTimeInfo {

  def envVar(name: String): Option[String] =
    macro CompileTimeInfoMacros.envVar

  def sysProp(name: String): Option[String] =
    macro CompileTimeInfoMacros.sysProp

  def envVarOrSysProp(name: String): Option[String] =
    macro CompileTimeInfoMacros.envVarOrSysProp

  def sysPropOrEnvVar(name: String): Option[String] =
    macro CompileTimeInfoMacros.sysPropOrEnvVar
}

final class CompileTimeInfoMacros(val c: Context) extends MacroUtils {
  import c.universe._

  private def lit(o: Option[String]): c.Expr[Option[String]] =
    c.Expr[Option[String]](
      o match {
        case Some(s) => q"_root_.scala.Some(${Literal(Constant(s))})"
        case None    => q"_root_.scala.Option.empty[String]"
      }
    )

  private def _envVar(key: String): Option[String] =
    Option(System.getenv(key))

  private def _sysProp(key: String): Option[String] =
    Option(System.getProperty(key, null))

  def envVar(name: c.Expr[String]): c.Expr[Option[String]] = {
    val key = readMacroArg_string(name)
    val value = _envVar(key)
    lit(value)
  }

  def sysProp(name: c.Expr[String]): c.Expr[Option[String]] = {
    val key = readMacroArg_string(name)
    val value = _sysProp(key)
    lit(value)
  }

  def envVarOrSysProp(name: c.Expr[String]): c.Expr[Option[String]] = {
    val key = readMacroArg_string(name)
    val value = _envVar(key) orElse _sysProp(key)
    lit(value)
  }

  def sysPropOrEnvVar(name: c.Expr[String]): c.Expr[Option[String]] = {
    val key = readMacroArg_string(name)
    val value = _sysProp(key) orElse _envVar(key)
    lit(value)
  }
}
