package japgolly.scalajs.react.internal

import scala.quoted.*

object CompileTimeInfo {

  inline def envVar(inline name: String): Option[String] =
    ${ _envVar('name) }

  inline def sysProp(inline name: String): Option[String] =
    ${ _sysProp('name) }

  inline def envVarOrSysProp(inline name: String): Option[String] =
    ${ _envVarOrSysProp('name) }

  inline def sysPropOrEnvVar(inline name: String): Option[String] =
    ${ _sysPropOrEnvVar('name) }

  private def getEnvVar(key: String): Option[String] =
    Option(System.getenv(key))

  private def getSysProp(key: String): Option[String] =
    Option(System.getProperty(key, null))

  private def _envVar(name: Expr[String])(using Quotes): Expr[Option[String]] = {
    import quotes.reflect.*
    val key = name.valueOrError
    val value = getEnvVar(key)
    Expr(value)
  }

  private def _sysProp(name: Expr[String])(using Quotes): Expr[Option[String]] = {
    import quotes.reflect.*
    val key = name.valueOrError
    val value = getSysProp(key)
    Expr(value)
  }

  private def _envVarOrSysProp(name: Expr[String])(using Quotes): Expr[Option[String]] = {
    import quotes.reflect.*
    val key = name.valueOrError
    val value = getEnvVar(key) orElse getSysProp(key)
    Expr(value)
  }

  private def _sysPropOrEnvVar(name: Expr[String])(using Quotes): Expr[Option[String]] = {
    import quotes.reflect.*
    val key = name.valueOrError
    val value = getSysProp(key) orElse getEnvVar(key)
    Expr(value)
  }
}
