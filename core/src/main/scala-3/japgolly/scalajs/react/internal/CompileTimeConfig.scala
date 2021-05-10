package japgolly.scalajs.react.internal

import japgolly.microlibs.compiletime.*
import japgolly.microlibs.compiletime.MacroEnv.*
import scala.quoted.*

object CompileTimeConfig {

  export CompileTimeInfo.{sysPropOrEnvVar => get}

  transparent inline def getTrimLowerCaseNonBlank(inline key: String): Option[String] =
    InlineUtils.trimLowerCaseNonBlank(get(key))

  inline def getOrUseModule[A](inline key: String, inline fallback: A): A =
    inline get(key) match {
      case Some(fqcn) => loadModuleOrError[A](fqcn)
      case None       => fallback
    }

  inline def loadModuleOrError[A](inline fqcn: String): A =
    ${ loadModuleOrError[A]('fqcn) }

  private def loadModuleOrError[A: Type](fqcn: Expr[String])(using Quotes): Expr[A] =
    loadModule(fqcn.valueOrError) match {
      case Right(e) => e
      case Left(e) => fail(e)
    }

  private def loadModule[A: Type](fqcn: String)(using Quotes): Either[String, Expr[A]] = {
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
