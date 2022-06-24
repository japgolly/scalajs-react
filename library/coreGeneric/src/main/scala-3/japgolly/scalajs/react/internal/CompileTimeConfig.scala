package japgolly.scalajs.react.internal

import japgolly.microlibs.compiletime.*
import japgolly.microlibs.compiletime.MacroEnv.*
import scala.quoted.*

object CompileTimeConfig {

  export CompileTimeInfo.{sysPropOrEnvVar => get}

  transparent inline def getTrimLowerCaseNonBlank(inline key: String): Option[String] =
    TransparentInlineUtils.trimLowerCaseNonBlank(get(key))

  transparent inline def getOrUseModule[A](inline key: String, inline fallback: A): A =
    ${ quoted.getOrUseModule[A]('key, 'fallback) }

  transparent inline def loadModuleOrError[A](inline fqcn: String): A =
    ${ quoted.loadModuleOrError[A]('fqcn) }

  // ========================================================================================================================
  object quoted {
    def getModule[A: Type](key: Expr[String])(using Quotes): Option[Expr[A]] = {
      import quotes.reflect.*
      val value: Option[String] =
        CompileTimeInfo.quoted.sysPropOrEnvVar(key).valueOrAbort
      value.map(fqcn => loadModuleOrError[A](Expr.inlineConst(fqcn)))
    }

    def getOrUseModule[A: Type](key: Expr[String], fallback: Expr[A])(using Quotes): Expr[A] = {
      import quotes.reflect.*
      getModule[A](key).getOrElse(fallback)
    }

    def loadModuleOrError[A: Type](fqcnExpr: Expr[String])(using Quotes): Expr[A] = {
      val fqcn = fqcnExpr.valueOrAbort
      loadModule(fqcn) match {
        case Right(e) => e
        case Left(e) => fail(s"$fqcn $e")
      }
    }

    def loadModule[A: Type](fqcn: String)(using Quotes): Either[String, Expr[A]] = {
      import quotes.reflect.*

      val mod = Symbol.requiredModule(fqcn)
      val ref = Ref(mod)
      val tpe = ref.tpe

      if tpe <:< TypeRepr.of[A] then
        Right(ref.asInlineExprOf[A])
      else if !tpe.exists then
        Left("doesn't exist")
      else
        Left("isn't an instance of " + Type.show[A])
    }

  }
}
