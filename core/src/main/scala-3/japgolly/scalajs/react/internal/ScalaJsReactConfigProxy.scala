package japgolly.scalajs.react.internal

import japgolly.microlibs.compiletime.MacroEnv.*
import japgolly.scalajs.react.ScalaJsReactConfig
import japgolly.scalajs.react.ScalaJsReactConfig.*
import scala.quoted.*

// This is here in .internal because users are expected to import japgolly.scalajs.react._
// and I don't want this included as it's not public API.
object ScalaJsReactConfigProxy {

  transparent inline def automaticComponentName(name: String) = ${ _automaticComponentName('name) }
  transparent inline def modifyComponentName   (name: String) = ${ _modifyComponentName('name) }
  transparent inline def reusabilityOverride                  = ${ _reusabilityOverride }

  private def _automaticComponentName(name: Expr[String])(using Quotes) =
    call[String]("automaticComponentName", (name -> "java.lang.String") :: Nil)

  private def _modifyComponentName(name: Expr[String])(using Quotes) =
    call[String]("modifyComponentName", (name -> "java.lang.String") :: Nil)

  private def _reusabilityOverride(using Quotes) =
    call[ReusabilityOverride]("reusabilityOverride", Nil)

  private def call[A](methodName: String, args: List[(Expr[Any], String | Int)])(using Quotes, Type[A]): Expr[A] = {
    import quotes.reflect.*

    val module =
      CompileTimeConfig.quoted.getModule[ScalaJsReactConfig](Expr.inlineConst(KeyConfigClass))
        .getOrElse('{Defaults}.inlined)
        .asTerm

    val moduleSym =
      module
        .tpe
        .classSymbol
        .getOrElse(fail("Unknown config module: " + module.show))

    val paramSigs =
      args.map(_._2)

    val methodSym =
      moduleSym
        .memberMethod(methodName)
        .iterator
        .filter(_.signature.paramSigs == paramSigs)
        .nextOption()
        .getOrElse(fail(s"${moduleSym.fullName}.$methodName either doesn't exist, or doesn't have the right signature."))

    var callTerm: Term =
      Select(module, methodSym)

    if (args.nonEmpty)
      callTerm = Apply(callTerm, args.map(_._1.asTerm))

    Inlined(None, Nil, callTerm).asExprOf[A]
  }
}
